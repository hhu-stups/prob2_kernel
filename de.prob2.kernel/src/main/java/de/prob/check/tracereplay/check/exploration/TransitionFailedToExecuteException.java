package de.prob.check.tracereplay.check.exploration;

import de.prob.check.tracereplay.PersistentTransition;

public class TransitionFailedToExecuteException extends Throwable {
	PersistentTransition transition;


	public TransitionFailedToExecuteException(PersistentTransition transition){
		this.transition = transition;
	}
}
