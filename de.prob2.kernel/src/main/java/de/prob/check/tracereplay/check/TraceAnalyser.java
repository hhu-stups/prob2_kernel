package de.prob.check.tracereplay.check;

import java.util.*;
import java.util.stream.Collectors;

import static de.prob.check.tracereplay.check.TraceAnalyser.AnalyzerResult.*;

public class TraceAnalyser {


	public static Map<String, AnalyzerResult> analyze(Set<String> typeIV, List<PersistenceDelta> newTrace){

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

		return space.entrySet()
				.stream()
				.filter(entry -> typeIV.contains(entry.getKey()))
				.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), analyze(entry.getValue())))
				.collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
	}

	
	public static AnalyzerResult analyze(List<PersistenceDelta> list){

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

	public enum AnalyzerResult{
		Straight, Intermediate, Mixed
	}


}
