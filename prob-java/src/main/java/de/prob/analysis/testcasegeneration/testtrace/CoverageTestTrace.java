package de.prob.analysis.testcasegeneration.testtrace;

import java.util.List;
import java.util.StringJoiner;

import de.prob.analysis.testcasegeneration.Target;
import de.prob.statespace.Trace;

/**
 * A test trace created by the test case generator with the aim to fulfill operation coverage.
 */
public class CoverageTestTrace extends TestTrace {

	public CoverageTestTrace(List<String> priorTransitions, Target target, boolean isComplete) {
		super(priorTransitions, target, target == null ? null : target.getOperation(), isComplete, true);
	}

	public CoverageTestTrace(List<String> priorTransitions, Target target, boolean isComplete, Trace trace) {
		super(priorTransitions, target, target == null ? null : target.getOperation(), isComplete, true, trace);
	}

	public CoverageTestTrace createNewTrace(List<String> transitions, Target t, boolean isComplete) {
		return new CoverageTestTrace(transitions, t, isComplete);
	}

	@Override
	public CoverageTestTrace createNewTrace(List<String> transitions, Target t, boolean isComplete, Trace trace) {
		return new CoverageTestTrace(transitions, t, isComplete, trace);
	}

	@Override
	public String toString() {
		StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
		transitionNames.forEach(stringJoiner::add);
		return stringJoiner.toString();
	}
}
