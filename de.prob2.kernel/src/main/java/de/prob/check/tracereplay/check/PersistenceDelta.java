package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.Transition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersistenceDelta {


	private final PersistentTransition oldTransition;
	private final List<PersistentTransition> newTransitions;
	private PrivilegeLevel privilegeLevel;


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
