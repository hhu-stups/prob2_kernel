package de.prob.cli;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import de.prob.annotations.Home;
import de.prob.exception.CliError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(ProBInstanceProvider.class);

	static final Pattern CLI_PORT_PATTERN = Pattern.compile("^.*Port: (\\d+)$");
	static final Pattern CLI_USER_INTERRUPT_REFERENCE_PATTERN = Pattern.compile("^.*user interrupt reference id: *(\\d+|off) *$");

	private final String home;
	private final OsSpecificInfo osInfo;
	private final Collection<Process> runningProcesses = new CopyOnWriteArrayList<>();
	private final Collection<ProBInstance> runningInstances = new CopyOnWriteArrayList<>();

	@Inject
	public ProBInstanceProvider(@Home final String home, final OsSpecificInfo osInfo, final Installer installer) {
		this.home = home;
		this.osInfo = osInfo;

		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownAll, "Prolog Process Destroyer"));

		installer.ensureCLIsInstalled();
	}

	@Override
	public ProBInstance get() {
		return this.startProlog();
	}

	void instanceWasShutDown(ProBInstance instance, Process process) {
		this.runningInstances.remove(instance);
		this.runningProcesses.remove(process);
	}

	public void shutdownAll() {
		for (ProBInstance instance : this.runningInstances) {
			// This also removes the instance and its process from the respective lists.
			instance.shutdown();
		}

		// Clean up Process objects that were never wrapped in a ProBInstance for some reason.
		for (Process process : this.runningProcesses) {
			process.destroy();
			// CopyOnWriteArrayList doesn't support Iterator.remove, but regular remove works fine in this case.
			this.runningProcesses.remove(process);

			final boolean exited;
			try {
				exited = process.waitFor(1, TimeUnit.SECONDS);
				if (exited) {
					final int exitCode = process.exitValue();
					if (exitCode != 0) {
						LOGGER.warn("Orphaned probcli process {} exited with non-zero status {} after being destroyed", process, exitCode);
					}
				} else {
					LOGGER.warn("Orphaned probcli process {} is taking more than 1 second to exit after being destroyed - ignoring", process);
				}
			} catch (InterruptedException exc) {
				LOGGER.warn("Thread interrupted while waiting for orphaned probcli process {} to exit after being destroyed", process, exc);
				Thread.currentThread().interrupt();
			} catch (Exception ignored) {
			}
		}
	}

	Process makeProcess() {
		final String executable = this.home + this.osInfo.getCliName();
		final List<String> command = new ArrayList<>();
		command.add(executable);
		command.add("-sf");
		final ProcessBuilder pb = new ProcessBuilder(command);
		pb.environment().put("PROB_HOME", this.home);
		pb.redirectErrorStream(true);

		final Process prologProcess;
		try {
			LOGGER.info("\nStarting ProB's Prolog Core. Path is {}", executable);
			prologProcess = pb.start();
			LOGGER.debug("probcli -sf started");
		} catch (Exception e) {
			throw new CliError("Problem while starting up ProB CLI: " + e.getMessage(), e);
		}

		this.runningProcesses.add(prologProcess);
		return prologProcess;
	}

	/**
	 * Return {@code process}'s exit code as an {@link Integer}, or {@link Optional#empty()} if it is still running.
	 *
	 * @param process the process whose exit code to get
	 * @return {@code process}'s exit code, or {@link Optional#empty()} if it is still running
	 */
	private static OptionalInt getProcessExitCode(final Process process) {
		try {
			return OptionalInt.of(process.exitValue());
		} catch (final IllegalThreadStateException ignored) {
			return OptionalInt.empty();
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
			final OptionalInt exitCode = getProcessExitCode(process);
			if (exitCode.isPresent()) {
				// CLI exited, report the exit code.
				throw new CliError("CLI exited with status " + exitCode.getAsInt() + " before socket connection could be opened", e);
			} else {
				// CLI didn't exit, just rethrow the error.
				throw e;
			}
		}

		final ProBConnection connection;
		try {
			connection = new ProBConnection(cliInformation.getPort());
		} catch (Exception e) {
			throw new CliError("Error while opening socket connection to CLI", e);
		}
		ProBInstance cli = ProBInstance.create(process, stream,
			cliInformation.getUserInterruptReference(), connection, this.home, this.osInfo, this);
		this.runningInstances.add(cli);
		return cli;
	}

	// prob_socketserver.pl prints the following:
	// format(Stdout,'Port: ~w~n', [Port]),
	// format(Stdout,'probcli revision: ~w~n',[Revision]),
	// format(Stdout,'user interrupt reference id: ~w~n',[Ref]),
	// format(Stdout,'-- starting command loop --~n', []),
	// The patterns match some of the lines and collect info
	static CliInformation extractCliInformation(final BufferedReader input) {
		Integer port = null;
		Long userInterruptReference = null;
		try {
			for (String line; (line = input.readLine()) != null; ) {
				LOGGER.info("CLI startup output: {}", line);

				final Matcher portMatcher = CLI_PORT_PATTERN.matcher(line);
				if (portMatcher.matches()) {
					port = Integer.parseInt(portMatcher.group(1));
					LOGGER.info("Received port number from CLI: {}", port);
				}

				final Matcher userInterruptReferenceMatcher = CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
				if (userInterruptReferenceMatcher.matches()) {
					final String userInterruptReferenceString = userInterruptReferenceMatcher.group(1);
					if ("off".equals(userInterruptReferenceString)) {
						userInterruptReference = -1L;
						LOGGER.info("This ProB build has user interrupt support disabled. Interrupting ProB may not work as expected.");
					} else {
						userInterruptReference = Long.parseLong(userInterruptReferenceString);
						LOGGER.info("Received user interrupt reference from CLI: {}", userInterruptReference);
					}
				}

				if ((port != null && userInterruptReference != null)
					|| line.contains("starting command loop")) {
					break;
				}
			}
		} catch (Exception e) {
			throw new CliError("Error while reading information from CLI", e);
		}

		if (port == null) {
			throw new CliError("Did not receive port number from CLI");
		} else if (userInterruptReference == null) {
			throw new CliError("Did not receive user interrupt reference from CLI");
		}
		return new CliInformation(port, userInterruptReference);
	}
}
