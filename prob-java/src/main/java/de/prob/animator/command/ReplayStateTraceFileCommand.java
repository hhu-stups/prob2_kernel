package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public final class ReplayStateTraceFileCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "replay_state_trace_from_file";

	private static final String DEST_ID_VAR = "DestID";

	private final String path, stateId;
	private String destStateId;

	public ReplayStateTraceFileCommand(final String path, final String stateId) {
		this.path = path;
		this.stateId = stateId;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(this.path);
		pto.printAtomOrNumber(this.stateId);
		pto.printVariable(DEST_ID_VAR);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.destStateId = bindings.get(DEST_ID_VAR).getFunctor();
	}

	public String getDestStateId() {
		return destStateId;
	}
}
