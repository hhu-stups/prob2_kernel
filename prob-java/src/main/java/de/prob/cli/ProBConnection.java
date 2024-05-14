package de.prob.cli;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class ProBConnection implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProBConnection.class);

	private final int port;
	private final Socket socket;
	private final Scanner inputScanner;
	private final Writer outputWriter;
	private volatile boolean shuttingDown = false;

	public ProBConnection(final int port) throws IOException {
		this.port = port;

		LOGGER.debug("Connecting to port {}", this.port);
		InetAddress loopbackAddress = InetAddress.getByName(null);
		this.socket = new Socket(loopbackAddress, this.port);
		this.inputScanner = new Scanner(socket.getInputStream(), StandardCharsets.UTF_8.name())
			.useDelimiter("\u0001") // Prolog sends character 1 to terminate its outputs
			.useLocale(Locale.ROOT);
		this.outputWriter = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
		LOGGER.debug("Connected");
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("port", port)
			.add("shuttingDown", shuttingDown)
			.toString();
	}

	private static String shorten(final String s) {
		String trimmed = s.trim();
		if (trimmed.length() <= 200) {
			return trimmed;
		}

		return s.substring(0, 197) + "...";
	}

	public String send(final String term) throws IOException {
		if (this.shuttingDown) {
			LOGGER.error("Cannot send terms while probcli is shutting down: {}", term);
			throw new IOException("ProB has been shut down. It does not accept messages.");
		} else if (LOGGER.isDebugEnabled()) {
			String trimmed = term.trim(); // shorten will always trim, so we can save us some work if TRACE is enabled
			LOGGER.debug("{}", shorten(trimmed));
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Full term: {}", trimmed);
			}
		}

		this.outputWriter.write(term);
		this.outputWriter.write('\n');
		this.outputWriter.flush();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Sent: {}", shorten(term));
		}
		return this.getAnswer();
	}

	public String getAnswer() throws IOException {
		if (this.shuttingDown) {
			LOGGER.error("Cannot receive terms while probcli is shutting down");
			throw new IOException("ProB has been shut down. It does not send messages.");
		}

		String input;
		try {
			input = this.inputScanner.next();
		} catch (NoSuchElementException | IllegalStateException e) {
			throw new IOException("ProB binary returned nothing - it might have crashed", e);
		}

		LOGGER.trace("Answer: {}", input);
		return input;
	}

	public void disconnect() {
		try {
			this.close();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		LOGGER.debug("Disconnecting...");
		this.shuttingDown = true;
		this.socket.close();
	}
}
