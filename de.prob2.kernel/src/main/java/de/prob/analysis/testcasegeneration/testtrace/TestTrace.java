package de.prob.analysis.testcasegeneration.testtrace;

import de.prob.analysis.testcasegeneration.Target;
import de.prob.statespace.Trace;

import java.util.ArrayList;
import java.util.List;

/**
 * A test trace (just a different denotation of test case to distinguish between target test cases and generated
 * test cases) created by the test case generator.
 *
 * It consists of a list of transitions and an identifier whether the trace is complete.
 * A complete trace cannot be extended, either because it has been statically proven to be infeasible (containing an infeasible
 *   operation or an infeasible combination of operations) or because it contains a final operation (as last operation)
 *  Note: final operations were introduced for JavaCard mutation testing and were mutants of correctly implemented Java bytecodes; they should only every be used as last operation in a test trace.
 */
public abstract class TestTrace {

    protected final List<String> transitionNames = new ArrayList<>();
    protected final Target target;
    private final boolean isComplete;
    private final boolean lastTransitionIsFeasible;
    private final Trace trace;

    public TestTrace(List<String> priorTransitions, Target target, String newTransition, boolean isComplete, boolean lastTransitionIsFeasible) {
        transitionNames.addAll(priorTransitions);
        this.target = target;
        if (newTransition != null) {
            transitionNames.add(newTransition);
        }
        this.isComplete = isComplete;
        this.lastTransitionIsFeasible = lastTransitionIsFeasible;
        this.trace = null;
    }

    public TestTrace(List<String> priorTransitions, Target target, String newTransition, boolean isComplete, boolean lastTransitionIsFeasible, Trace trace) {
        transitionNames.addAll(priorTransitions);
        this.target = target;
        if (newTransition != null) {
            transitionNames.add(newTransition);
        }
        this.isComplete = isComplete;
        this.lastTransitionIsFeasible = lastTransitionIsFeasible;
        this.trace = trace;
    }

    public List<String> getTransitionNames() {
        return transitionNames;
    }

    public int getDepth() {
        return transitionNames.size();
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean lastTransitionIsFeasible() {
        return lastTransitionIsFeasible;
    }

    public Trace getTrace() {
        return trace;
    }
    
    public Target getTarget() {
		return target;
	}

    public abstract TestTrace createNewTrace(List<String> transitions, Target t, boolean isComplete, Trace trace);
}