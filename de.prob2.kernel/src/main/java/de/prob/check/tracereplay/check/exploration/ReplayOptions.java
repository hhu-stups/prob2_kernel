package de.prob.check.tracereplay.check.exploration;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.formula.PredicateBuilder;

import java.util.*;

import static java.util.Collections.*;

public class ReplayOptions {


	private final Set<OptionFlags> globalOptions;
	private final List<String> identifierBlacklist;
	private final Map<String, Set<OptionFlags>> operationOptions;
	private final Map<String, List<String>> operationBlacklist;


	public ReplayOptions(Set<OptionFlags> globalOptions, List<String> identifierBlacklist, Map<String, Set<OptionFlags>> operationOptions,
						 Map<String, List<String>> operationBlacklist){
		this.globalOptions = globalOptions;
		this.identifierBlacklist = identifierBlacklist;
		this.operationOptions = operationOptions;
		this.operationBlacklist = operationBlacklist;
		if(!operationOptions.keySet().equals(operationBlacklist.keySet())) throw new IllegalStateException("Key set of operationOptions and operationBlacklist needs to be equal");
	}

	public ReplayOptions(){
		this.globalOptions = emptySet();
		this.identifierBlacklist = emptyList();
		this.operationOptions = emptyMap();
		this.operationBlacklist = emptyMap();
		if(!operationOptions.keySet().equals(operationBlacklist.keySet())) throw new IllegalStateException("Key set of operationOptions and operationBlacklist needs to be equal");
	}

	public PredicateBuilder createMapping(PersistentTransition persistentTransition) {
		String name = persistentTransition.getOperationName();
		PredicateBuilder predicateBuilder = new PredicateBuilder();
		Map<OptionFlags, Map<String, String>> currentStatus = new HashMap<>();
		Map<String, String> destState = persistentTransition.getDestinationStateVariables();
		Map<String, String> output = persistentTransition.getOutputParameters();
		Map<String, String> input = persistentTransition.getParameters();
		currentStatus.put(OptionFlags.Variables, destState);
		currentStatus.put(OptionFlags.Output, output);
		currentStatus.put(OptionFlags.Input, input);

		Map<OptionFlags, Map<String, String>> globalCleansing = cleanMap(currentStatus, identifierBlacklist, globalOptions);
		Map<OptionFlags, Map<String, String>> cleansed;
		if(operationOptions.containsKey(name)){
			cleansed = cleanMap(globalCleansing, operationBlacklist.get(name), operationOptions.get(name));
		}else{
			cleansed = globalCleansing;
		}

		for(Map.Entry<OptionFlags, Map<String, String>> entry : cleansed.entrySet()){
			predicateBuilder.addMap(entry.getValue());
		}

		return predicateBuilder;
	}

	public static Map<OptionFlags, Map<String, String>> cleanMap(Map<OptionFlags, Map<String, String>> currentStatus, List<String> blackList, Set<OptionFlags> options){
		for(OptionFlags value : OptionFlags.values()){
			if(options.contains(value)){
				currentStatus.remove(value);
			}else {
				for (String entry : blackList) {
					currentStatus.get(value).remove(entry);
				}
			}
		}

		return currentStatus;
	}

	public static ReplayOptions allowAll(){
		return new ReplayOptions(emptySet(), emptyList(), emptyMap(), emptyMap());
	}

	//public static ReplayOptions replayJustNames(){ return new ReplayOptions()}

	//createReplayOptionsWithIgnoredOperations

	//createReplayOptionsWithIgnoredVariables

	public enum OptionFlags{
		Variables, Input, Output
	}
}
