package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;

import java.util.*;
import java.util.stream.Collectors;

public class TraceModifier {


	private final List<List<PersistentTransition>> changelogPhase1 = new LinkedList<>();
	private final Map<Set<Delta>, List<PersistentTransition>> changelogPhase2II = new HashMap<>();


	public TraceModifier(PersistentTrace trace){
		changelogPhase1.add(trace.getTransitionList());
	}


	public void insertMultipleUnambiguousChanges(List<Delta> delta){
		delta.forEach(value -> changelogPhase1.add(changeAll(value, getLastChange())));
	}

	public void insertAmbiguousChanges(Map<String, List<Delta>> typeIICandidates){
		changelogPhase2II.putAll(changeLog(new HashSet<>(typeIICandidates.values()), getLastChange()));
	}


	/**
	 * Takes a delta and applies it to all PersistentTransitions of g given Transistionlist
	 * @param delta the delta to apply
	 * @param currentState the list of transition to apply it to
	 * @return the modified list, a copy of the original
	 */
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



	/**
	 * Calculates all possible permutations of a given delta
	 * @param change a set where each element represents one operation from the original trace. Each list inside represents a collection of possible deltas
	 * @return a list where each element represents a possible permutation
	 */
	public static List<Set<Delta>> deltaPermutation(Set<List<Delta>> change){

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

	/**
	 * Concatenates two collections
	 * @param one the first collection
	 * @param two the second collection
	 * @param <U> the type of the collection
	 * @return the concatenated collection
	 */
	public static <U> List<U> concat(Collection<U> one, Collection<U> two){
		List<U> result = new ArrayList<>();
		result.addAll(one);
		result.addAll(two);
		return result;
	}


	/**
	 * Gets a nested collection of deltas, representing possible overlapping/interfering changes. Calculates all possible
	 * permutations of the applied deltas
	 * @param change a set where each element represents one operation from the original trace. Each list inside represents a collection of possible deltas
	 * @param currentState a list of persistent transitions representing the trace
	 * @return a map containing a mapping between all possible combinations of deltas to their corresponding Transition list
	 */
	public static Map<Set<Delta>, List<PersistentTransition>> changeLog(Set<List<Delta>> change, List<PersistentTransition> currentState){
		if(!change.isEmpty())
		{
			List<Set<Delta>> deltaPermutation = deltaPermutation(change);

			List<List<PersistentTransition>> transformed = deltaPermutation.stream()
					.map(set -> applyMultipleChanges(set , new ArrayList<>(currentState))).collect(Collectors.toList());

			return TraceCheckerUtils.zip(deltaPermutation, transformed);
		}

		return Collections.emptyMap();
	}

	/**
	 * applies multiple deltas to a given transition list
	 * @param deltas a set of changes to be applied
	 * @param currentState the state from where to start
	 * @return a transition list with all changes applied
	 */
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


	/**
	 * Unifies two transition list
	 * Example:
	 * List1:
	 * a - a - b - d
	 * List2:
	 * a - c - d - d
	 * current List
	 * h - a - d - d
	 *
	 * result:
	 * a - c - b - d
	 *
	 * @param list1 the first list
	 * @param list2 the second list
	 * @param currentState the third list - used to detect the change
	 * @return the list with all changes applied
	 */
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

	/**
	 * Searches a list to find if it contains a certain element at the same postion
	 * @param element the element to search for
	 * @param list the list where the element is from
	 * @param searchList the list to search in
	 * @param <U> the type of the element
	 * @return true if the element is contained in both lists on the same position
	 */
	public static <U> boolean hasElementAtPosition(U element, List<U> list, List<U> searchList){
		return list.get(searchList.indexOf(element)).equals(element);
	}


	public List<PersistentTransition> getLastChange() {
		return changelogPhase1.get(changelogPhase1.size()-1);
	}

	/**
	 *
	 * @return the original trace was modified
	 */
	public boolean isDirty(){
		return changelogPhase1.size()==1 && !changelogPhase2II.isEmpty();
	}


	public Map<Set<Delta>, List<PersistentTransition>> getChangelogPhase2II() {
		return changelogPhase2II;
	}


}
