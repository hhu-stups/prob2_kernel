package de.prob.cli;

import java.io.BufferedReader;
import java.io.IOException;

import de.prob.animator.IConsoleOutputListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ConsoleListener implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleListener.class);

	private final BufferedReader stream;
	private final IConsoleOutputListener outputListener;

	ConsoleListener(BufferedReader stream, IConsoleOutputListener outputListener) {
		this.stream = stream;
		this.outputListener = outputListener;
	}

	@SuppressWarnings("try") // don't warn about unused resource in try
	public void run() {
		try (final BufferedReader ignored = this.stream) {
			String line = stream.readLine();
			while (line != null) {
				outputListener.lineReceived(line);
				line = stream.readLine();
			}
		} catch (IOException e) {
			if ("Stream closed".equals(e.getMessage())) {
				LOGGER.debug("CLI stdout stream closed - stopping ConsoleListener", e);
			} else {
				LOGGER.info("ConsoleListener died with error", e);
			}
		}
	}
}
