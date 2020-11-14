package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;

import java.util.*;
import java.util.stream.Collectors;

public class TraceModifier {


	private final List<List<PersistentTransition>> changelogPhase1 = new LinkedList<>();
	private final List<List<List<PersistentTransition>>> changelogPhase2 = new LinkedList<>();
	//Changelog phase 3 and 4


	public TraceModifier(PersistentTrace trace){
		changelogPhase1.add(trace.getTransitionList());
	}


	public void insertMultipleUnambiguousChanges(Map<String, Delta> delta){
		delta.forEach((key, value) -> changelogPhase1.add(changeAll(key, value, getLastChange())));
	}


	public void insertMultipleAmbiguousChanges(Map<String, List<Delta>> delta){
		changelogPhase2.add(changeAmbiguous(delta, getLastChange()));
	}


	public static List<PersistentTransition> changeAll(String operationName, Delta delta, List<PersistentTransition> currentState){
		return currentState.stream().map(persistentTransition -> {
			if(persistentTransition.getOperationName().equals(operationName)){

				Map<String, String> newDestState = persistentTransition.getDestinationStateVariables().entrySet().stream()
						.collect(Collectors.toMap(entry -> delta.variables.getOrDefault(entry.getKey(), entry.getKey()), Map.Entry::getValue));

				Map<String, String> newParameters = persistentTransition.getParameters().entrySet().stream()
						.collect(Collectors.toMap(entry -> delta.inputParameters.getOrDefault(entry.getKey(), entry.getKey()), Map.Entry::getValue));


				Map<String, String> newOutputParameters = persistentTransition.getOutputParameters().entrySet().stream()
						.collect(Collectors.toMap(entry -> delta.outputParameters.getOrDefault(entry.getKey(), entry.getKey()), Map.Entry::getValue));


				Set<String> destStateNotChanged = persistentTransition.getDestStateNotChanged().stream()
						.map(entry -> delta.variables.getOrDefault(entry, entry)).collect(Collectors.toSet());

				return new PersistentTransition(delta.operationName, newParameters, newOutputParameters, newDestState,
						destStateNotChanged, Collections.emptyList());
			}else{
				return persistentTransition;
			}

		}).collect(Collectors.toList());
	}


	public static List<List<PersistentTransition>> changeAmbiguous(Map<String, List<Delta>> change, List<PersistentTransition> currentState){

		if(change.isEmpty()){
			return Collections.singletonList(currentState);
		}

		List<List<List<PersistentTransition>>> transformedDelta = change.entrySet().stream().map(entry->
			entry.getValue().stream().map(value -> changeAll(entry.getKey(), value, currentState))
					.collect(Collectors.toList()))
				.collect(Collectors.toList());

		if(transformedDelta.size()==1){
			return transformedDelta.get(0);
		}else{
			 List<List<PersistentTransition>> first = transformedDelta.get(0);
			 transformedDelta.remove(first);
			 return transformedDelta.stream().reduce(first, (given, actual) ->
					given.stream().flatMap(transitionList ->
							actual.stream().map(transitionList1 -> unifyTransitionList(transitionList, transitionList1, currentState)))
							.collect(Collectors.toList()));
		}


	}



	public static List<PersistentTransition> unifyTransitionList(List<PersistentTransition> list1,
																 List<PersistentTransition> list2,
																 List<PersistentTransition> currentState){
		return currentState.stream().map(persistentTransition -> {
			if(hasElementAtPosition(persistentTransition, list1, currentState)&& !hasElementAtPosition(persistentTransition, list2, currentState)){
				return list2.get(currentState.indexOf(persistentTransition));
			}else if(!hasElementAtPosition(persistentTransition, list1, currentState)&& hasElementAtPosition(persistentTransition, list2, currentState)){
				return list1.get(currentState.indexOf(persistentTransition));
			}else{
				return persistentTransition;
			}
		}).collect(Collectors.toList());
	}

	public static <U> boolean hasElementAtPosition(U element, List<U> list, List<U> searchList){
		return list.get(searchList.indexOf(element)).equals(element);
	}


	public List<PersistentTransition> getLastChange() {
		return changelogPhase1.get(changelogPhase1.size()-1);
	}

	public boolean isDirty(){
		return changelogPhase1.size()==1 && !changelogPhase2.isEmpty();
	}

}
