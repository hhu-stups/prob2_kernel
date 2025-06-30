package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.ITraceDescription;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

public class GetShortestTraceCommand extends AbstractCommand implements
		ITraceDescription, IStateSpaceModifier {
	private static final String PROLOG_COMMAND_NAME = "find_trace_to_node";
	private static final String TRACE = "Trace";

	private final String stateId;
	private final List<Transition> transitions = new ArrayList<>();
	private boolean tracefound;

	private final StateSpace s;

	public GetShortestTraceCommand(final StateSpace s, final String stateId) {
		this.s = s;
		this.stateId = stateId;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(stateId);
		pto.printVariable(TRACE);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		PrologTerm trace = bindings.get(TRACE);
		if (trace instanceof ListPrologTerm) {
			tracefound = true;
			for (PrologTerm term : (ListPrologTerm) trace) {
				transitions.add(Transition.createTransitionFromCompoundPrologTerm(
					s, term));
			}
		} else {
			tracefound = false;
		}
	}

	@Override
	public List<Transition> getNewTransitions() {
		return transitions;
	}

	public boolean traceFound() {
		return tracefound;
	}

	@Override
	public Trace getTrace(final StateSpace s) {
		if (!tracefound) {
			throw new NoTraceFoundException("No trace was found");
		}
		return Trace.getTraceFromTransitions(s, transitions);
	}
}
