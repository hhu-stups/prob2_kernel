package de.prob.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.prob.annotations.Home;
import de.prob.exception.CliError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class ProBInstanceProvider implements Provider<ProBInstance> {
	static final class CliInformation {
		private final int port;
		private final long userInterruptReference;

		CliInformation(final int port, final long userInterruptReference) {
			this.port = port;
			this.userInterruptReference = userInterruptReference;
		}

		int getPort() {
			return port;
		}

		long getUserInterruptReference() {
			return userInterruptReference;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(ProBInstanceProvider.class);

	static final Pattern CLI_PORT_PATTERN = Pattern.compile("^.*Port: (\\d+)$");
	static final Pattern CLI_USER_INTERRUPT_REFERENCE_PATTERN = Pattern.compile("^.*user interrupt reference id: *(\\d+) *$");

	private final PrologProcessProvider processProvider;
	private final String home;
	private final OsSpecificInfo osInfo;
	private final AtomicInteger processCounter;
	private final Set<WeakReference<ProBInstance>> processes = new HashSet<>();

	@Inject
	public ProBInstanceProvider(final PrologProcessProvider processProvider,
			@Home final String home, final OsSpecificInfo osInfo, final Installer installer) {
		this.processProvider = processProvider;
		this.home = home;
		this.osInfo = osInfo;
		installer.ensureCLIsInstalled();
		processCounter = new AtomicInteger();
	}

	@Override
	public ProBInstance get() {
		return startProlog();
	}

	@Deprecated
	public int numberOfCLIs() {
		return processCounter.get();
	}

	public void shutdownAll() {
		for (WeakReference<ProBInstance> wr : processes) {
			ProBInstance process = wr.get();
			if (process != null) {
				process.shutdown();
			}
		}
	}
	
	/**
	 * Return {@code process}'s exit code as an {@link Integer}, or {@link Optional#empty()} if it is still running.
	 * 
	 * @param process the process whose exit code to get
	 * @return {@code process}'s exit code, or {@link Optional#empty()} if it is still running
	 */
	private static Optional<Integer> getProcessExitCode(final Process process) {
		try {
			return Optional.of(process.exitValue());
		} catch (final IllegalThreadStateException ignored) {
			return Optional.empty();
		}
	}

	private ProBInstance startProlog() {
		ProcessHandle processTuple = processProvider.get();
		Process process = processTuple.getProcess();
		String key = processTuple.getKey();
		final BufferedReader stream = new BufferedReader(new InputStreamReader(
				process.getInputStream(), StandardCharsets.UTF_8));

		final CliInformation cliInformation;
		try {
			cliInformation = extractCliInformation(stream);
		} catch (CliError e) {
			// Check if the CLI exited while extracting the information.
			final Optional<Integer> exitCode = getProcessExitCode(process);
			if (exitCode.isPresent()) {
				// CLI exited, report the exit code.
				throw new CliError("CLI exited with status " + exitCode.get() + " while matching output patterns", e);
			} else {
				// CLI didn't exit, just rethrow the error.
				throw e;
			}
		}

		final ProBConnection connection;
		try {
			connection = new ProBConnection(key, cliInformation.getPort());
		} catch (IOException e) {
			throw new CliError("Error connecting to Prolog binary.", e);
		}
		processCounter.incrementAndGet();
		ProBInstance cli = new ProBInstance(process, stream,
				cliInformation.getUserInterruptReference(), connection, home, osInfo,
				processCounter);
		processes.add(new WeakReference<>(cli));
		return cli;
	}

	// prob_socketserver.pl prints the following:
	// format(Stdout,'Port: ~w~n', [Port]),
	// format(Stdout,'probcli revision: ~w~n',[Revision]),
	// format(Stdout,'user interrupt reference id: ~w~n',[Ref]),
	// format(Stdout,'-- starting command loop --~n', []),
	// The patterns match some of the lines and collect info
	CliInformation extractCliInformation(final BufferedReader input) {
		Integer port = null;
		Long userInterruptReference = null;

		try {
			String line;
			do {
				line = input.readLine();
				if (line == null) {
					break;
				}
				logger.info("Apply cli detection patterns to {}", line);

				final Matcher portMatcher = CLI_PORT_PATTERN.matcher(line);
				if (portMatcher.matches()) {
					port = Integer.parseInt(portMatcher.group(1));
					logger.info("Server has started and listens on port {}", port);
				}

				final Matcher userInterruptReferenceMatcher = CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
				if (userInterruptReferenceMatcher.matches()) {
					userInterruptReference = Long.parseLong(userInterruptReferenceMatcher.group(1));
					logger.info("Server can receive user interrupts via reference {}", userInterruptReference);
				}
			} while ((port == null || userInterruptReference == null) && !line.contains("starting command loop"));
		} catch (IOException e) {
			final String message = "Problem while starting ProB. Cannot read from input stream.";
			logger.error(message);
			logger.debug(message, e);
			throw new CliError(message, e);
		}

		if (port == null) {
			throw new CliError("Did not receive port number from CLI");
		}
		if (userInterruptReference == null) {
			throw new CliError("Did not receive user interrupt reference from CLI");
		}
		return new CliInformation(port, userInterruptReference);
	}
}
