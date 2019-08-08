package de.prob.analysis.testcasegeneration.testtrace;

import de.prob.analysis.testcasegeneration.Target;
import de.prob.statespace.Trace;

import java.util.List;
import java.util.StringJoiner;

/**
 * A test trace created by the test case generator with the aim to fulfill operation coverage.
 */
public class CoverageTestTrace extends TestTrace {

    public CoverageTestTrace(List<String> priorTransitions, String newTransition, boolean isComplete) {
        super(priorTransitions, newTransition, isComplete, true);
    }

    public CoverageTestTrace(List<String> priorTransitions, String newTransition, boolean isComplete, Trace trace) {
        super(priorTransitions, newTransition, isComplete, true, trace);
    }

    public CoverageTestTrace createNewTrace(List<String> transitions, Target t, boolean isComplete) {
        return new CoverageTestTrace(transitions, t.getOperation(), isComplete);
    }

    public CoverageTestTrace createNewTrace(List<String> transitions, Target t, boolean isComplete, Trace trace) {
        return new CoverageTestTrace(transitions, t.getOperation(), isComplete, trace);
    }

    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
        transitionNames.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }
}
