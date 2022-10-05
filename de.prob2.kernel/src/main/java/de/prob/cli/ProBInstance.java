package de.prob.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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

	private final Collection<IConsoleOutputListener> consoleOutputListeners;

	private ProBInstance(final Process process, final BufferedReader stream, final Long userInterruptReference,
			final ProBConnection connection, final String home, final OsSpecificInfo osInfo) {
		this.process = process;
		this.connection = connection;
		final String command = home + osInfo.getUserInterruptCmd();
		interruptCommand = new String[] { command, Long.toString(userInterruptReference) };
		this.consoleOutputListeners = new ArrayList<>();
	}

	public static ProBInstance create(final Process process, final BufferedReader stream, final Long userInterruptReference,
			final ProBConnection connection, final String home, final OsSpecificInfo osInfo) {
		final ProBInstance instance = new ProBInstance(process, stream, userInterruptReference, connection, home, osInfo);
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
		this.thread = new Thread(new ConsoleListener(this, stream, this::logConsoleLine),
				String.format("ProB Output Logger for instance %x", this.hashCode()));
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
			if (thread != null) {
				thread.interrupt();
			}
			connection.disconnect();
		} finally {
			process.destroy();
		}
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
		return MoreObjects.toStringHelper(ProBInstance.class).addValue(connection).toString();
	}

}
