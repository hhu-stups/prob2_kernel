package de.prob.analysis.testcasegeneration;

import java.util.List;

import de.prob.analysis.testcasegeneration.testtrace.TestTrace;

/**
 * The result of the test case generation, wrapping the final test traces and the uncovered targets.
 */
public class TestCaseGeneratorResult {

	private final List<TestTrace> testTraces;
	private final List<Target> uncoveredTargets;
	private final List<String> infeasibleOperations;
	private final boolean interrupted;

	public TestCaseGeneratorResult(final List<TestTrace> testTraces, final List<Target> uncoveredTargets,
							final List<String> infeasibleOperations, final boolean interrupted) {
		this.testTraces = testTraces;
		this.testTraces.removeIf(t -> t.getTransitionNames().isEmpty());
		this.uncoveredTargets = uncoveredTargets;
		this.infeasibleOperations = infeasibleOperations;
		this.interrupted = interrupted;
	}

	public List<TestTrace> getTestTraces() {
		return testTraces;
	}

	public List<Target> getUncoveredTargets() {
		return uncoveredTargets;
	}

	public List<String> getInfeasibleOperations() {
		return infeasibleOperations;
	}

	public boolean isInterrupted() {
		return interrupted;
	}
}
