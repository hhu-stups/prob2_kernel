package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.check.tracereplay.TraceReplayStatus;
import de.prob.check.tracereplay.TransitionReplayPrecision;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.ITraceDescription;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

public final class ReplayTraceFileCommand extends AbstractCommand implements ITraceDescription {
	private static final String PROLOG_COMMAND_NAME = "prob2_replay_json_trace_file";
	
	private static final String REPLAY_STATUS_VAR = "ReplayStatus";
	private static final String TRANSITIONS_VAR = "Transitions";
	private static final String MATCH_INFO_LIST_VAR = "MatchInfoList";
	
	private final String path;
	
	private TraceReplayStatus replayStatus;
	private List<PrologTerm> transitionTerms;
	private List<TransitionReplayPrecision> transitionReplayPrecisions;
	private List<List<String>> transitionErrorMessages;
	
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
		this.replayStatus = TraceReplayStatus.fromPrologTerm(bindings.get(REPLAY_STATUS_VAR));
		this.transitionTerms = BindingGenerator.getList(bindings, TRANSITIONS_VAR);
		this.transitionReplayPrecisions = new ArrayList<>();
		this.transitionErrorMessages = new ArrayList<>();
		for (final PrologTerm term : BindingGenerator.getList(bindings, MATCH_INFO_LIST_VAR)) {
			BindingGenerator.getCompoundTerm(term, "replay_step", 2);
			this.transitionReplayPrecisions.add(TransitionReplayPrecision.fromPrologTerm(term.getArgument(1)));
			
			final List<String> errorMessages = new ArrayList<>();
			for (final PrologTerm errorTerm : BindingGenerator.getList(term.getArgument(2))) {
				if ("rerror".equals(errorTerm.getFunctor()) && errorTerm.getArity() == 1) {
					errorMessages.add(PrologTerm.atomicString(errorTerm.getArgument(1)));
				} else {
					errorMessages.add(errorTerm.toString());
				}
			}
			this.transitionErrorMessages.add(Collections.unmodifiableList(errorMessages));
		}
	}
	
	public TraceReplayStatus getReplayStatus() {
		return this.replayStatus;
	}
	
	@Override
	public Trace getTrace(final StateSpace s) {
		final List<Transition> transitions = this.transitionTerms.stream()
			.map(t -> BindingGenerator.getCompoundTerm(t, "op", 4))
			.map(t -> Transition.createTransitionFromCompoundPrologTerm(s, t))
			.collect(Collectors.toList());
		return new Trace(s).addTransitions(transitions);
	}
	
	public List<TransitionReplayPrecision> getTransitionReplayPrecisions() {
		return Collections.unmodifiableList(this.transitionReplayPrecisions);
	}
	
	public List<List<String>> getTransitionErrorMessages() {
		return Collections.unmodifiableList(this.transitionErrorMessages);
	}
}
