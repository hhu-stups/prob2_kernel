package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.Trace;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gets the old transition list and tries to find a operations to represent the the trace in regard to the current machine
 */
public class TransitionChecker {


	final List<PersistentTransition> oldTrace;
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
	public TransitionChecker(List<PersistentTransition> oldTrace,
							 LoadedMachine currentMachine,
							 List<String> oldVariableNames,
							 List<String> oldConstantNames,
							 List<String> oldSetNames,
							 Map<String, OperationInfo>oldMachineOperationInfos){
		this.oldTrace = oldTrace;
		this.currentMachine = currentMachine;
		this.oldMachineOperationInfos = oldMachineOperationInfos;

	}


	/**
	 * Currently just resolving equal name conflicts for operations
	 * @return the modified trace
	 */
	List<PersistentTransition> findProblems(){
		Set<Conflict> conflicts = transitionListContainsOperationsByName(currentMachine.getOperationNames(), oldTrace);
		Map<String, String> resolvedConflicts = resolveOperationRenamingConflict(conflicts, oldMachineOperationInfos, currentMachine.getOperations());

		return oldTrace.stream().map(persistentTransition -> {
			String newOperationName = resolvedConflicts.get(persistentTransition.getOperationName());
			return new PersistentTransition(newOperationName, persistentTransition.getParameters(),
					persistentTransition.getResults(),
					persistentTransition.getDestinationStateVariables(),
					persistentTransition.getDestStateNotChanged(),
					persistentTransition.getPreds());
		}).collect(Collectors.toList());

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
					return equals(oldOperation, currentOperation);
					})
					.map(Map.Entry::getKey).collect(Collectors.toSet());

			//TODO in future use are more ... elaborated way to calculate the target

			return new AbstractMap.SimpleEntry<>(oldOperation.getOperationName(), new ArrayList<>(candidates).get(0));
		}).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
		
	}

	public <T> List<T> getFirstElementOrEmptyList(List<T> list){
		if(list.size()>0){
			return Collections.singletonList(list.get(0));
		}else{
			return new ArrayList<>();
		}
	}

	/**
	 * compares if two OperationInfos are the same expect for their name
	 * @param old the operationInfo form the file
	 * @param currentObject the current operationInfo
	 * @return true if equal
	 */
	public boolean equals(OperationInfo old, OperationInfo currentObject){
		boolean equalNonDetWrittenVariables = listComparator(old.getNonDetWrittenVariables(), currentObject.getNonDetWrittenVariables());
		boolean outputParameterNames = listComparator(old.getOutputParameterNames(), currentObject.getOutputParameterNames());
		boolean parameterNames = listComparator(old.getParameterNames(), currentObject.getParameterNames());
		boolean writtenVariables = listComparator(old.getWrittenVariables(), currentObject.getWrittenVariables());
		boolean readVariables = listComparator(old.getReadVariables(), currentObject.getReadVariables());

		return  equalNonDetWrittenVariables && outputParameterNames && parameterNames && writtenVariables && readVariables;
	}

	/**
	 * Return true if two list contain equal elements
	 * @param a the first list
	 * @param b the second list
	 * @return the result of the comparison
	 */
	public boolean listComparator(List<String> a, List<String> b){
		if(a.size() == b.size()){
			for(int i = 0; i < a.size(); i++){
				if(!a.get(i).equals(b.get(i))){
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
