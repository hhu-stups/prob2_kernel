package de.prob.animator.domainobjects;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import com.google.common.io.ByteStreams;

import de.prob.exception.ProBError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an interface for calling the {@code dot} command-line tool to render a graph from a dot file.
 */
public final class DotCall {
	private static final Logger LOGGER = LoggerFactory.getLogger(DotCall.class);
	
	private final String dotCommand;
	private String layoutEngine;
	private String outputFormat;
	private List<String> extraDotArgs;
	private byte[] input;
	
	/**
	 * Set up a new dot call.
	 * 
	 * @param dotCommand the name or path of the dot command to use
	 */
	public DotCall(final String dotCommand) {
		super();
		
		this.dotCommand = Objects.requireNonNull(dotCommand, "dotCommand");
	}
	
	/**
	 * Set the layout engine that dot should use.
	 * {@link DotLayoutEngine} provides constants for common layout engine names.
	 * This setting is optional - by default the layout engine is derived from the command name (normally dot).
	 * 
	 * @param layoutEngine the layout engine to use
	 * @return {@code this}
	 */
	public DotCall layoutEngine(final String layoutEngine) {
		Objects.requireNonNull(layoutEngine, "layoutEngine");
		if (this.layoutEngine != null) {
			throw new IllegalStateException("layoutEngine already set");
		}
		this.layoutEngine = layoutEngine;
		return this;
	}
	
	/**
	 * Set the output format that dot should produce.
	 * {@link DotLayoutEngine} provides constants for common output format names.
	 * This setting is required.
	 *
	 * @param outputFormat the output format to produce
	 * @return {@code this}
	 */
	public DotCall outputFormat(final String outputFormat) {
		Objects.requireNonNull(outputFormat, "outputFormat");
		if (this.outputFormat != null) {
			throw new IllegalStateException("outputFormat already set");
		}
		this.outputFormat = outputFormat;
		return this;
	}
	
	/**
	 * Set arbitrary extra arguments to pass to dot.
	 * 
	 * @param extraDotArgs extra arguments to pass to dot
	 * @return {@code this}
	 */
	public DotCall extraDotArgs(final List<String> extraDotArgs) {
		Objects.requireNonNull(extraDotArgs, "extraDotArgs");
		if (this.extraDotArgs != null) {
			throw new IllegalStateException("extraDotArgs already set");
		}
		this.extraDotArgs = extraDotArgs;
		return this;
	}
	
	/**
	 * Set the Graphviz source code to provide to dot.
	 * This method can be used to provide source code that might not be encoded as UTF-8.
	 * This setting is required.
	 *
	 * @param input the source code to provide to dot
	 * @return {@code this}
	 */
	public DotCall input(final byte[] input) {
		Objects.requireNonNull(input, "input");
		if (this.input != null) {
			throw new IllegalStateException("input already set");
		}
		this.input = input;
		return this;
	}
	
