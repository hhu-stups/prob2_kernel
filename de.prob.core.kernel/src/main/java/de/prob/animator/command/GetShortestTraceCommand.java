package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.ITraceDescription;
import de.prob.statespace.OpInfo;
import de.prob.statespace.StateId;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

public class GetShortestTraceCommand extends AbstractCommand implements
		ITraceDescription, IStateSpaceModifier {

	Logger logger = LoggerFactory.getLogger(GetShortestTraceCommand.class);

	private static final String TRACE = "Trace";
	private final StateId id;
	private final List<OpInfo> transitions = new ArrayList<OpInfo>();

	public GetShortestTraceCommand(final StateId id) {
		this.id = id;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm("find_trace_to_node");
		pto.printAtomOrNumber(id.getId());
		pto.printVariable(TRACE);
		pto.closeTerm();
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		PrologTerm trace = bindings.get(TRACE);
		if (trace instanceof ListPrologTerm) {
			for (PrologTerm term : (ListPrologTerm) trace) {
				transitions
						.add(OpInfo
								.createOpInfoFromCompoundPrologTerm((CompoundPrologTerm) term));
			}
		} else {
			String msg = "Trace was not found. Error was: "
					+ trace.getFunctor();
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	@Override
	public List<OpInfo> getNewTransitions() {
		return transitions;
	}

	@Override
	public Trace getTrace(final StateSpace s) throws RuntimeException {
		return Trace.getTraceFromOpList(s, transitions);
	}
}
