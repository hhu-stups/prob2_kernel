package de.prob.check;

import de.prob.statespace.ITraceDescription;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

public class ModelCheckGoalFound implements IModelCheckingResult,
		ITraceDescription {

	private final String message;
	private final String stateID;

	public ModelCheckGoalFound(final String message, final String stateID) {
		this.message = message;
		this.stateID = stateID;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public String getStateID() {
		return stateID;
	}

	@Override
	public Trace getTrace(final StateSpace s) {
		return s.getTrace(stateID);
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
