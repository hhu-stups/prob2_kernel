package de.prob.check.tracereplay.interactive;

import de.prob.animator.command.InteractiveTraceReplayFastForwardCommand;
import de.prob.animator.command.InteractiveTraceReplayInitCommand;
import de.prob.animator.command.InteractiveTraceReplayStatusCommand;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.check.tracereplay.TransitionReplayPrecision;
import de.prob.exception.ProBError;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InteractiveTraceReplay {
	private final File traceFile;
	private final StateSpace stateSpace;

	private final List<Transition> currentTransitions = new ArrayList<>(); // can contain manual animation steps
	private final List<ErrorItem> errors = new ArrayList<>();

	private List<InteractiveReplayStep> replaySteps;
	private Transition nextTransition;
	private TransitionReplayPrecision nextTransitionPrecision;
	private List<String> nextTransitionsErrors = new ArrayList<>();
	private int currentStep = 0;

	public InteractiveTraceReplay(File traceFile, StateSpace stateSpace) {
		this.traceFile = traceFile;
		this.stateSpace = stateSpace;
	}

	public void initialise() {
		if (isInitialised())
			return;

		InteractiveTraceReplayInitCommand cmd = new InteractiveTraceReplayInitCommand(traceFile);
		try {
			stateSpace.execute(cmd);
		} catch (ProBError e) {
			if (cmd.getReplaySteps() == null) {
				throw e;
			} else {
				errors.addAll(e.getErrors());
			}
		}
		replaySteps = cmd.getReplaySteps();
		updateStatus();
	}

	public void restart() {
		checkInitialised(false);
		currentStep = 0;
		currentTransitions.clear();
		replaySteps.forEach(InteractiveReplayStep::undo);
		updateStatus();
	}

	public void replayCurrentStep() {
		checkInitialised(true);
		if (nextTransition != null) { // add nextTransition from status command (saves one Prolog call)
			performReplayStep(nextTransition, nextTransitionPrecision, nextTransitionsErrors);
		}
		updateStatus();
	}

	public void fastForward() {
		checkInitialised(true);
		InteractiveTraceReplayFastForwardCommand cmd = new InteractiveTraceReplayFastForwardCommand(currentStep+1, getCurrentStateId());
		stateSpace.execute(cmd);
		List<Transition> transitions = cmd.getTransitionTerms().stream()
				.map(t -> Transition.createTransitionFromCompoundPrologTerm(stateSpace, t))
				.collect(Collectors.toList());
		for (int t = 0; t < transitions.size(); t++) {
			performReplayStep(transitions.get(t), cmd.getTransitionPrecisions().get(t), cmd.getTransitionErrors().get(t));
		}
		updateStatus();
	}

	public void skipCurrentStep() {
		checkInitialised(true);
		getCurrentStep().withTransitionIndex(currentTransitions.size());
		String id = getCurrentStateId();
		currentTransitions.add(Transition.generateArtificialTransition(stateSpace, "skip", "skip", id, id));
		// add artificial skip transition: this is removed in getCurrentTrace()
		currentStep++;
		updateStatus();
	}

	public void undoLastStep() {
		checkInitialised(false);
		int possiblePreviousStep = currentStep-1;
		if (possiblePreviousStep >= 0) {
			int index = getStep(possiblePreviousStep).getTransitionIndex();
			if (index == currentTransitions.size()-1) {
				currentStep--;
				getCurrentStep().undo();
			}
			currentTransitions.remove(currentTransitions.size()-1);
		}
		updateStatus();
	}

	private void performReplayStep(Transition transition, TransitionReplayPrecision precision, List<String> errors) {
		InteractiveReplayStep step = getCurrentStep();
		step.withTransitionIndex(currentTransitions.size());
		step.withPrecision(precision);
		step.withErrors(errors);
		currentTransitions.add(transition);
		currentStep++;
	}

	public void performManualAnimationStep(Transition transition) {
		checkInitialised(false);
		currentTransitions.add(transition);
		updateStatus();
	}

	private void updateStatus() {
		InteractiveTraceReplayStatusCommand statusCmd = new InteractiveTraceReplayStatusCommand(currentStep+1, getCurrentStateId());
		// currentStep on Prolog side is +1!
		stateSpace.execute(statusCmd);
		nextTransition = statusCmd.getOpTerm() != null
				? Transition.createTransitionFromCompoundPrologTerm(stateSpace, statusCmd.getOpTerm())
				: null;
		nextTransitionPrecision = statusCmd.getMatchInfo();
		nextTransitionsErrors = statusCmd.getErrors();
	}

	private void checkInitialised(boolean failOnFinish) {
		if (stateSpace.isKilled()) {
			throw new IllegalStateException("StateSpace for interactive trace replay has been killed.");
		} else if (!isInitialised()) {
			throw new IllegalStateException("Interactive Trace Replay is not initialised for " + traceFile.getAbsolutePath());
		} else if (failOnFinish && isFinished()) {
			throw new IllegalStateException("Interactive Trace Replay is already finished for " + traceFile.getAbsolutePath());
		}
	}

	public File getTraceFile() {
		return traceFile;
	}

	public StateSpace getStateSpace() {
		return stateSpace;
	}

	public boolean isInitialised() {
		return replaySteps != null;
	}

	public boolean isFinished() {
		return currentStep >= replaySteps.size();
	}

	public int getCurrentStepNr() {
		return currentStep;
	}

	public boolean isReplayStepPossible() {
		return nextTransition != null;
	}

	public Transition getNextTransition() {
		return nextTransition;
	}

	public TransitionReplayPrecision getNextTransitionPrecision() {
		return nextTransitionPrecision;
	}

	public List<String> getNextTransitionErrors() {
		return Collections.unmodifiableList(nextTransitionsErrors);
	}

	public Trace getCurrentTrace() {
		List<Transition> filteredTransitions = new ArrayList<>(currentTransitions);
		filteredTransitions.removeIf(t -> t.getId().equals("skip")); // remove skips
		return new Trace(this.stateSpace).addTransitions(filteredTransitions);
	}

	public Transition getStepTransition(int stepNr) {
		int index = getStep(stepNr).getTransitionIndex();
		if (index < 0 || index >= currentTransitions.size()) {
			throw new IllegalArgumentException("step " + stepNr + " not replayed yet");
		}
		return currentTransitions.get(index);
	}

	public List<InteractiveReplayStep> getReplaySteps() {
		return Collections.unmodifiableList(replaySteps);
	}

	public InteractiveReplayStep getStep(int stepNr) {
		if (stepNr < 0 || stepNr >= replaySteps.size()) {
			throw new IllegalArgumentException("stepNr must be between 0 and " + (replaySteps.size()-1) + ", but is " + stepNr);
		}
		return replaySteps.get(stepNr);
	}

	public InteractiveReplayStep getCurrentStep() {
		return getStep(getCurrentStepNr());
	}
	
	public List<ErrorItem> getErrors() {
		return Collections.unmodifiableList(this.errors);
	}

	private String getCurrentStateId() {
		return currentTransitions.isEmpty()
				? stateSpace.getRoot().getId()
				: currentTransitions.get(currentTransitions.size()-1).getDestination().getId();
	}

}
