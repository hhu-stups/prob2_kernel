package de.prob.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
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
	static final Pattern CLI_USER_INTERRUPT_REFERENCE_PATTERN = Pattern.compile("^.*user interrupt reference id: *(\\d+|off) *$");

	private final String home;
	private final OsSpecificInfo osInfo;
	private final Collection<Process> toDestroyOnShutdown = new CopyOnWriteArrayList<>();
	private final Set<WeakReference<ProBInstance>> processes = new HashSet<>();

	@Inject
	public ProBInstanceProvider(@Home final String home, final OsSpecificInfo osInfo, final Installer installer) {
		this.home = home;
		this.osInfo = osInfo;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			for (final Process process : toDestroyOnShutdown) {
				process.destroy();
			}
		}, "Prolog Process Destroyer"));

		installer.ensureCLIsInstalled();
	}

	@Override
	public ProBInstance get() {
		return startProlog();
	}

	public void shutdownAll() {
		for (WeakReference<ProBInstance> wr : processes) {
			ProBInstance process = wr.get();
			if (process != null) {
				process.shutdown();
			}
		}

		// Clean up Process objects that were never wrapped in a ProBInstance for some reason.
		for (final Process process : toDestroyOnShutdown) {
			process.destroy();
		}
	}

	Process makeProcess() {
		final String executable = this.home + osInfo.getCliName();
		final List<String> command = new ArrayList<>();
		command.add(executable);
		command.add("-sf");
		final ProcessBuilder pb = new ProcessBuilder(command);
		pb.environment().put("PROB_HOME", this.home);
		pb.redirectErrorStream(true);

		final Process prologProcess;
		try {
			logger.info("\nStarting ProB's Prolog Core. Path is {}", executable);
			prologProcess = pb.start();
			logger.debug("probcli -sf started");
		} catch (IOException e) {
			throw new CliError("Problem while starting up ProB CLI: " + e.getMessage(), e);
		}

		toDestroyOnShutdown.add(prologProcess);
		return prologProcess;
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
		Process process = makeProcess();
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
				throw new CliError("CLI exited with status " + exitCode.get() + " before socket connection could be opened", e);
			} else {
				// CLI didn't exit, just rethrow the error.
				throw e;
			}
		}

		final ProBConnection connection;
		try {
			connection = new ProBConnection(cliInformation.getPort());
		} catch (IOException e) {
			throw new CliError("Error while opening socket connection to CLI", e);
		}
		ProBInstance cli = ProBInstance.create(process, stream,
				cliInformation.getUserInterruptReference(), connection, home, osInfo);
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

		String line;
		do {
			try {
				line = input.readLine();
			} catch (IOException e) {
				throw new CliError("Error while reading information from CLI", e);
			}
			if (line == null) {
				break;
			}
			logger.info("CLI startup output: {}", line);
			
			final Matcher portMatcher = CLI_PORT_PATTERN.matcher(line);
			if (portMatcher.matches()) {
				port = Integer.parseInt(portMatcher.group(1));
				logger.info("Received port number from CLI: {}", port);
			}
			
			final Matcher userInterruptReferenceMatcher = CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
			if (userInterruptReferenceMatcher.matches()) {
				final String userInterruptReferenceString = userInterruptReferenceMatcher.group(1);
				if ("off".equals(userInterruptReferenceString)) {
					userInterruptReference = -1L;
					logger.info("This ProB build has user interrupt support disabled. Interrupting ProB may not work as expected.");
				} else {
					userInterruptReference = Long.parseLong(userInterruptReferenceString);
					logger.info("Received user interrupt reference from CLI: {}", userInterruptReference);
				}
			}
		} while ((port == null || userInterruptReference == null) && !line.contains("starting command loop"));

		if (port == null) {
			throw new CliError("Did not receive port number from CLI");
		}
		if (userInterruptReference == null) {
			throw new CliError("Did not receive user interrupt reference from CLI");
		}
		return new CliInformation(port, userInterruptReference);
	}
}
