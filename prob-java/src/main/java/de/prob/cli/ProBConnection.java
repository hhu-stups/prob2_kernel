package de.prob.cli;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;

import com.google.common.base.MoreObjects;

import de.prob.prolog.output.FastSicstusTermOutput;
import de.prob.prolog.output.FastSwiTermOutput;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProBConnection implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProBConnection.class);

	private final ProBInstanceProvider.CliInformation info;
	private final Socket socket;
	private final Scanner inputScanner;
	private final Writer outputWriter;
	private final IPrologTermOutput termOutput;
	private volatile boolean shuttingDown = false;

	ProBConnection(final ProBInstanceProvider.CliInformation info) throws IOException {
		this.info = info;

		LOGGER.debug("Connecting to port {}", this.info.getPort());
		InetAddress loopbackAddress = InetAddress.getByName(null);
		this.socket = new Socket(loopbackAddress, this.info.getPort());
		this.inputScanner = new Scanner(socket.getInputStream(), StandardCharsets.UTF_8.name())
			.useDelimiter("\u0001") // Prolog sends character 1 to terminate its outputs
			.useLocale(Locale.ROOT);
		this.outputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		if (this.info.isFastRw()) {
			if ("swi".equals(this.info.getProlog())) {
				this.termOutput = new GroundReprTermOutput(new FastSwiTermOutput(new BufferedOutputStream(socket.getOutputStream())));
			} else if ("sicstus".equals(this.info.getProlog())) {
				this.termOutput = new GroundReprTermOutput(new FastSicstusTermOutput(new BufferedOutputStream(socket.getOutputStream())));
			} else {
				LOGGER.warn("Unknown prolog system {}, unable to configure socket for fastrw communication. Falling back to textual term.", this.info.getProlog());
				this.termOutput = new PrologTermOutput(this.outputWriter, false);
			}
		} else {
			this.termOutput = new PrologTermOutput(this.outputWriter, false);
		}
		LOGGER.debug("Connected");
	}

	@Deprecated
	public ProBConnection(final int port) throws IOException {
		this(new ProBInstanceProvider.CliInformation(port, -1, "sicstus", false));
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("port", info.getPort())
			.add("prolog", info.getProlog())
			.add("fastrw", info.isFastRw())
			.add("shuttingDown", shuttingDown)
			.toString();
	}

	private static String shorten(final String s) {
		final int maxLength = 200;
		if (s.length() <= maxLength) {
			if (s.endsWith("\n")) {
				return s.substring(0, s.length() - 1);
			} else {
				return s;
			}
		} else {
			String trimmed = s.substring(0, maxLength - 3);
			return trimmed + "...";
		}
	}

	public String send(final String term) throws IOException {
		if (this.isFastRw()) {
			throw new IllegalStateException("Cannot set string via fastrw connection");
		}

		if (this.shuttingDown) {
			LOGGER.error("Cannot send terms while probcli is shutting down: {}", term);
			throw new IOException("ProB has been shut down. It does not accept messages.");
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Sending: {}", shorten(term));
			if (LOGGER.isTraceEnabled()) {
				String trimmed = term.endsWith("\n") ? term.substring(0, term.length() - 1) : term;
				LOGGER.trace("Full term: {}", trimmed);
			}
		}
		this.outputWriter.write(term);
		this.outputWriter.write('\n');
		this.outputWriter.flush();
		LOGGER.trace("Sent");

		return this.getAnswer();
	}

	public String send(final Consumer<IPrologTermOutput> termOutput) throws IOException {
		if (this.shuttingDown) {
			LOGGER.error("Cannot send terms while probcli is shutting down");
			throw new IOException("ProB has been shut down. It does not accept messages.");
		}

		LOGGER.debug("Sending command term directly");
		termOutput.accept(this.termOutput); // the command should end with a fullstop
		this.outputWriter.flush();
		LOGGER.trace("Sent");

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

	boolean isFastRw() {
		return this.info.isFastRw();
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
