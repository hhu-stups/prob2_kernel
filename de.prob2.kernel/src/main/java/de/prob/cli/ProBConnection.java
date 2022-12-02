package de.prob.cli;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.common.base.MoreObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProBConnection {
	private final Scanner inputScanner;
	private final PrintWriter outputStream;
	private final Logger logger = LoggerFactory.getLogger(ProBConnection.class);
	private volatile boolean shutingDown;
	private final int port;

	public ProBConnection(final int port) throws IOException {
		this.port = port;

		logger.debug("Connecting to port {}", this.port);
		// The socket is closed in .disconnect() by closing its input/output streams.
		@SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed", "SocketOpenedButNotSafelyClosed"})
		final Socket socket = new Socket(InetAddress.getByName(null), this.port);
		inputScanner = new Scanner(socket.getInputStream()).useDelimiter("\u0001"); // Prolog sends character 1 to terminate its outputs
		outputStream = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		logger.debug("Connected");
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ProBConnection.class)
				.add("port", port).toString();
	}

	private static String shorten(final String s) {
		final String shortened = s.length() <= 200 ? s : (s.substring(0, 200) + "...");
		return shortened.endsWith("\n") ? shortened.substring(0, shortened.length()-1) : shortened;
	}

	public String send(final String term) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug(shorten(term));
		}
		if (shutingDown) {
			logger.error("Cannot send terms while probcli is shutting down: {}", term);
			throw new IOException("ProB has been shut down. It does not accept messages.");
		}
		outputStream.println(term);
		outputStream.flush();
		String answer = getAnswer();
		return answer;
	}

	public String getAnswer() throws IOException {
		String input;
		try {
			input = inputScanner.next();
		} catch (NoSuchElementException e) {
			throw new IOException("ProB binary returned nothing - it might have crashed", e);
		}
		logger.trace(input);
		return input;
	}

	public void disconnect() {
		shutingDown = true;
		inputScanner.close();
		outputStream.close();
	}

	/**
	 * @deprecated The debugging key has no use anymore. probcli no longer supports the debug_console/2 command.
	 */
	@Deprecated
	public String getKey() {
		return "";
	}

}
