package de.prob.check;

import java.util.ArrayList;
import java.util.List;

import de.prob.animator.command.LtlCheckingCommand.PathType;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.LTL;
import de.prob.statespace.ITraceDescription;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

public class LTLCounterExample extends AbstractEvalResult implements IModelCheckingResult, 
		ITraceDescription {

	private final LTL formula;
	private final StateSpace stateSpace;
	private final List<Transition> pathToCE;
	private final List<Transition> counterExample;
	private final int loopEntry;
	private final PathType pathType;

	public LTLCounterExample(final LTL formula,
			final StateSpace stateSpace,
			final List<Transition> pathToCE,
			final List<Transition> counterExample, final int loopEntry,
			final PathType pathType) {
		super();
		this.formula = formula;
		this.stateSpace = stateSpace;
		this.pathToCE = pathToCE;
		this.counterExample = counterExample;
		this.loopEntry = loopEntry;
		this.pathType = pathType;

	}

	public int getLoopEntryPosition() {
		return this.loopEntry;
	}

	public Transition getLoopEntry() {
		if (this.getLoopEntryPosition() == -1) {
			return null;
		}
		return counterExample.get(this.getLoopEntryPosition());
	}

	public PathType getPathType() {
		return pathType;
	}

	public List<Transition> getOpList() {
		List<Transition> ops = new ArrayList<>();
		ops.addAll(pathToCE);
		ops.addAll(counterExample);
		return ops;
	}

	public String getCode() {
		return formula.getCode();
	}

	@Override
	public String getMessage() {
		return "LTL counterexample found";
	}

	public Trace getTrace() {
		Trace t = new Trace(this.stateSpace);
		t = t.addTransitions(pathToCE);
		t = t.addTransitions(counterExample);
		return t;
	}

	@Override
	public Trace getTrace(final StateSpace s) {
		if (!this.stateSpace.equals(s)) {
			throw new IllegalArgumentException("This LTL counterexample was found in state space " + this.stateSpace + " and cannot be represented as a trace in a different state space: " + s);
		}
		return this.getTrace();
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
