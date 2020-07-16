package de.prob.check.tracereplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class DefaultTraceReplayFileHandler implements ITraceReplayFileHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTraceReplayFileHandler.class);

	public void showSaveError(IOException e) {
		LOGGER.warn("Trace could not be saved", e);
	}

	public void showLoadError(Path path, Exception e) {
		LOGGER.warn("Trace could not be loaded", e);
	}

}
