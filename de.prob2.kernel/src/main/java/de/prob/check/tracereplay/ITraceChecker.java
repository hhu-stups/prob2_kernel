package de.prob.check.tracereplay;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import de.prob.statespace.StateSpace;

/**
 * @deprecated Use {@link TraceReplay#replayTraceFile(StateSpace, Path)} and {@link ReplayedTrace} instead.
 */
@Deprecated
public interface ITraceChecker {

	void updateProgress(double value, Map<String, Object> replayInformation);
	void setResult(boolean success, Map<String, Object> replayInformation);
	void setResult(boolean success, List<List<TraceReplay.PostconditionResult>> postconditionResults, Map<String, Object> replayInformation);
	void afterInterrupt();
	void showError(TraceReplay.TraceReplayError errorType, Map<String, Object> replayInformation);
	void showTestError(PersistentTrace persistentTrace, List<List<TraceReplay.PostconditionResult>> postconditionResults);
}
