package de.prob.check.tracereplay;

import java.io.IOException;
import java.nio.file.Path;

public interface ITraceReplayFileHandler {

    void showSaveError(IOException e);
    void showLoadError(Path path, Exception e);

}
