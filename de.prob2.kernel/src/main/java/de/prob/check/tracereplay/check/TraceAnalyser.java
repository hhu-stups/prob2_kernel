package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;

import java.util.*;
import java.util.stream.Collectors;

import static de.prob.check.tracereplay.check.TraceAnalyser.AnalyserResult.*;

public class TraceAnalyser {


	public static Map<String, AnalyserResult> analyze(Set<String> typeIV, List<PersistenceDelta> newTrace, List<PersistentTransition> oldTrace){

		Map<String, List<PersistenceDelta>> space = new HashMap<>();
		for(PersistenceDelta persistenceDelta : newTrace){
			String name = persistenceDelta.getOldTransition().getOperationName();
			if(space.containsKey(name)){
				space.get(persistenceDelta.getOldTransition().getOperationName()).add(persistenceDelta);
			}else{
				List<PersistenceDelta> helper = new ArrayList<>();
				helper.add(persistenceDelta);
				space.put(name, helper);
			}
		}

		if(oldTrace.size() != newTrace.size()){
			PersistentTransition lastTransitionOld = oldTrace.get(newTrace.size()-1);
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

	
	public static AnalyserResult analyze(List<PersistenceDelta> list){

		List<NewTransitionsStatus> analyzedList = list.stream().map(element -> {
			if(element.getNewTransitions().size()==2){
				return NewTransitionsStatus.Intermediate;
			}else{
				return NewTransitionsStatus.Straight;
			}
		}).collect(Collectors.toList());
		if(analyzedList.contains(NewTransitionsStatus.Straight) && analyzedList.contains(NewTransitionsStatus.Intermediate)){
			return Mixed;
		}else if(analyzedList.contains(NewTransitionsStatus.Straight))
		{
			return Straight;
		}else{
			return Intermediate;
		}
	}

	public enum NewTransitionsStatus{
		Straight, Intermediate
	}

	public enum AnalyserResult {
		Straight, Intermediate, Mixed, Removed
	}


}
