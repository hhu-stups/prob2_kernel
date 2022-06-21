package de.prob.check.tracereplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.ErrorItem;
import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.ITraceDescription;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

public final class ReplayedTrace implements ITraceDescription {
	private final TraceReplayStatus replayStatus;
	private final List<ErrorItem> errors;
	private final List<PrologTerm> transitionTerms;
	private final List<TransitionReplayPrecision> transitionReplayPrecisions;
	private final List<List<String>> transitionErrorMessages;
	
	public ReplayedTrace(final TraceReplayStatus replayStatus, final List<ErrorItem> errors, final List<PrologTerm> transitionTerms, final List<TransitionReplayPrecision> transitionReplayPrecisions, final List<List<String>> transitionErrorMessages) {
		this.replayStatus = replayStatus;
		this.errors = errors;
		this.transitionTerms = transitionTerms;
		this.transitionReplayPrecisions = transitionReplayPrecisions;
		this.transitionErrorMessages = transitionErrorMessages;
	}
	
	public static ReplayedTrace fromProlog(final PrologTerm replayStatusTerm, final PrologTerm transitionTerms, final PrologTerm matchInfoTerms) {
		final TraceReplayStatus replayStatus = TraceReplayStatus.fromPrologTerm(replayStatusTerm);
		final List<TransitionReplayPrecision> transitionReplayPrecisions = new ArrayList<>();
		final List<List<String>> transitionErrorMessages = new ArrayList<>();
		for (final PrologTerm term : BindingGenerator.getList(matchInfoTerms)) {
			BindingGenerator.getCompoundTerm(term, "replay_step", 2);
			transitionReplayPrecisions.add(TransitionReplayPrecision.fromPrologTerm(term.getArgument(1)));
			
			final List<String> errorMessages = new ArrayList<>();
			for (final PrologTerm errorTerm : BindingGenerator.getList(term.getArgument(2))) {
				if ("rerror".equals(errorTerm.getFunctor()) && errorTerm.getArity() == 1) {
					errorMessages.add(errorTerm.getArgument(1).atomToString());
				} else {
					errorMessages.add(errorTerm.toString());
				}
			}
			transitionErrorMessages.add(Collections.unmodifiableList(errorMessages));
		}
		return new ReplayedTrace(replayStatus, Collections.emptyList(), BindingGenerator.getList(transitionTerms), transitionReplayPrecisions, transitionErrorMessages);
	}
	
	public TraceReplayStatus getReplayStatus() {
		return this.replayStatus;
	}
	
	public List<ErrorItem> getErrors() {
		return Collections.unmodifiableList(this.errors);
	}
	
	public ReplayedTrace withErrors(final List<ErrorItem> errors) {
		return new ReplayedTrace(
			this.getReplayStatus(),
			errors,
			this.transitionTerms,
			this.getTransitionReplayPrecisions(),
			this.getTransitionErrorMessages()
		);
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
