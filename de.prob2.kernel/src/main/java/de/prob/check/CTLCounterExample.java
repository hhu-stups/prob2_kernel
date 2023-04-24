package de.prob.check;

import de.prob.animator.domainobjects.CTL;
import de.prob.statespace.ITraceDescription;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.util.List;

public class CTLCounterExample implements IModelCheckingResult, ITraceDescription {

	private final StateSpace s;

	private final CTL ctl;

	private final List<Transition> transitions;

	public CTLCounterExample(final StateSpace s, final CTL ctl, final List<Transition> transitions) {
		this.s = s;
		this.ctl = ctl;
		this.transitions = transitions;
	}

	@Override
	public String getMessage() {
		return "CTL counterexample found";
	}

	public String getCode() {
		return ctl.getCode();
	}

	@Override
	public String toString() {
		return getMessage();
	}

	public List<Transition> getTransitions() {
		return transitions;
	}

	public Trace getTrace() {
		Trace t = new Trace(this.s);
		t = t.addTransitions(transitions);
		return t;
	}

	@Override
	public Trace getTrace(final StateSpace s) {
		if (!this.s.equals(s)) {
			throw new IllegalArgumentException("This CTL counterexample was found in state space " + this.s + " and cannot be represented as a trace in a different state space: " + s);
		}
		return this.getTrace();
	}
}
