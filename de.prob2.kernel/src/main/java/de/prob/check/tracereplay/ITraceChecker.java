package de.prob.check.tracereplay;

import java.util.Map;

public interface ITraceChecker {

    void updateProgress(double value, Map<String, Object> replayInformation);
    void setResult(boolean success, Map<String, Object> replayInformation);
    void interrupt();
    void showCommandErrors(Map<String, Object> replayInformation);
    void showNoOperationPossible(Map<String, Object> replayInformation);
    void showTraceReplayError(Exception e);
    void showMismatchOutputParameters(Map<String, Object> replayInformation);

}
