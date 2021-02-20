package de.prob.check.tracereplay.check.exceptions;

import de.prob.check.tracereplay.PersistentTransition;

import java.util.List;

public class TransitionHasNoSuccessorException extends TraceExplorerExceptions {
	
	
	PersistentTransition transition;
	

	public TransitionHasNoSuccessorException(PersistentTransition transition){
		this.transition = transition;
	}
}
