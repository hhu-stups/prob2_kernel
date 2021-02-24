package de.prob.cli;

import java.util.Scanner;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.common.base.MoreObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProBConnection {

	private static final int BUFFER_SIZE = 1024;

	private Socket socket;
	private Scanner inputScanner;
	private PrintWriter outputStream;
	private final Logger logger = LoggerFactory.getLogger(ProBConnection.class);
	private volatile boolean shutingDown;
	private volatile boolean busy;
	private final String key;
	private final int port;

	public ProBConnection(final String key, final int port) {
		this.key = key;
		this.port = port;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ProBConnection.class).add("key", key)
				.add("port", port).toString();
	}

	public void connect() throws IOException {
		logger.debug("Connecting to port {} using key {}", port, key);
		socket = new Socket(InetAddress.getByName(null), port);
		inputScanner = new Scanner(socket.getInputStream()).useDelimiter("\u0001"); // Prolog sends character 1 to terminate its outputs
		OutputStream outstream = socket.getOutputStream();
		outputStream = new PrintWriter(new OutputStreamWriter(outstream, StandardCharsets.UTF_8));
		logger.debug("Connected");
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
		if (isStreamReady()) {
			outputStream.println(term);
			outputStream.flush();
		}
		String answer = getAnswer();
		return answer;
	}

	public boolean isBusy() {
		return busy;
	}

	public String getAnswer() throws IOException {
		String input = inputScanner.next();
	   // no need to do this anymore?: treated as ignore token in answerparser:
	   //          input.replace("\r", "").replace("\n", ""); // were two traversals !
		if (input == null) {
			throw new IOException(
					"ProB binary returned nothing - it might have crashed");
		}
		logger.trace(input);
		return input;
	}

	private boolean isStreamReady() {
		if (inputScanner == null || outputStream == null) {
			logger.warn("Stream to ProB server not ready");
			return false;
		}
		return true;
	}

	public void disconnect() {
		shutingDown = true;
	}

	public String getKey() {
		return key;
	}

}
