package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TraceModifier {


	private final List<List<PersistentTransition>> changelogPhase1 = new LinkedList<>();
	private final List<List<List<PersistentTransition>>> changelogPhase2 = new LinkedList<>();
	private Map<Set<Delta>, List<PersistentTransition>> changelogPhase2II = new HashMap<>();
	//Changelog phase 3 and 4


	public TraceModifier(PersistentTrace trace){
		changelogPhase1.add(trace.getTransitionList());
	}


	public void insertMultipleUnambiguousChanges(List<Delta> delta){
		delta.forEach(value -> changelogPhase1.add(changeAll(value, getLastChange())));
	}


	public void insertMultipleAmbiguousChanges(Map<String, List<Delta>> delta){
		changelogPhase2.add(changeAmbiguous(delta, getLastChange()));
	}


	public static List<PersistentTransition> changeAll(Delta delta, List<PersistentTransition> currentState){
		return currentState.stream().map(persistentTransition -> {
			if(persistentTransition.getOperationName().equals(delta.originalName)){

				Map<String, String> newDestState = persistentTransition.getDestinationStateVariables().entrySet().stream()
						.collect(Collectors.toMap(entry -> delta.variables.getOrDefault(entry.getKey(), entry.getKey()), Map.Entry::getValue));

				Map<String, String> newParameters = persistentTransition.getParameters().entrySet().stream()
						.collect(Collectors.toMap(entry -> delta.inputParameters.getOrDefault(entry.getKey(), entry.getKey()), Map.Entry::getValue));


				Map<String, String> newOutputParameters = persistentTransition.getOutputParameters().entrySet().stream()
						.collect(Collectors.toMap(entry -> delta.outputParameters.getOrDefault(entry.getKey(), entry.getKey()), Map.Entry::getValue));


				Set<String> destStateNotChanged = persistentTransition.getDestStateNotChanged().stream()
						.map(entry -> delta.variables.getOrDefault(entry, entry)).collect(Collectors.toSet());

				return new PersistentTransition(delta.deltaName, newParameters, newOutputParameters, newDestState,
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

		List<List<List<PersistentTransition>>> transformedDelta = change.values().stream()
				.map(deltas -> deltas.stream().map(value -> changeAll(value, currentState))
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


	public static List<Set<Delta>> deltaPermutation(List<List<Delta>> change){

		List<List<Set<Delta>>> bla = change.stream().map(set -> set.stream().map(delta -> {
			Set<Delta> deltaList = new HashSet<>();
			deltaList.add(delta);
			return deltaList;
		}).collect(Collectors.toList())).collect(Collectors.toList());

		return bla.stream().reduce(Collections.emptyList(), (acc, current) -> {
			if(acc.isEmpty()) return current;

			if(current.isEmpty()) return acc;

			return current.stream()
					.flatMap(delta1 -> acc.stream()
							.map(delta2 -> new HashSet<>(concat(delta1, delta2)))).collect(Collectors.toList());}
		);
	}

	public static <U> List<U> concat(Collection<U> one, Collection<U> two){
		List<U> result = new ArrayList<>();
		result.addAll(one);
		result.addAll(two);
		return result;
	}

	public void setChangelogPhase2II(Set<List<Delta>> change){
		changelogPhase2II = changeLog(change, getLastChange());
	}

	public static Map<Set<Delta>, List<PersistentTransition>> changeLog(Set<List<Delta>> change, List<PersistentTransition> currentState){
		if(!change.isEmpty())
		{
			List<Set<Delta>> deltaPermutation = deltaPermutation(new ArrayList<>(change));

			List<List<PersistentTransition>> transformed = deltaPermutation.stream()
					.map(set -> applyMultipleChanges(set , new ArrayList<>(currentState))).collect(Collectors.toList());

			return zip(deltaPermutation, transformed);
		}

		return Collections.emptyMap();
	}


	public static <T, U> Map<T, U> zip(List<T> list1, List<U> list2){
		if(list1.size() == list2.size()){
			Map<T, U> sideResult = new HashMap<>();
			for(int i = 0;  i < list1.size(); i++){
				sideResult.put(list1.get(i), list2.get(i));
			}
			return sideResult;
		}
		return Collections.emptyMap();
	}


	public static List<PersistentTransition> applyMultipleChanges(Set<Delta> deltas, List<PersistentTransition> currentState){

		if(deltas.isEmpty()) return currentState;

		return deltas.stream()
				.map(delta -> changeAll(delta, new ArrayList<>(currentState)))
				.reduce(Collections.emptyList(), (acc, current)-> {
					if(acc.isEmpty()) return current;
					if(current.isEmpty()) return acc;
					return unifyTransitionList(acc, current, new ArrayList<>(currentState));
				});
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
		return changelogPhase1.size()==1 && !changelogPhase2.isEmpty() && !changelogPhase2II.isEmpty();
	}


	public Map<Set<Delta>, List<PersistentTransition>> getChangelogPhase2II() {
		return changelogPhase2II;
	}


}
