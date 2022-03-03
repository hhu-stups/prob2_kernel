package de.prob.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.ref.WeakReference;

import de.prob.animator.IConsoleOutputListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ConsoleListener implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleListener.class);

	private final WeakReference<ProBInstance> cli;
	private final BufferedReader stream;
	private final IConsoleOutputListener outputListener;

	ConsoleListener(ProBInstance cli, BufferedReader stream, IConsoleOutputListener outputListener) {
		this.cli = new WeakReference<>(cli);
		this.stream = stream;
		this.outputListener = outputListener;
	}

	@SuppressWarnings("try") // don't warn about unused resource in try
	public void run() {
		try (final BufferedReader ignored = this.stream) {
			logLines();
		} catch (IOException e) {
			if ("Stream closed".equals(e.getMessage())) {
				LOGGER.debug("CLI stdout stream closed - stopping ConsoleListener", e);
			} else {
				LOGGER.info("ConsoleListener died with error", e);
			}
		}
	}

	void logLines() throws IOException {
		String line;
		do {
			ProBInstance instance = cli.get();
			if (instance == null || instance.isShuttingDown()) {
				return;
			}
			line = readAndLog();
		} while (line != null);
	}

	String readAndLog() throws IOException {
		String line;
		line = stream.readLine();
		if (line != null) {
			outputListener.lineReceived(line);
		}
		return line;
	}

}
