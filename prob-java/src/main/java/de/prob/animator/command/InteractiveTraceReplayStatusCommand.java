package de.prob.animator.command;

import de.prob.check.tracereplay.TransitionReplayPrecision;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.util.List;

public final class InteractiveTraceReplayStatusCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_interactive_replay_status";

	private static final String OP_TERM = "OpTerm";
	private static final String MATCH_INFO = "MatchInfo";
	private static final String ERRORS = "Errors";

	private final int currentStepNr;
	private final String stateID;

	private PrologTerm opTerm;
	private TransitionReplayPrecision matchInfo;
	private List<String> errors;

	public InteractiveTraceReplayStatusCommand(final int currentStepNr, final String stateID) {
		this.currentStepNr = currentStepNr;
		this.stateID = stateID;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printNumber(currentStepNr);
		pto.printAtomOrNumber(stateID);
		pto.printVariable(OP_TERM);
		pto.printVariable(MATCH_INFO);
		pto.printVariable(ERRORS);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		PrologTerm op = bindings.get(OP_TERM);
		this.opTerm = op != null && !op.hasFunctor("none") ? op : null;
		this.matchInfo = TransitionReplayPrecision.fromPrologTerm(bindings.get(MATCH_INFO));
		this.errors = PrologTerm.atomicsToStrings(BindingGenerator.getList(bindings.get(ERRORS)));
	}

	public PrologTerm getOpTerm() {
		return opTerm;
	}

	public TransitionReplayPrecision getMatchInfo() {
		return matchInfo;
	}

	public List<String> getErrors() {
		return errors;
	}

}
