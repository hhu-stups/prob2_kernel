package de.prob.check.tracereplay.check.exploration;

import de.prob.check.tracereplay.PersistentTransition;

public class TransitionHasNoSuccessorException extends TraceExplorerExceptions {
	private static final long serialVersionUID = 1L;
	
	PersistentTransition transition;
	

	public TransitionHasNoSuccessorException(PersistentTransition transition){
		this.transition = transition;
	}
}
