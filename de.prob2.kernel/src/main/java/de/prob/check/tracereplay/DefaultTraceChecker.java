package de.prob.check.tracereplay;

import java.util.List;
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
	public void setResult(boolean success, List<List<Boolean>> postconditionResults, Map<String, Object> replayInformation) {
		if(success) {
			System.out.println("Trace Replay and Checking Postconditions Successful");
		} else {
			System.out.println("Trace Replay and Checking Postconditions Failed");
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

	@Override
	public void showTestError(PersistentTrace persistentTrace, List<List<Boolean>> postconditionResults) {
		StringBuilder sb = new StringBuilder();
		List<PersistentTransition> transitions = persistentTrace.getTransitionList();
		boolean failed = false;
		for(int i = 0; i < transitions.size(); i++) {
			PersistentTransition transition = transitions.get(i);
			List<Boolean> postconditionTransitionResults = postconditionResults.get(i);
			for(int j = 0; j < postconditionTransitionResults.size(); j++) {
				boolean result = postconditionTransitionResults.get(j);
				if(!result) {
					Postcondition postcondition = transition.getPostconditions().get(j);
					switch (postcondition.getKind()) {
						case PREDICATE:
							sb.append(String.format("Checking predicate postcondition in transition %s failed for predicate %s", transition.getOperationName(), ((PostconditionPredicate) postcondition).getPredicate()));
							sb.append("\n");
							break;
						case ENABLEDNESS: {
							String predicate = ((OperationEnabledness) postcondition).getPredicate();
							if (predicate.isEmpty()) {
								sb.append(String.format("Checking enabledness postcondition in transition %s failed for operation %s", transition.getOperationName(), ((OperationEnabledness) postcondition).getOperation()));
							} else {
								sb.append(String.format("Checking enabledness postcondition in transition %s failed for operation %s for predicate %s", transition.getOperationName(), ((OperationEnabledness) postcondition).getOperation(), predicate));
							}
							sb.append("\n");
							break;
						}
						case DISABLEDNESS: {
							String predicate = ((OperationDisabledness) postcondition).getPredicate();
							if (predicate.isEmpty()) {
								sb.append(String.format("Checking disabledness postcondition in transition %s failed for operation %s", transition.getOperationName(), ((OperationDisabledness) postcondition).getOperation()));
							} else {
								sb.append(String.format("Checking disabledness postcondition in transition %s failed for operation %s for predicate %s", transition.getOperationName(), ((OperationDisabledness) postcondition).getOperation(), predicate));
							}
							sb.append("\n");
							break;
						}
						default:
							throw new RuntimeException("Postcondition class is unknown: " + postcondition.getKind());
					}
					failed = true;
				}
			}
		}
		if(failed) {
			System.out.println("Checking tests for trace failed");
			System.out.println(sb.toString());
		}
	}
}
