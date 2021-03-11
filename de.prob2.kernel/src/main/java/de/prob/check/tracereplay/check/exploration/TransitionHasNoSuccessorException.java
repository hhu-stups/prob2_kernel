package de.prob.check.tracereplay.check.exploration;

import de.prob.check.tracereplay.PersistentTransition;

public class TransitionHasNoSuccessorException extends TraceExplorerExceptions {
	
	
	PersistentTransition transition;
	

	public TransitionHasNoSuccessorException(PersistentTransition transition){
		this.transition = transition;
	}
}
