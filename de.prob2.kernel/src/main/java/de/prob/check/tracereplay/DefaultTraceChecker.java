package de.prob.check.tracereplay;

import java.util.Map;

public class DefaultTraceChecker implements ITraceChecker {
	@Override
	public void updateProgress(double value, Map<String, Object> replayInformation) {
		System.out.println("Trace Replay Progress: " + value);
	}

	@Override
	public void setResult(boolean success, Map<String, Object> replayInformation) {
		if(success) {
			System.out.println("Trace Replay Successful");
		} else {
			System.out.println("Trace Replay Failed");
		}
	}

	@Override
	public void afterInterrupt() {
		//Nothing to do
	}

	@Override
	public void showError(TraceReplay.TraceReplayError errorType, Map<String, Object> replayInformation) {
		switch(errorType) {
			case COMMAND:
				System.out.println("Command for Trace Replay failed");
				break;
			case NO_OPERATION_POSSIBLE:
				System.out.println("Given Operation is not executable");
			case MISMATCH_OUTPUT:
				System.out.println("Mismatch Output Parameters");
				break;
			case TRACE_REPLAY:
				System.out.println("Trace Replay failed");
				break;
			default:
				break;
		}
	}
}
