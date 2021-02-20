package de.prob.check.tracereplay.check.exceptions;

import java.util.List;

public class TransitionNotAccessibleException extends TraceExplorerExceptions {

	public TransitionNotAccessibleException() {
		super();
	}
	
	public TransitionNotAccessibleException(List<String> message) {
		super(message);
	}
}
