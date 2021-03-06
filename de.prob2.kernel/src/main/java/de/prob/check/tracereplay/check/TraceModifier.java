package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class TraceModifier {

	private final List<PersistentTransition> orginal;
	private final List<List<PersistentTransition>> changelogPhase1 = new LinkedList<>();
	private final Map<Set<RenamingDelta>, List<PersistentTransition>> changelogPhase2 = new HashMap<>();
	private final Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> changelogPhase3 = new HashMap<>();
	private final Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, Map<String, TraceAnalyser.AnalyserResult>>> changelogPhase4 = new HashMap<>();
	private final StateSpace stateSpace;
	private final ProgressMemoryInterface progressMemoryInterface;


	public TraceModifier(List<PersistentTransition> transitionList, StateSpace stateSpace, ProgressMemoryInterface progressMemoryInterface) {
		orginal = transitionList;
		changelogPhase1.add(transitionList);
		this.stateSpace = stateSpace;
		this.progressMemoryInterface = progressMemoryInterface;
	}

	/**
	 * Takes a renamingDelta and applies it to all PersistentTransitions of g given Transition list
	 *
	 * @param renamingDelta        the renamingDelta to apply
	 * @param currentState the list of transition to apply it to
	 * @return the modified list, a copy of the original
	 */
	public static List<PersistentTransition> changeAll(RenamingDelta renamingDelta, List<PersistentTransition> currentState) {
		return currentState.stream().map(persistentTransition -> {
			if (persistentTransition.getOperationName().equals(renamingDelta.originalName)) {

				Map<String, String> newDestState = persistentTransition.getDestinationStateVariables().entrySet().stream()
						.collect(Collectors.toMap(entry -> renamingDelta.variables.getOrDefault(entry.getKey(), entry.getKey()), Map.Entry::getValue));

				Map<String, String> newParameters = persistentTransition.getParameters().entrySet().stream()
						.collect(Collectors.toMap(entry -> renamingDelta.inputParameters.getOrDefault(entry.getKey(), entry.getKey()), Map.Entry::getValue));


				Map<String, String> newOutputParameters = persistentTransition.getOutputParameters().entrySet().stream()
						.collect(Collectors.toMap(entry -> renamingDelta.outputParameters.getOrDefault(entry.getKey(), entry.getKey()), Map.Entry::getValue));


				Set<String> destStateNotChanged = persistentTransition.getDestStateNotChanged().stream()
						.map(entry -> renamingDelta.variables.getOrDefault(entry, entry)).collect(toSet());

				return new PersistentTransition(renamingDelta.deltaName, newParameters, newOutputParameters, newDestState,
						destStateNotChanged, Collections.emptyList());
			} else {
				return persistentTransition;
			}

		}).collect(toList());
	}

	/**
	 * Calculates all possible permutations of a given delta
	 *
	 * @param change a set where each element represents one operation from the original trace. Each list inside represents a collection of possible deltas
	 * @return a list where each element represents a possible permutation
	 */
	public static List<Set<RenamingDelta>> deltaPermutation(Set<List<RenamingDelta>> change) {

		List<List<Set<RenamingDelta>>> bla = change.stream().map(set -> set.stream().map(delta -> {
			Set<RenamingDelta> renamingDeltaList = new HashSet<>();
			renamingDeltaList.add(delta);
			return renamingDeltaList;
		}).collect(toList())).collect(toList());

		return bla.stream().reduce(Collections.emptyList(), (acc, current) -> {
					if (acc.isEmpty()) return current;

					if (current.isEmpty()) return acc;

					return current.stream()
							.flatMap(delta1 -> acc.stream()
									.map(delta2 -> new HashSet<>(concat(delta1, delta2)))).collect(toList());
				}
		);
	}



	/**
	 * Concatenates two collections
	 *
	 * @param one the first collection
	 * @param two the second collection
	 * @param <U> the type of the collection
	 * @return the concatenated collection
	 */
	public static <U> List<U> concat(Collection<U> one, Collection<U> two) {
		List<U> result = new ArrayList<>();
		result.addAll(one);
		result.addAll(two);
		return result;
	}

	/**
	 * Gets a nested collection of deltas, representing possible overlapping/interfering changes. Calculates all possible
	 * permutations of the applied deltas
	 *
	 * @param change       a set where each element represents one operation from the original trace. Each list inside represents a collection of possible deltas
	 * @param currentState a list of persistent transitions representing the trace
	 * @return a map containing a mapping between all possible combinations of deltas to their corresponding Transition list
	 */
	public static Map<Set<RenamingDelta>, List<PersistentTransition>> changeLog(Set<List<RenamingDelta>> change, List<PersistentTransition> currentState) {
		if (!change.isEmpty()) {
			List<Set<RenamingDelta>> deltaPermutation = deltaPermutation(change);

			List<List<PersistentTransition>> transformed = deltaPermutation.stream()
					.map(set -> applyMultipleChanges(set, new ArrayList<>(currentState))).collect(toList());

			return TraceCheckerUtils.zip(deltaPermutation, transformed);
		}

		return emptyMap();
	}

	/**
	 * applies multiple renamingDeltas to a given transition list
	 *
	 * @param renamingDeltas       a set of changes to be applied
	 * @param currentState the state from where to start
	 * @return a transition list with all changes applied
	 */
	public static List<PersistentTransition> applyMultipleChanges(Set<RenamingDelta> renamingDeltas, List<PersistentTransition> currentState) {

		if (renamingDeltas.isEmpty()) return currentState;

		return renamingDeltas.stream()
				.map(delta -> changeAll(delta, new ArrayList<>(currentState)))
				.reduce(Collections.emptyList(), (acc, current) -> {
					if (acc.isEmpty()) return current;
					if (current.isEmpty()) return acc;
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
	 * <p>
	 * result:
	 * a - c - b - d
	 *
	 * @param list1        the first list
	 * @param list2        the second list
	 * @param currentState the third list - used to detect the change
	 * @return the list with all changes applied
	 */
	public static List<PersistentTransition> unifyTransitionList(List<PersistentTransition> list1,
																 List<PersistentTransition> list2,
																 List<PersistentTransition> currentState) {
		return currentState.stream().map(persistentTransition -> {
			if (hasElementAtPosition(persistentTransition, list1, currentState) && !hasElementAtPosition(persistentTransition, list2, currentState)) {
				return list2.get(currentState.indexOf(persistentTransition));
			} else if (!hasElementAtPosition(persistentTransition, list1, currentState) && hasElementAtPosition(persistentTransition, list2, currentState)) {
				return list1.get(currentState.indexOf(persistentTransition));
			} else {
				return persistentTransition;
			}
		}).collect(toList());
	}

	/**
	 * Searches a list to find if it contains a certain element at the same position
	 *
	 * @param element    the element to search for
	 * @param list       the list where the element is from
	 * @param searchList the list to search in
	 * @param <U>        the type of the element
	 * @return true if the element is contained in both lists on the same position
	 */
	public static <U> boolean hasElementAtPosition(U element, List<U> list, List<U> searchList) {
		return list.get(searchList.indexOf(element)).equals(element);
	}

	public void insertMultipleUnambiguousChanges(List<RenamingDelta> renamingDelta) {
		renamingDelta.forEach(value -> changelogPhase1.add(changeAll(value, getLastChange())));
	}

	/**
	 * Inserts changes with multiple choices. Ensures the data structure is filled at least with an empty element
	 * @param typeIICandidates the found deltas to insert
	 */
	public void insertAmbiguousChanges(Map<String, List<RenamingDelta>> typeIICandidates) {
		if(typeIICandidates.isEmpty()){
			changelogPhase2.put(emptySet(), getLastChange());
		}else {
			changelogPhase2.putAll(changeLog(new HashSet<>(typeIICandidates.values()), getLastChange()));
		}
	}


	/**
	 * Manages the exploration and analyzes phase and storing it results, produces results for type III and type IV
	 * @param typeIIICandidates the candidate assumed to be type III
	 * @param typeIVCandidates the candidates assumed to be type IV
	 * @param newInfos new operation infos
	 * @param oldInfos old operations infos
	 * @param traceExplorer the trace explorer
	 */
	public void makeTypeIII(Set<String> typeIIICandidates, Set<String> typeIVCandidates,
							Map<String, OperationInfo> newInfos, Map<String, OperationInfo> oldInfos, TraceExplorer traceExplorer) {


			Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> results = changelogPhase2
					.entrySet()
					.stream()
					.map(entry ->
					{
						Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result =
								traceExplorer.replayTrace(entry.getValue(), stateSpace, newInfos, oldInfos, typeIIICandidates, typeIVCandidates);
						return new AbstractMap.SimpleEntry<>(entry.getKey(), result);
					})
					.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


			changelogPhase3.putAll(results);
			progressMemoryInterface.nextStep();
			Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, Map<String, TraceAnalyser.AnalyserResult>>> typeIVResults =
					performTypeIVAnalysing(traceExplorer.getUpdatedTypeIV(), results);
			progressMemoryInterface.nextStep();
			changelogPhase4.putAll(typeIVResults);


	}

	/**
	 * Helper to perform type IV analyzes for all candidates
	 * @param typeIVCandidates the candidates to be type IV
	 * @param results the results of the trace explorer
	 * @return the analysis for each type IV under the impression of the explored traces
	 */
	public Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, Map<String, TraceAnalyser.AnalyserResult>>> performTypeIVAnalysing(
			Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, Set<String>> typeIVCandidates,
			Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> results){

		return results.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().entrySet()
						.stream()
						.collect(toMap(Map.Entry::getKey,
								innerEntry -> TraceAnalyser.analyze(typeIVCandidates.get(innerEntry.getKey()), innerEntry.getValue(), changelogPhase2.get(entry.getKey()))))));
	}



	public List<PersistentTransition> getLastChange() {
		return changelogPhase1.get(changelogPhase1.size() - 1);
	}



	public Map<Set<RenamingDelta>, List<PersistentTransition>> getChangelogPhase2() {
		return changelogPhase2;
	}

	public Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> getChangelogPhase3II() {
		return changelogPhase3;
	}

	public Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, Map<String, TraceAnalyser.AnalyserResult>>> getChangelogPhase4() {
		return changelogPhase4;
	}



	public boolean isDirty(){
		return typeIIIDirty() || typeIINonDetDirty() || typeIVDirty() || typeIIDetDirty();
	}

	public boolean typeIIDetDirty() {
		return changelogPhase1.size() > 1;
	}

	public boolean typeIINonDetDirty(){
		return changelogPhase2.keySet().stream().mapToLong(Collection::size).sum() > 0;
	}

	public boolean typeIIIDirty(){
		return changelogPhase3.values().stream().flatMap(entry -> entry.keySet().stream().filter(innerEntry -> !innerEntry.isEmpty())).count() > 0;
	}

	public boolean typeIVDirty(){
		return changelogPhase4.values().stream().flatMap(entry -> entry.values().stream().flatMap(innerEntry -> innerEntry.entrySet().stream())).count() > 0;
	}


	public long changedTracesTypeIIDet(){
		return changelogPhase1.size() - 1;
	}

	public long changedTracesTypeIINonDet(){
		if(typeIINonDetDirty()){
			return changelogPhase2.keySet().stream().mapToLong(Collection::size).sum();
		}else{
			return 0;
		}
	}

	public long changedTracesTypeIII(){
		if(typeIIIDirty()){
			return changelogPhase3.values().stream().flatMap(entry -> entry.keySet().stream().filter(innerEntry -> !innerEntry.isEmpty())).count();
		}else{
			return 0;
		}
	}

	public long changedTracesTypeIV(){
		if(typeIVDirty()){
			return changelogPhase4.values().stream().flatMap(entry -> entry.values().stream().flatMap(innerEntry -> innerEntry.entrySet().stream())).count();
		}else{
			return 0;
		}
	}

	public long tracesStoredInTypeIII(){
		return changelogPhase3.values().stream().mapToLong(entry -> entry.values().size()).sum();
	}


	public boolean tracingFoundResult(){
		return tracesStoredInTypeIII() > 0;
	}

	public boolean traceMatchesExactly(){
		return !isDirty() && tracesStoredInTypeIII() == 1;
	}
}
