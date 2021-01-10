package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;

import java.util.*;
import java.util.stream.Collectors;

import static de.prob.check.tracereplay.check.TraceAnalyser.AnalyserResult.*;

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

		List<NewTransitionsStatus> analyzedList = list.stream().map(element -> {
			if (element.getNewTransitions().size() == 2) {
				return NewTransitionsStatus.Intermediate;
			} else {
				return NewTransitionsStatus.Straight;
			}
		}).collect(Collectors.toList());
		if (analyzedList.contains(NewTransitionsStatus.Straight) && analyzedList.contains(NewTransitionsStatus.Intermediate)) {
			return Mixed;
		} else if (analyzedList.contains(NewTransitionsStatus.Straight)) {
			Set<String> names = list.stream().flatMap(entry -> entry.getNewTransitions().stream().map(PersistentTransition::getOperationName)).collect(Collectors.toSet());
			if(names.size()>1){
				return MixedNames;
			}else {
				return Straight;
			}
		} else {
			return Intermediate;
		}
	}

	public enum NewTransitionsStatus {
		Straight, Intermediate
	}

	public enum AnalyserResult {
		Straight, Intermediate, Mixed, MixedNames, Removed
	}


}
