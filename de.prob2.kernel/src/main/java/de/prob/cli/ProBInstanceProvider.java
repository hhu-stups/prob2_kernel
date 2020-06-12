package de.prob.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.prob.annotations.Home;
import de.prob.exception.CliError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class ProBInstanceProvider implements Provider<ProBInstance> {
	private static final Logger logger = LoggerFactory.getLogger(ProBInstanceProvider.class);

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

		final Map<Class<? extends AbstractCliPattern<?>>, AbstractCliPattern<?>> cliInformation;
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

		Integer port = ((PortPattern) cliInformation.get(PortPattern.class))
				.getValue();
		Long userInterruptReference = ((InterruptRefPattern) cliInformation
				.get(InterruptRefPattern.class)).getValue();

		ProBConnection connection = new ProBConnection(key, port);

		try {
			connection.connect();
		} catch (IOException e) {
			throw new CliError("Error connecting to Prolog binary.", e);
		}
		processCounter.incrementAndGet();
		ProBInstance cli = new ProBInstance(process, stream,
				userInterruptReference, connection, home, osInfo,
				processCounter);
		processes.add(new WeakReference<>(cli));
		return cli;
	}

	Map<Class<? extends AbstractCliPattern<?>>, AbstractCliPattern<?>> extractCliInformation(
			final BufferedReader input) {
		final PortPattern portPattern = new PortPattern();
		final InterruptRefPattern intPattern = new InterruptRefPattern();

		Map<Class<? extends AbstractCliPattern<?>>, AbstractCliPattern<?>> pattern = new HashMap<>();
		pattern.put(PortPattern.class, portPattern);
		pattern.put(InterruptRefPattern.class, intPattern);
		analyseStdout(input, pattern.values());
		return pattern;
	}

	// prob_socketserver.pl prints the following:
	// format(Stdout,'Port: ~w~n', [Port]),
	// format(Stdout,'probcli revision: ~w~n',[Revision]),
	// format(Stdout,'user interrupt reference id: ~w~n',[Ref]),
	// format(Stdout,'-- starting command loop --~n', []),
	// The patterns match some of the lines and collect info
	private static void analyseStdout(final BufferedReader input, final Collection<? extends AbstractCliPattern<?>> patterns) {
		final List<AbstractCliPattern<?>> patternsList = new ArrayList<>(patterns);
		try {
			String line;
			do {
				line = input.readLine();
				if (line == null) {
					break;
				}
				logger.info("Apply cli detection patterns to {}", line);
				applyPatterns(patternsList, line);
			} while (!patternsList.isEmpty() && !line.contains("starting command loop"));
		} catch (IOException e) {
			final String message = "Problem while starting ProB. Cannot read from input stream.";
			logger.error(message);
			logger.debug(message, e);
			throw new CliError(message, e);
		}
		// if p is not empty we have failed to find some patterns:
		// todoL also provide actual input (line) above in error message
		for (AbstractCliPattern<?> p : patternsList) {
			p.notifyNotFound();
			if (p.notFoundIsFatal()) {
				throw new CliError("Missing info from CLI " + p.getClass().getSimpleName());
			}
		}
	}

	private static void applyPatterns(final Collection<? extends AbstractCliPattern<?>> patterns, final String line) {
		patterns.removeIf(p -> p.matchesLine(line));
	}
}
