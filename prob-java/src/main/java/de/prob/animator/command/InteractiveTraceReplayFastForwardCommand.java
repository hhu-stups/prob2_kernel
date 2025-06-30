package de.prob.animator.command;

import de.prob.check.tracereplay.TransitionReplayPrecision;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.List;
import java.util.stream.Collectors;

public final class InteractiveTraceReplayFastForwardCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_interactive_replay_fast_forward";

	private static final String NR_STEPS = "NrSteps";
	private static final String TRANSITIONS = "Transitions";
	private static final String MATCH_INFOS = "MatchInfos";
	private static final String ERRORS = "Errors";

	private final int currentStepNr;
	private final String stateID;

	private int nrSteps;
	private List<PrologTerm> transitionTerms;
	private List<TransitionReplayPrecision> transitionPrecisions;
	private List<List<String>> transitionErrors;

	public InteractiveTraceReplayFastForwardCommand(final int currentStepNr, final String stateID) {
		this.currentStepNr = currentStepNr;
		this.stateID = stateID;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printNumber(currentStepNr);
		pto.printAtomOrNumber(stateID);
		pto.printVariable(NR_STEPS);
		pto.printVariable(TRANSITIONS);
		pto.printVariable(MATCH_INFOS);
		pto.printVariable(ERRORS);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.nrSteps = ((AIntegerPrologTerm) bindings.get(NR_STEPS)).intValueExact();
		this.transitionTerms = BindingGenerator.getList(bindings, TRANSITIONS);
		this.transitionPrecisions = BindingGenerator.getList(bindings, MATCH_INFOS).stream()
				.map(TransitionReplayPrecision::fromPrologTerm).collect(Collectors.toList());
		this.transitionErrors = BindingGenerator.getList(bindings, ERRORS).stream()
				.map(t -> PrologTerm.atomicsToStrings((ListPrologTerm) t))
				.collect(Collectors.toList());
	}
	
	public int getNrSteps() {
		return nrSteps;
	}

	public List<PrologTerm> getTransitionTerms() {
		return transitionTerms;
	}

	public List<TransitionReplayPrecision> getTransitionPrecisions() {
		return transitionPrecisions;
	}

	public List<List<String>> getTransitionErrors() {
		return transitionErrors;
	}

}
