package de.prob.statespace;

import java.util.HashMap;
import java.util.Set;

import de.prob.animator.command.ExploreStateCommand;
import de.prob.animator.domainobjects.EvaluationResult;

/**
 * Contains the information about the StateSpace. This includes the Operations
 * for a given OpInfo and the variables, invariant, timeouts, and operations
 * with timeout for a given StateId.
 * 
 * @author joy
 * 
 */
public class StateSpaceInfo {
	private final HashMap<StateId, HashMap<String, String>> variables = new HashMap<StateId, HashMap<String, String>>();
	private final HashMap<StateId, Boolean> invariantOk = new HashMap<StateId, Boolean>();
	private final HashMap<StateId, Boolean> timeoutOccured = new HashMap<StateId, Boolean>();
	private final HashMap<StateId, Set<String>> operationsWithTimeout = new HashMap<StateId, Set<String>>();

	/**
	 * Records the map of variables (vars) corresponding to the given state id.
	 * 
	 * @param id
	 * @param vars
	 */
	public void add(final StateId id, final HashMap<String, String> vars) {
		HashMap<String, String> hashMap = variables.get(id);
		if (hashMap == null) {
			hashMap = new HashMap<String, String>();
		}
		hashMap.putAll(vars);
		variables.put(id, hashMap);
	}

	public void add(final StateId id, final String var,
			final EvaluationResult val) {
		HashMap<String, String> hashMap = variables.get(id);
		if (hashMap == null) {
			hashMap = new HashMap<String, String>();
		}
		hashMap.put(var, val.value);
		variables.put(id, hashMap);

	}

	/**
	 * Records if the invariant (invOK) at a given state id is ok.
	 * 
	 * @param id
	 * @param invOK
	 */
	public void addInvOk(final StateId id, final Boolean invOK) {
		invariantOk.put(id, invOK);
	}

	/**
	 * Records if a timeout occurred (tOccured) for the given id
	 * 
	 * @param id
	 * @param tOccured
	 */
	public void addTimeOcc(final StateId id, final Boolean tOccured) {
		// FIXME is this id a state id or an operation id
		timeoutOccured.put(id, tOccured);
	}

	/**
	 * Records the operations with timeout (opsWT) that correspond to the given
	 * id
	 * 
	 * @param id
	 * @param opsWT
	 */
	public void add(final StateId id, final Set<String> opsWT) {
		operationsWithTimeout.put(id, opsWT);
	}

	/**
	 * Takes a processed ExploreStateCommand and extracts the values for the
	 * variables, invariantOk, timeoutOccured, and the operationsWithTimeout
	 * 
	 * @param stateId
	 * @param command
	 */
	public void add(final StateId stateId, final ExploreStateCommand command) {
		variables.put(stateId, command.getVariables());
		invariantOk.put(stateId, command.isInvariantOk());
		timeoutOccured.put(stateId, command.isTimeoutOccured());
		operationsWithTimeout.put(stateId, command.getOperationsWithTimeout());
	}

	public HashMap<StateId, Boolean> getInvariantOk() {
		return invariantOk;
	}

	public HashMap<StateId, Boolean> getTimeoutOccured() {
		return timeoutOccured;
	}

	public HashMap<StateId, Set<String>> getOperationsWithTimeout() {
		return operationsWithTimeout;
	}

	/**
	 * Returns the map representation of the variables for the given state id
	 * 
	 * @param stateId
	 * @return returns the variables at the given state
	 */
	public HashMap<String, String> getState(final StateId stateId) {
		return variables.get(stateId);
	}

	// public HashMap<String, String> getState(final int stateId) {
	// String id = String.valueOf(stateId);
	// return getState(id);
	// }

	/**
	 * The value of the variable at the given state is found and returned.
	 * 
	 * @param stateId
	 * @param variable
	 * @return gets the value of the variable for the given state
	 */
	public String getVariable(final StateId stateId, final String variable) {
		return variables.get(stateId).get(variable);
	}

	/**
	 * Returns if the given state has a specified variable
	 * 
	 * @param stateId
	 * @param variable
	 * @return if the given state has a variable with name variable
	 */
	public boolean stateHasVariable(final StateId stateId, final String variable) {
		HashMap<String, String> state = getState(stateId);
		return state.containsKey(variable);
	}

	@Override
	public String toString() {
		String result = "";
		result += "Variables: \n  " + variables.toString() + "\n";
		result += "Invariants Ok: \n  " + invariantOk.toString() + "\n";
		result += "Timeout Occured: \n  " + timeoutOccured.toString() + "\n";
		result += "Operations With Timeout: \n  "
				+ operationsWithTimeout.toString() + "\n";
		return result;
	}

}
