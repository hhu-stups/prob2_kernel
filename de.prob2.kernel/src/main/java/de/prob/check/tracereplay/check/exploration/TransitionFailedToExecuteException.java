package de.prob.check.tracereplay.check.exploration;

import de.prob.check.tracereplay.PersistentTransition;
@Deprecated
public class TransitionFailedToExecuteException extends Throwable {
	PersistentTransition transition;


	@Deprecated
	public TransitionFailedToExecuteException(PersistentTransition transition){
		this.transition = transition;
	}
}
