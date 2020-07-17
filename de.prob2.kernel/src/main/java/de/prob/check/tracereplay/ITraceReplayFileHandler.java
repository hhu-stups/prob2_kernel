package de.prob.check.tracereplay;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @deprecated Use the exception-throwing {@link TraceLoaderSaver} methods instead.
 */
@Deprecated
public interface ITraceReplayFileHandler {

	void showSaveError(IOException e);
	void showLoadError(Path path, Exception e);

}
