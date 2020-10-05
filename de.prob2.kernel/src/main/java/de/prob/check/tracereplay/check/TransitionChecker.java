package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.Trace;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gets the old transition list and tries to find a operations to represent the the trace in regard to the current machine
 */
public class TransitionChecker {


	final List<PersistentTransition> oldTrace;
	final String description;
	final LoadedMachine currentMachine;
	final Map<String, OperationInfo>oldMachineOperationInfos;

	/**
	 *
	 * @param oldTrace the old trace (loaded from file)
	 * @param currentMachine the current selected machine
	 * @param oldVariableNames the old variables (from file)
	 * @param oldConstantNames the old constants (from file)
	 * @param oldSetNames the old set (from file)
	 * @param oldMachineOperationInfos the machine operation details (from file)
	 */
	public TransitionChecker(PersistentTrace oldTrace,
							 LoadedMachine currentMachine,
							 List<String> oldVariableNames,
							 List<String> oldConstantNames,
							 List<String> oldSetNames,
							 Map<String, OperationInfo>oldMachineOperationInfos){
		this.oldTrace = oldTrace.getTransitionList();
		this.description = oldTrace.getDescription();
		this.currentMachine = currentMachine;
		this.oldMachineOperationInfos = oldMachineOperationInfos;

	}


	/**
	 * Currently just resolving equal name conflicts for operations
	 * @return the modified trace
	 */
	public PersistentTrace resolveConflicts(){

		//Hardocded $initialize_machine to prevent things from getting into trouble
		Set<String> currentOperationNames = currentMachine.getOperationNames();
		currentOperationNames.remove("$initialise_machine");

		Set<Conflict> conflicts = transitionListContainsOperationsByName(currentOperationNames, oldTrace);
		Map<String, String> resolvedConflicts = resolveOperationRenamingConflict(conflicts, oldMachineOperationInfos, currentMachine.getOperations());

		List<PersistentTransition> newTransitions = oldTrace.stream().map(persistentTransition -> {
			String newOperationName = resolvedConflicts.get(persistentTransition.getOperationName());
			return new PersistentTransition(newOperationName, persistentTransition.getParameters(),
					persistentTransition.getResults(),
					persistentTransition.getDestinationStateVariables(),
					persistentTransition.getDestStateNotChanged(),
					persistentTransition.getPreds());
		}).collect(Collectors.toList());

		return new PersistentTrace(description, newTransitions);
	}


	/**
	 * Finds naming conflicts by searching the old transition list with the new operation names
	 * @param operationNames the names of operations in the current machine
	 * @param oldTrace the loaded traces
	 * @return all naming conflicts found
	 */
	Set<Conflict> transitionListContainsOperationsByName(Set<String> operationNames, List<PersistentTransition> oldTrace){
		return oldTrace.stream().map(persistentTransition -> {
			String operationName = persistentTransition.getOperationName();
			if(!operationNames.contains(operationName)){
				return new Conflict(Conflict.Type.Renamed, operationName);
			}else{
				return new Conflict(Conflict.Type.NoConflict, operationName);
			}
		}).filter(conflict -> conflict.type != Conflict.Type.NoConflict).collect(Collectors.toSet());
	}


	/**
	 * purposes a solution for each conflict by finding an equivalent operation doing the same thing
	 * @param conflicts the conflicts are solution is needed for
	 * @param oldMachineOperationInfos  OperationInfos from file
	 * @param currentMachineOperationInfo operationInfos current machine
	 * @return proposed solutions for the conflicts
	 */
	Map<String, String> resolveOperationRenamingConflict(Set<Conflict> conflicts, Map<String, OperationInfo>oldMachineOperationInfos,
										  Map<String, OperationInfo>currentMachineOperationInfo){
		
		return conflicts.stream().map(conflict -> {

			OperationInfo oldOperation = oldMachineOperationInfos.get(conflict.oldName);

			Set<String> candidates = currentMachineOperationInfo.entrySet().stream().filter(
					stringOperationInfoEntry -> {
					OperationInfo currentOperation = stringOperationInfoEntry.getValue();
					return CheckerUtils.equals(oldOperation, currentOperation);
					})
					.map(Map.Entry::getKey).collect(Collectors.toSet());

			//TODO in future use are more ... elaborated way to calculate the target

			return new AbstractMap.SimpleEntry<>(oldOperation.getOperationName(), new ArrayList<>(candidates).get(0));
		}).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
		
	}
}
