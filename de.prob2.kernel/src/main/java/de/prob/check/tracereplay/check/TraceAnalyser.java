package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exploration.PersistenceDelta;
import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.check.tracereplay.check.renamig.RenamingDelta;

import java.util.*;
import java.util.stream.Collectors;

import static de.prob.check.tracereplay.check.TraceAnalyser.AnalyserResult.*;
import static java.util.stream.Collectors.toMap;

public class TraceAnalyser {


	public static Map<String, AnalyserResult> analyze(Set<String> typeIV, List<PersistenceDelta> newTrace, List<PersistentTransition> oldTrace) {

		if (typeIV.isEmpty()) return Collections.emptyMap();

		Map<String, List<PersistenceDelta>> space = new HashMap<>();
		for (PersistenceDelta persistenceDelta : newTrace) {
			String name = persistenceDelta.getOldTransition().getOperationName();
			if (space.containsKey(name)) {
				space.get(persistenceDelta.getOldTransition().getOperationName()).add(persistenceDelta);
			} else {
				List<PersistenceDelta> helper = new ArrayList<>();
				helper.add(persistenceDelta);
				space.put(name, helper);
			}
		}

		if (oldTrace.size() != newTrace.size()) {
			PersistentTransition lastTransitionOld = oldTrace.get(newTrace.size() - 1);
			Map<String, AnalyserResult> result = new HashMap<>();
			result.put(lastTransitionOld.getOperationName(), Removed);
			return result;
		}

		return space.entrySet()
				.stream()
				.filter(entry -> typeIV.contains(entry.getKey()))
				.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), analyze(entry.getValue())))
				.collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
	}


	public static Map<String, String> calculateStraight(Set<String> typeIVCandidates, List<PersistenceDelta> list) {
		return typeIVCandidates.stream()
				.collect(Collectors.toMap(entry -> entry, entry -> {
					List<PersistenceDelta> candidates = list
							.stream()
							.filter(element -> element.getOldTransition().getOperationName().equals(entry))
							.collect(Collectors.toList());
					return candidates.get(0).getNewTransitions().get(0).getOperationName();
				}));
	}

	public static Map<String, List<String>> calculateIntermediate(Set<String> typeIVCandidates, List<PersistenceDelta> list) {
		return typeIVCandidates.stream()
				.collect(Collectors.toMap(entry -> entry, entry -> {
					List<PersistenceDelta> candidates = list
							.stream()
							.filter(element -> element.getOldTransition().getOperationName().equals(entry))
							.collect(Collectors.toList());
					return TraceCheckerUtils.firstOrEmpty(candidates
							.stream()
							.map(innerEntry -> innerEntry.getNewTransitions()
									.stream()
									.map(PersistentTransition::getOperationName)
									.collect(Collectors.toList()))
							.collect(Collectors.toList()));
				}));
	}


	public static AnalyserResult analyze(List<PersistenceDelta> list) {

		List<NewTransitionsStatus> preAnalyzedList = list.stream().map(element -> {
			if (element.getNewTransitions().size() == 2) {
				return NewTransitionsStatus.Intermediate;
			} else {
				return NewTransitionsStatus.Straight;
			}
		}).collect(Collectors.toList());


		if (preAnalyzedList.contains(NewTransitionsStatus.Straight) && preAnalyzedList.contains(NewTransitionsStatus.Intermediate)) {
			return Mixed;
		} else if (preAnalyzedList.contains(NewTransitionsStatus.Straight)) {
			Set<String> names = list.stream()
					.flatMap(entry -> entry.getNewTransitions()
							.stream()
							.map(PersistentTransition::getOperationName))
					.collect(Collectors.toSet());
			if(names.size()>1){
				return MixedNames;
			}else {
				return Straight;
			}
		} else {
			return Intermediate;
		}
	}


	/**
	 * Helper to perform type IV analyzes for all candidates
	 * @param typeIVCandidates the candidates to be type IV
	 * @param results the results of the trace explorer
	 * @return the analysis for each type IV under the impression of the explored traces
	 */
	public static Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, Map<String, TraceAnalyser.AnalyserResult>>> performTypeIVAnalysing(
			Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, Set<String>> typeIVCandidates,
			Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> results,
			Map<Set<RenamingDelta>, List<PersistentTransition>> changelogPhase2){

		return results.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().entrySet()
						.stream()
						.collect(toMap(Map.Entry::getKey,
								innerEntry -> TraceAnalyser.analyze(typeIVCandidates.get(innerEntry.getKey()), innerEntry.getValue(), changelogPhase2.get(entry.getKey()))))));
	}


	public enum NewTransitionsStatus {
		Straight, Intermediate
	}

	/**
	 * straight -&gt; operations are mapped 1:1 there are differences in the state tho
	 * intermediate -&gt; operations are mapped 1:2 there was a new operation inserted in before for all executions of the original operation
	 * mixed -&gt; operation were mixed 1:2 sometimes. An intermediate operation was inserted but is not always necessary
	 * mixedNames -&gt; the operation was mapped 1:1 but there are differences on which target operation the original was mapped, there are different solutions for mapping
	 * Removed -&gt; the operation was removed (only if last operation)
	 */
	public enum AnalyserResult {
		Straight, Intermediate, Mixed, MixedNames, Removed
	}


}
