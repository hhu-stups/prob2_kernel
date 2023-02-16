package de.prob.check.tracereplay.check.exploration;

import de.prob.check.tracereplay.PersistentTransition;
@Deprecated
public class TransitionHasNoSuccessorException extends TraceExplorerExceptions {
	private static final long serialVersionUID = 1L;
	
	PersistentTransition transition;
	

	@Deprecated
	public TransitionHasNoSuccessorException(PersistentTransition transition){
		this.transition = transition;
	}
}
