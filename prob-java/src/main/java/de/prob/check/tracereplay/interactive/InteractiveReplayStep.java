package de.prob.check.tracereplay.interactive;

import de.prob.check.tracereplay.TransitionReplayPrecision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InteractiveReplayStep {

	private final int nr;
	private final String description;
	private final List<String> staticErrors;
	private final List<String> errors = new ArrayList<>();

	private int transitionIndex; // index in currentTrace
	private TransitionReplayPrecision precision;

	public InteractiveReplayStep(final int nr, final String description, final List<String> staticErrors) {
		this.nr = nr;
		this.description = description;
		this.staticErrors = staticErrors;
	}

	public int getNr() {
		return nr;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getErrors() {
		List<String> errorList = new ArrayList<>(this.staticErrors);
		errorList.addAll(this.errors);
		return Collections.unmodifiableList(errorList);
	}

	public void withErrors(List<String> errors) {
		this.errors.addAll(errors);
	}

	public void withTransitionIndex(int transitionIndex) {
		this.transitionIndex = transitionIndex;
	}

	public int getTransitionIndex() {
		return transitionIndex;
	}

	public void withPrecision(TransitionReplayPrecision precision) {
		this.precision = precision;
	}

	public TransitionReplayPrecision getPrecision() {
		return precision;
	}

	public void undo() {
		this.transitionIndex = -1;
		this.precision = null;
		this.errors.clear();
	}

}
