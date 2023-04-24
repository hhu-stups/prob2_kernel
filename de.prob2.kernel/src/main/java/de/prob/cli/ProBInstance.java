package de.prob.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;

import de.prob.animator.IConsoleOutputListener;
import de.prob.exception.CliError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProBInstance {

	private Thread thread;

	private volatile boolean shuttingDown = false;

	final Logger logger = LoggerFactory.getLogger(ProBInstance.class);
	private final Process process;

	private final ProBConnection connection;

	private String[] interruptCommand;

	// ProBInstanceProvider needs to be notified when each instance shuts down,
	// so that the instance and its process are removed from the respective collections.
	private final ProBInstanceProvider provider;

	private final Collection<IConsoleOutputListener> consoleOutputListeners;

	private ProBInstance(
		final Process process, final BufferedReader stream, final Long userInterruptReference,
		final ProBConnection connection, final String home, final OsSpecificInfo osInfo,
		final ProBInstanceProvider provider
	) {
		this.process = process;
		this.connection = connection;
		final String command = home + osInfo.getUserInterruptCmd();
		interruptCommand = new String[] { command, Long.toString(userInterruptReference) };
		this.provider = provider;
		// Because the console output logger is its own thread,
		// we have to worry about thread safety when listeners are added/removed.
		// CopyOnWriteArrayList makes more sense than explicit synchronization,
		// because the list is read very often (for every line of output) and almost never changes.
		this.consoleOutputListeners = new CopyOnWriteArrayList<>();
	}

	static ProBInstance create(
		final Process process, final BufferedReader stream, final Long userInterruptReference,
		final ProBConnection connection, final String home, final OsSpecificInfo osInfo,
		final ProBInstanceProvider provider
	) {
		final ProBInstance instance = new ProBInstance(process, stream, userInterruptReference, connection, home, osInfo, provider);
		// The output logger thread must be started after the constructor,
		// to prevent the thread from possibly seeing final instance fields before they are initialized
		// (in particular, logger and consoleOutputListeners).
		// This is rare, but possible - see the Java Language Specification, section 17.5. "final Field Semantics".
		instance.startOutputPublisher(stream);
		return instance;
	}

	private void logConsoleLine(final String line) {
		logger.info("{}\u001b[0m", line);
		for (final IConsoleOutputListener l : this.consoleOutputListeners) {
			l.lineReceived(line);
		}
	}

	private void startOutputPublisher(final BufferedReader stream) {
		this.thread = new Thread(new ConsoleListener(stream, this::logConsoleLine),
				"ProB Output Logger for " + this.process);
		this.thread.start();
	}

	public void addConsoleOutputListener(final IConsoleOutputListener listener) {
		this.consoleOutputListeners.add(listener);
	}

	public void removeConsoleOutputListener(final IConsoleOutputListener listener) {
		this.consoleOutputListeners.remove(listener);
	}

	public void shutdown() {
		shuttingDown = true;
		try {
			this.sendInterrupt();
			connection.disconnect();

			final boolean exited = process.waitFor(1, TimeUnit.SECONDS);
			if (exited) {
				final int exitCode = process.exitValue();
				if (exitCode != 0) {
					logger.warn("{} exited with non-zero status {}", this, exitCode);
				}
			} else {
				logger.warn("{} is taking more than 1 second to exit - will destroy the process instead", this);
			}
		} catch (InterruptedException exc) {
			logger.warn("Thread interrupted while waiting for {} to exit - will destroy the process instead", this, exc);
		} finally {
			process.destroy();
		}

		final boolean exited;
		try {
			exited = process.waitFor(1, TimeUnit.SECONDS);
		} catch (InterruptedException exc) {
			logger.warn("Thread interrupted while waiting for {} to exit after being destroyed - ignoring", this, exc);
			return;
		}
		if (exited) {
			final int exitCode = process.exitValue();
			if (exitCode != 0) {
				logger.warn("{} exited with non-zero status {} after being destroyed", this, exitCode);
			}
		} else {
			logger.warn("{} is taking more than 1 second to exit after being destroyed - ignoring", this);
		}

		provider.instanceWasShutDown(this, this.process);
	}

	public void sendInterrupt() {
		try {
			logger.info("sending interrupt signal");
			// calls send_user_interrupt or send_user_interrupt.exe on Windows
			final int exitCode = new ProcessBuilder(interruptCommand).start().waitFor();
			if (exitCode != 0) {
				logger.warn("send_user_interrupt command exited with status code {}", exitCode);
			} else {
				logger.trace("send_user_interrupt command exited successfully");
			}
		} catch (IOException e) {
			logger.warn("calling the send_user_interrupt command failed", e);
		} catch (InterruptedException e) {
			logger.warn("Thread interrupted while waiting for send_user_interrupt to finish", e);
		}
	}

	// a method to send a string to Prolog and return the response provided by Prolog
	public String send(final String term) {
		try {
			return connection.send(term);
		} catch (IOException e) {
			throw new CliError("Error during communication with Prolog core.", e);
		}
	}
	// a method to just receive input from Prolog, without sending a string first
	public String receive() {
		try {
			return connection.getAnswer();
		} catch (IOException e) {
			throw new CliError("Error receiving from Prolog core.", e);
		}
	}
	
	

	public boolean isShuttingDown() {
		return shuttingDown;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ProBInstance.class)
			.addValue(this.process)
			.addValue(this.connection)
			.toString();
	}

}