	/**
	 * Set the Graphviz source code to provide to dot.
	 * The source code will be internally encoded as UTF-8,
	 * which dot expects by default.
	 * Use {@link #input(byte[])} if you need to provide source code using a different encoding.
	 * This setting is required.
	 *
	 * @param input the source code to provide to dot
	 * @return {@code this}
	 */
	public DotCall input(final String input) {
		return this.input(input.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Get a {@link RunnableFuture} for the configured dot command,
	 * which can be used to call dot asynchronously.
	 * The dot command is started by calling {@link RunnableFuture#run()} on the returned runnable future.
	 * 
	 * @return a {@link RunnableFuture} for the configured dot command
	 */
	public RunnableFuture<byte[]> getRunnableFuture() {
		final List<String> fullDotCommand = new ArrayList<>();
		if (this.dotCommand == null) {
			throw new IllegalStateException("dotCommand must be set");
		}
		fullDotCommand.add(this.dotCommand);
		if (this.layoutEngine != null) {
			fullDotCommand.add("-K" + this.layoutEngine);
		}
		if (this.outputFormat == null) {
			throw new IllegalStateException("outputFormat must be set");
		}
		fullDotCommand.add("-T" + this.outputFormat);
		if (this.extraDotArgs != null) {
			fullDotCommand.addAll(this.extraDotArgs);
		}
		if (this.input == null) {
			throw new IllegalStateException("input must be set");
		}
		
		final ProcessBuilder dotProcessBuilder = new ProcessBuilder(fullDotCommand);
		
		return new FutureTask<>(() -> {
			LOGGER.debug("Starting dot command: {}", dotProcessBuilder.command());
			final Process dotProcess = dotProcessBuilder.start();
			
			// Write to stdin in a background thread, so that if dot's stdin buffer fills up, it doesn't block our code.
			final Thread stdinWriter = new Thread(() -> {
				try {
					dotProcess.getOutputStream().write(this.input);
					dotProcess.getOutputStream().close();
				} catch (IOException e) {
					if (dotProcess.isAlive()) {
						LOGGER.error("Failed to write dot input", e);
					} else {
						LOGGER.info("dot is no longer alive - could not write input", e);
					}
				}
			}, String.format("stdin writer for DotProcess %x", this.hashCode()));
			stdinWriter.start();
			
			// Read stderr in a background thread, to prevent the stream buffer from filling up and blocking dot.
			// (This is very unlikely to happen, because dot normally doesn't produce a lot of stderr output.)
			final StringJoiner errorOutput = new StringJoiner("\n");
			final Thread stderrLogger = new Thread(() -> {
				try (
					final Reader reader = new InputStreamReader(dotProcess.getErrorStream());
					final BufferedReader br = new BufferedReader(reader);
				) {
					br.lines().forEach(line -> {
						errorOutput.add(line);
						LOGGER.error("Error output from dot: {}", line);
					});
				} catch (IOException | UncheckedIOException e) {
					if (dotProcess.isAlive()) {
						LOGGER.error("Failed to read dot error output", e);
					} else {
						LOGGER.info("dot is no longer alive - could not read error output", e);
					}
				}
			}, String.format("stderr reader for DotProcess %x", this.hashCode()));
			stderrLogger.start();
			
			// Read stdout while dot is running, to prevent the stream buffer from filling up and blocking dot.
			// (Unlike with stderr, this actually happens in practice, when the generated output is large.)
			final ByteArrayOutputStream renderedStream = new ByteArrayOutputStream();
			final Thread stdoutReader = new Thread(() -> {
				try {
					ByteStreams.copy(dotProcess.getInputStream(), renderedStream);
				} catch (IOException e) {
					if (dotProcess.isAlive()) {
						LOGGER.error("Failed to read dot output", e);
					} else {
						LOGGER.info("dot is no longer alive - could not read output", e);
					}
				}
			}, String.format("stdout reader for DotProcess %x", this.hashCode()));
			stdoutReader.start();
			
			final int exitCode;
			try {
				exitCode = dotProcess.waitFor();
			} catch (InterruptedException e) {
				LOGGER.debug("DotCall task interrupted, terminating dot process", e);
				dotProcess.destroy();
				throw e;
			}
			LOGGER.debug("dot exited with status code {}", exitCode);
			
			if (exitCode != 0) {
				stderrLogger.join(); // Make sure that all stderr output has been read
				throw new ProBError("dot exited with status code " + exitCode + ":\n" + errorOutput);
			}
			
			stdoutReader.join(); // Make sure that all output has been read
			return renderedStream.toByteArray();
		});
	}
	
	/**
	 * Call the configured dot command synchronously.
	 * This call blocks until the dot process has finished (successfully or not).
	 * 
	 * @return the converted output produced by dot
	 * @throws InterruptedException if the current thread is interrupted
	 * @throws ProBError if the dot command failed
	 */
	public byte[] call() throws InterruptedException {
		final RunnableFuture<byte[]> runnableFuture = this.getRunnableFuture();
		runnableFuture.run();
		try {
			return runnableFuture.get();
		} catch (ExecutionException e) {
			if (e.getCause() instanceof InterruptedException) {
				throw (InterruptedException)e.getCause();
			} else {
				throw new ProBError(e.getCause());
			}
		}
	}
}
