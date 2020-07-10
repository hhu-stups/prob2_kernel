package de.prob.check.tracereplay;

import java.util.Map;

public interface ITraceChecker {

    void updateProgress(double value, Map<String, Object> replayInformation);
    void setResult(boolean success, Map<String, Object> replayInformation);
    void interrupt();
    void showError(TraceReplay.TraceReplayError errorType, Map<String, Object> replayInformation);
}
