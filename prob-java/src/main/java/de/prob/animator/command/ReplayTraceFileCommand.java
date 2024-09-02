package de.prob.animator.command;

import java.util.Collections;
import java.util.List;

import de.prob.check.tracereplay.ReplayedTrace;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Transition;

public final class ReplayTraceFileCommand extends AbstractCommand implements IStateSpaceModifier {
	private static final String PROLOG_COMMAND_NAME = "prob2_replay_json_trace_file";
	
	private static final String REPLAY_STATUS_VAR = "ReplayStatus";
	private static final String TRANSITIONS_VAR = "Transitions";
	private static final String MATCH_INFO_LIST_VAR = "MatchInfoList";
	
	private final String path;
	
	private ReplayedTrace trace;
	
	public ReplayTraceFileCommand(final String path) {
		this.path = path;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(this.path);
		pto.printVariable(REPLAY_STATUS_VAR);
		pto.printVariable(TRANSITIONS_VAR);
		pto.printVariable(MATCH_INFO_LIST_VAR);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.trace = ReplayedTrace.fromProlog(bindings.get(REPLAY_STATUS_VAR), bindings.get(TRANSITIONS_VAR), bindings.get(MATCH_INFO_LIST_VAR));
	}
	
	public ReplayedTrace getTrace() {
		return trace;
	}
	
	@Override
	public List<Transition> getNewTransitions() {
		// TODO
		return Collections.emptyList();
	}
}
