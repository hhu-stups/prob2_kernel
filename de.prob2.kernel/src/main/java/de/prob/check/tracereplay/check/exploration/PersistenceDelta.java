package de.prob.check.tracereplay.check.exploration;

import de.prob.check.tracereplay.PersistentTransition;

import java.util.List;

public class PersistenceDelta {


	private final PersistentTransition oldTransition;
	private final List<PersistentTransition> newTransitions;


	@Deprecated
	public PersistenceDelta(PersistentTransition oldTransition, List<PersistentTransition> newTransitions){
		this.oldTransition = oldTransition;
		this.newTransitions = newTransitions;
	}

	public PersistentTransition getOldTransition() {
		return oldTransition;
	}

	public List<PersistentTransition> getNewTransitions() {
		return newTransitions;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PersistenceDelta){
			return oldTransition.equals(((PersistenceDelta) obj).oldTransition) && newTransitions.equals(((PersistenceDelta) obj).newTransitions);
		}
		return false;
	}

	@Override
	public String toString() {
		return "{" +oldTransition + " : " + newTransitions + "}";
	}


	public PersistentTransition getLast(){
		return newTransitions.get(newTransitions.size()-1);
	}
}
