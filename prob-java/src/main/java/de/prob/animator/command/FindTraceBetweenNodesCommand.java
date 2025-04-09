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

public class FindTraceBetweenNodesCommand extends AbstractCommand implements
		ITraceDescription, IStateSpaceModifier {
	private static final String PROLOG_COMMAND_NAME = "find_trace_from_node_to_node";
	private static final String TRACE = "Trace";

	final List<Transition> newTransitions = new ArrayList<>();
	private final StateSpace stateSpace;
	private final String sourceId;
	private final String destId;

	public FindTraceBetweenNodesCommand(final StateSpace stateSpace,
			final String sourceId, final String destId) {
		this.stateSpace = stateSpace;
		this.sourceId = sourceId;
		this.destId = destId;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(sourceId);
		pto.printAtomOrNumber(destId);
		pto.printVariable(TRACE);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		PrologTerm trace = bindings.get(TRACE);
		if (trace instanceof ListPrologTerm) {
			for (PrologTerm term : (ListPrologTerm) trace) {
				newTransitions.add(Transition.createTransitionFromCompoundPrologTerm(
						stateSpace, term));
			}
		} else {
			throw new NoTraceFoundException("Trace was not found. Error was: " + trace.getFunctor());
		}
	}

	@Override
	public List<Transition> getNewTransitions() {
		return newTransitions;
	}

	@Override
	public Trace getTrace(final StateSpace s) {
		if (newTransitions.isEmpty()) {
			return new Trace(s.getState(sourceId));
		} else {
			return Trace.getTraceFromTransitions(s, newTransitions);
		}
	}

}
