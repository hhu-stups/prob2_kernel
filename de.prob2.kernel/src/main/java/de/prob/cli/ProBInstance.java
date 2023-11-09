package de.prob.cli;

import com.google.common.base.MoreObjects;
import de.prob.animator.IConsoleOutputListener;
import de.prob.exception.CliError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public final class ProBInstance implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProBInstance.class);

	private final Process probProcess;
	private final Thread outputLoggerThread;

	private final ProBConnection connection;

	private final List<String> interruptCommand;

	// ProBInstanceProvider needs to be notified when each instance shuts down,
	// so that the instance and its process are removed from the respective collections.
	private final ProBInstanceProvider provider;

	private final Collection<IConsoleOutputListener> consoleOutputListeners;
	private volatile boolean shuttingDown = false;

	private ProBInstance(
		final Process probProcess, final BufferedReader stream, final long userInterruptReference,
		final ProBConnection connection, final String home, final OsSpecificInfo osInfo,
		final ProBInstanceProvider provider
	) {
		this.probProcess = probProcess;
		this.outputLoggerThread = new Thread(new ConsoleListener(stream, this::logConsoleLine), "ProB Output Logger for " + this.probProcess);
		this.connection = connection;
		final String command = home + osInfo.getUserInterruptCmd();
		this.interruptCommand = Arrays.asList(command, Long.toString(userInterruptReference));
		this.provider = provider;
		// Because the console output logger is its own thread,
		// we have to worry about thread safety when listeners are added/removed.
		// CopyOnWriteArrayList makes more sense than explicit synchronization,
		// because the list is read very often (for every line of output) and almost never changes.
		this.consoleOutputListeners = new CopyOnWriteArrayList<>();
	}

	static ProBInstance create(
		final Process process, final BufferedReader stream, final long userInterruptReference,
		final ProBConnection connection, final String home, final OsSpecificInfo osInfo,
		final ProBInstanceProvider provider
	) {
		final ProBInstance instance = new ProBInstance(process, stream, userInterruptReference, connection, home, osInfo, provider);
		// The output logger thread must be started after the constructor,
		// to prevent the thread from possibly seeing final instance fields before they are initialized
		// (in particular, logger and consoleOutputListeners).
		// This is rare, but possible - see the Java Language Specification, section 17.5. "final Field Semantics".
		instance.startOutputPublisher();
		return instance;
	}

	private void logConsoleLine(final String line) {
		LOGGER.info("{}\u001b[0m", line);
		for (final IConsoleOutputListener l : this.consoleOutputListeners) {
			l.lineReceived(line);
		}
	}

	private void startOutputPublisher() {
		this.outputLoggerThread.start();
	}

	public void addConsoleOutputListener(final IConsoleOutputListener listener) {
		this.consoleOutputListeners.add(listener);
	}

	public void removeConsoleOutputListener(final IConsoleOutputListener listener) {
		this.consoleOutputListeners.remove(listener);
	}

	public void shutdown() {
		try {
			this.close();
		} catch (Exception ignored) {}
	}

	public void sendInterrupt() {
		try {
			LOGGER.info("sending interrupt signal");
			// calls send_user_interrupt or send_user_interrupt.exe on Windows
			final int exitCode = new ProcessBuilder(this.interruptCommand).start().waitFor();
			if (exitCode != 0) {
				LOGGER.warn("send_user_interrupt command exited with status code {}", exitCode);
			} else {
				LOGGER.trace("send_user_interrupt command exited successfully");
			}
		} catch (InterruptedException e) {
			LOGGER.warn("Thread interrupted while waiting for send_user_interrupt to finish", e);
		} catch (Exception e) {
			LOGGER.warn("calling the send_user_interrupt command failed", e);
		}
	}

	/**
	 * a method to send a string to Prolog and return the response provided by Prolog
	 */
	public String send(final String term) {
		try {
			return connection.send(term);
		} catch (IOException e) {
			throw new CliError("Error during communication with Prolog core.", e);
		}
	}

	/**
	 * a method to just receive input from Prolog, without sending a string first
	 */
	public String receive() {
		try {
			return connection.getAnswer();
		} catch (IOException e) {
			throw new CliError("Error receiving from Prolog core.", e);
		}
	}

	public boolean isShuttingDown() {
		return this.shuttingDown;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.addValue(this.probProcess)
			.addValue(this.connection)
			.toString();
	}

	@Override
	public void close() {
		this.shuttingDown = true;
		try {
			this.sendInterrupt();
			this.connection.disconnect();

			final boolean exited = this.probProcess.waitFor(1, TimeUnit.SECONDS);
			if (exited) {
				final int exitCode = this.probProcess.exitValue();
				if (exitCode != 0) {
					LOGGER.warn("{} exited with non-zero status {}", this, exitCode);
				}
			} else {
				LOGGER.warn("{} is taking more than 1 second to exit - will destroy the process instead", this);
			}
		} catch (InterruptedException exc) {
			LOGGER.warn("Thread interrupted while waiting for {} to exit - will destroy the process instead", this, exc);
		} catch (Exception ignored) {
		} finally {
			this.probProcess.destroy();
		}

		final boolean exited;
		try {
			exited = this.probProcess.waitFor(1, TimeUnit.SECONDS);
			if (exited) {
				final int exitCode = this.probProcess.exitValue();
				if (exitCode != 0) {
					LOGGER.warn("{} exited with non-zero status {} after being destroyed", this, exitCode);
				}
			} else {
				LOGGER.warn("{} is taking more than 1 second to exit after being destroyed - ignoring", this);
			}
		} catch (InterruptedException exc) {
			LOGGER.warn("Thread interrupted while waiting for {} to exit after being destroyed - ignoring", this, exc);
		} catch (Exception ignored) {
		}

		this.provider.instanceWasShutDown(this, this.probProcess);
	}
}
