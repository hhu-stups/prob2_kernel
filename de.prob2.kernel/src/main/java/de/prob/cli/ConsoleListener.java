package de.prob.cli;

import de.prob.animator.IConsoleOutputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;

final class ConsoleListener implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleListener.class);

	private final BufferedReader stream;
	private final IConsoleOutputListener outputListener;

	ConsoleListener(BufferedReader stream, IConsoleOutputListener outputListener) {
		this.stream = stream;
		this.outputListener = outputListener;
	}

	@Override
	@SuppressWarnings("try") // javac warns about unused resource (stream) in try-with-resources
	public void run() {
		try (final BufferedReader ignored = this.stream) {
			for (String line; (line = stream.readLine()) != null; ) {
				this.outputListener.lineReceived(line);
			}
		} catch (Exception e) {
			if ("Stream closed".equals(e.getMessage())) {
				LOGGER.debug("CLI stdout stream closed - stopping ConsoleListener", e);
			} else {
				LOGGER.warn("ConsoleListener died with error", e);
				throw new RuntimeException("ConsoleListener died with error", e);
			}
		}
	}
}
