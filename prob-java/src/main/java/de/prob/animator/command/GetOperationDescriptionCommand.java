package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class GetOperationDescriptionCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "get_operation_description_for_state_and_transition_id";
	private static final String RESULT_VARIABLE = "Desc";

	private final String stateId;
	private final String transitionId;
	private String description;

	public GetOperationDescriptionCommand(final String stateId, final String transitionId) {
		// get_operation_description_for_state_and_transition_id(StateId,TransId,Desc)
		this.stateId = stateId;
		this.transitionId = transitionId;
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.description = bindings.get(RESULT_VARIABLE).atomToString();
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(stateId);
		pto.printAtomOrNumber(transitionId);
		pto.printVariable(RESULT_VARIABLE);
		pto.closeTerm();
	}

	public String getDescription() {
		return description;
	}
}
