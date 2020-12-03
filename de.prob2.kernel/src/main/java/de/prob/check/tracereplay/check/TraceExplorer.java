package de.prob.check.tracereplay.check;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.github.krukow.clj_lang.PersistentVector;
import com.google.common.collect.Maps;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.*;
import de.prob.check.tracereplay.*;
import de.prob.formula.PredicateBuilder;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;

public class TraceExplorer {



	public static Set<List<PersistenceDelta>> replayTrace(List<PersistentTransition> transitionList,
														  StateSpace stateSpace, Map<String,OperationInfo> operationInfo,
														  Set<String> typeIIICandidates) {

		Trace trace = new Trace(stateSpace);
		trace.setExploreStateByDefault(true);

		PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> traceStorage = PersistentHashMap.create();
		Map<String, Set<Map<MappingNames, Map<String, String>>>> varMappings = new HashMap<>();
		//Maybe special treatment for init and setup constants?

		for (PersistentTransition transition : transitionList) {

			String currentName = transition.getOperationName();
			if(operationInfo.containsKey(currentName) && !varMappings.containsKey(currentName)){
				varMappings.put(currentName,calculateVarMappings(transition, operationInfo.get(currentName)));
			}

			final Set<PersistentTransition> variations = new HashSet<>();

			if(varMappings.containsKey(currentName)){
				variations.addAll(varMappings.get(currentName).stream()
						.map(mapping -> createPersistentTransitionFromMapping(mapping, transition)).collect(toList()));
			}

			if(variations.isEmpty()){
				variations.add(transition);
			}

			Map<Trace, Set<Trace>> pathsMapedToPathsToGo;
			if(traceStorage.isEmpty()){
				pathsMapedToPathsToGo = new HashMap<>();
				pathsMapedToPathsToGo.put(trace, replayPersistentTransition(trace, transition).stream().map(trace::add).collect(Collectors.toSet()));
			}else{
				pathsMapedToPathsToGo = traceStorage.entrySet().stream().collect(toMap(Map.Entry::getKey, entry ->
						variations.stream().flatMap(variation -> replayPersistentTransition(entry.getKey(), variation).stream()
								.map(innerTransition -> entry.getKey().add(innerTransition))).collect(Collectors.toSet())));

			}


			PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> finalTraceStorage = traceStorage;
			Map<Trace, PersistentVector<PersistenceDelta>> da = pathsMapedToPathsToGo.entrySet().stream().flatMap(entry -> entry.getValue().stream()
					.map(innerEntry -> {

						PersistentTransition previous;
						if(trace.getCurrentTransition() == null){
							previous = null;
						}else{
							previous = new PersistentTransition(trace.getCurrentTransition());
						}


						return new AbstractMap.SimpleEntry<>(innerEntry,
								finalTraceStorage.getOrDefault(entry.getKey(), PersistentVector.emptyVector()).cons(
										new PersistenceDelta(transition,
												singletonList(
														new PersistentTransition(innerEntry.getCurrentTransition(), previous)))));
					}))
					.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


			Map<Trace, PersistentVector<PersistenceDelta>> trueResult = new HashMap<>();
			for(Trace t : da.keySet()){

				if (!trueResult.containsKey(t)&&!setContainsEqualTrace(t, trueResult.keySet())) {
					trueResult.put(t, da.get(t));
				}
			}


			traceStorage = PersistentHashMap.create(trueResult);
		}


		return traceStorage.values().stream().map(ArrayList::new).collect(Collectors.toSet());
	}



	public static Map<Map<String, Map<MappingNames, Map<String, String>>> , List<PersistenceDelta>> replayTrace2(List<PersistentTransition> transitionList,
														  StateSpace stateSpace, Map<String,OperationInfo> operationInfo,
														  Set<String> typeIIICandidates) {

		Trace trace = new Trace(stateSpace);
		trace.setExploreStateByDefault(true);

		PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> traceStorage = PersistentHashMap.create();
		Map<String, Set<Map<MappingNames, Map<String, String>>>> varMappings = generateVarMappings(transitionList, operationInfo, typeIIICandidates);

		Map<Map<String, Map<MappingNames, Map<String, String>>> , List<PersistenceDelta>> selectedMappingsToResults
				= generateAllPossibleMappingVariations(transitionList, operationInfo, typeIIICandidates).stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(entry, new ArrayList<PersistenceDelta>()))
				.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

		Map<Map<String, Map<MappingNames, Map<String, String>>> , Trace> selectedMappingToCurrentState
				= generateAllPossibleMappingVariations(transitionList, operationInfo, typeIIICandidates).stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(entry, new ArrayList<PersistenceDelta>()))
				.collect(toMap(AbstractMap.SimpleEntry::getKey, entry -> trace));

		Set<Map<String, Map<MappingNames, Map<String, String>>> > selectedMappingsToResultsKeys = selectedMappingsToResults.keySet();
		//Maybe special treatment for init and setup constants?

		selectedMappingsToResultsKeys.forEach(entry ->
				transitionList.forEach(transition -> {
			Transition result;
			if(entry.containsKey(transition.getOperationName())){
				PersistentTransition reworkedTransition =
						createPersistentTransitionFromMapping(entry.get(transition.getOperationName()), transition);
				result =  replayPersistentTransition2(selectedMappingToCurrentState.get(entry), reworkedTransition);
			}
			else{
				result =  replayPersistentTransition2(selectedMappingToCurrentState.get(entry), transition);
			}

			Transition current = selectedMappingToCurrentState.get(entry).getCurrentTransition();

			Trace newState = selectedMappingToCurrentState.get(entry).add(result);
			selectedMappingToCurrentState.put(entry, newState);

			PersistentTransition newPersistentTransition;
			if(current==null){
				newPersistentTransition = new PersistentTransition(result, null);
			}else{
				newPersistentTransition = new PersistentTransition(result, new PersistentTransition(current, null));
			}

			selectedMappingsToResults.get(entry).add(new PersistenceDelta(transition, singletonList(newPersistentTransition)));
		}));

		return selectedMappingsToResults;
	}


	public static Map<String, Set<Map<MappingNames, Map<String, String>>>> generateVarMappings(List<PersistentTransition> transitionList, Map<String,
			OperationInfo> operationInfo, Set<String> typeIIICandidates){
		List<PersistentTransition> typeIIITransitions = transitionList.stream()
				.filter(transition -> transition.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME) ||
						transition.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME))
				.filter(transition -> typeIIICandidates.contains(transition.getOperationName())).collect(toList());

		Map<String, Set<Map<MappingNames, Map<String, String>>>> varMappings = new HashMap<>();

		for (PersistentTransition transition : transitionList) {

			String currentName = transition.getOperationName();
			if (operationInfo.containsKey(currentName) && !varMappings.containsKey(currentName) && typeIIICandidates.contains(currentName)) {
				varMappings.put(currentName, calculateVarMappings(transition, operationInfo.get(currentName)));

			}
		}

		return varMappings;
	}




	public static Set<Map<String, Map<MappingNames, Map<String, String>>>>
	generateAllPossibleMappingVariations(List<PersistentTransition> transitionList, Map<String,
			OperationInfo> operationInfo, Set<String> typeIIICandidates){

		List<PersistentTransition> typeIIITransitions = transitionList.stream()
				.filter(transition -> !transition.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME) ||
						!transition.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME))
				.filter(transition -> typeIIICandidates.contains(transition.getOperationName())).collect(toList());


		List<List<HashMap<String, Map<MappingNames, Map<String, String>>>>> listOfMappings = typeIIITransitions.stream()
				.map(transition -> calculateVarMappings(transition, operationInfo.get(transition.getOperationName()))
				.stream().map(mapping -> {
					HashMap<String, Map<MappingNames, Map<String, String>>> result = new HashMap<>();
					result.put(transition.getOperationName(), mapping);
					return result;
				}).collect(toList())).collect(toList());


		return new HashSet<>(listOfMappings.stream().reduce(emptyList(), (acc, current) -> {
			if (acc.isEmpty()) return current;
			if (current.isEmpty()) return acc;
			return product(acc, current);
		}));


	}

	private static List<HashMap<String, Map<MappingNames, Map<String, String>>>> product(
			List<HashMap<String, Map<MappingNames, Map<String, String>>>> a,
			List<HashMap<String, Map<MappingNames, Map<String, String>>>> b) {
		return a.stream().flatMap(entryA -> b.stream().map(entryB -> {
			HashMap<String, Map<MappingNames, Map<String, String>>> result = Maps.newHashMap(entryA);
			result.putAll(entryB);
			return result;
		})).collect(toList());
	}

	public enum MappingNames{
		VARIABLES, INPUT_PARAMETERS, OUTPUT_PARAMETERS
	}


	/**
	 * Creates a PersistentTransition with an existing mapping
	 * @param mapping the mapping to create the PT from
	 * @param current the current (old) transition
	 * @return the new transition
	 */
	public static PersistentTransition createPersistentTransitionFromMapping(Map<MappingNames, Map<String, String>> mapping,
																			 PersistentTransition current){

		HashMap<MappingNames, Map<String, String>> mappingsHelper = new HashMap<>();

		mappingsHelper.put(MappingNames.VARIABLES, current.getDestinationStateVariables());
		mappingsHelper.put(MappingNames.OUTPUT_PARAMETERS, current.getOutputParameters());

		Map<MappingNames, Map<String, String>> result = mapping.entrySet().stream()
				.filter(entry -> !entry.getKey().equals(MappingNames.INPUT_PARAMETERS))
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().entrySet().stream()
						.collect(toMap(Map.Entry::getValue, innerEntry ->
								mappingsHelper.get(entry.getKey()).get(innerEntry.getKey())))));

		Map<String, String> resultInputParameters = mapping.get(MappingNames.INPUT_PARAMETERS).entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> current.getParameters().get(entry.getValue())));


		return current.copyWithNewDestState(result.get(MappingNames.VARIABLES)).copyWithNewParameters(resultInputParameters)
				.copyWithNewOutputParameters(result.get(MappingNames.OUTPUT_PARAMETERS));
	}

	/**
	 * Calculates the new all possible mappings for a variable
	 * @param transition the transition to calculate the mappings for
	 * @param operationMapping the corresponding operation
	 * @return the mapping
	 */
	public static Set<Map<MappingNames, Map<String, String>>> calculateVarMappings(PersistentTransition transition,
																				   OperationInfo operationMapping){

		Map<MappingNames, List<String>> operationInfos = new HashMap<>();

		operationInfos.put(MappingNames.VARIABLES, operationMapping.getWrittenVariables());
		operationInfos.put(MappingNames.INPUT_PARAMETERS, operationMapping.getParameterNames());
		operationInfos.put(MappingNames.OUTPUT_PARAMETERS, operationMapping.getOutputParameterNames());


		HashMap<MappingNames, Map<String, String>> mappingsHelper = new HashMap<>();

		mappingsHelper.put(MappingNames.VARIABLES, createSelfMapping(transition.getDestinationStateVariables()));
		mappingsHelper.put(MappingNames.INPUT_PARAMETERS, createSelfMapping(transition.getParameters()));
		mappingsHelper.put(MappingNames.OUTPUT_PARAMETERS, createSelfMapping(transition.getOutputParameters()));

		Set<Map<MappingNames, Map<String, String>>> mappings = new HashSet<>();
		mappings.add(mappingsHelper);

		for(MappingNames name : operationInfos.keySet()){

			if(!operationInfos.get(name).isEmpty())
			{
				mappings = mappings.stream()
						.flatMap(mapping -> possibleConstellations2(new ArrayList<>(mapping.get(name).keySet()),
								operationInfos.get(name)).stream().map(mappingValue -> {
									HashMap<MappingNames, Map<String, String>> alteredInnerMapping = new HashMap<>(mapping);
									alteredInnerMapping.put(name, mappingValue);
									return alteredInnerMapping; })).collect(Collectors.toSet());
			}

			if(mappings.isEmpty()) mappings.add(mappingsHelper);

		}

		return mappings;

	}

	public static Map<String, String> createSelfMapping(Map<String, String> mapping){
		return mapping.entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getKey));
	}


	/**
	 * Gets a mapping from variables to values (input/output/variables) and the variables of the current operation and
	 * calculates all possible mappings, e.g.
	 * inc(1,2) -> inc(x,y,z) produces
	 * 			-> inc(1, 2, ?)
	 * 			-> inc(1, ?, 2)
	 * 			-> inc(?, 1, 2)
	 * 		[..]
	 * void means that the mapping is not existent in the resulting map
	 * @param values the mapping from the current transition
	 * @param target the variables manipulated by the transition according to the new machine
	 * @return all possible mappings
	 */
	public static Set<Map<String, String>> possibleConstellations2(List<String> values, List<String> target){

		if(values.isEmpty()||target.isEmpty()) return Collections.emptySet();

		Set<Map<String, String>> result;
		if(values.size()>target.size()){
			result =  permutate(TraceCheckerUtils.generatePerm(values), new ArrayList<>(target));
		}
		else {
			result =  permutate(TraceCheckerUtils.generatePerm(new ArrayList<>(target)), values);
		}

		return result;
	}

	/**
	 * Gets a mapping from variables to values (input/output/variables) and the variables of the current operation and
	 * calculates all possible mappings, e.g.
	 * inc(1,2) -> inc(x,y,z) produces
	 * 			-> inc(1, 2, ?)
	 * 			-> inc(1, ?, 2)
	 * 			-> inc(?, 1, 2)
	 * 		[..]
	 * void means that the mapping is not existent in the resulting map
	 * @param elements the mapping from the current transition
	 * @param target the variables manipulated by the transition according to the new machine
	 * @return all possible mappings
	 */
	public static Set<Map<String, String>> possibleConstellations(Map<String, String> elements, List<String> target){

		if(elements.isEmpty()||target.isEmpty()) return Collections.emptySet();

		List<String> values = new ArrayList<>(elements.keySet());
		Set<Map<String, String>> result;
		if(values.size()>target.size()){
			result =  permutate(TraceCheckerUtils.generatePerm(values), new ArrayList<>(target));
		}
		else {
			result =  permutate(TraceCheckerUtils.generatePerm(new ArrayList<>(target)), values);
		}

		return result.stream().map(entry ->
				entry.entrySet().stream().collect(toMap(Map.Entry::getValue, inner -> elements.get(inner.getKey())))).collect(Collectors.toSet());
	}

	/**
	 * helper for @possibleConstellations
	 * @param permutations a list with all permutations
	 * @param values the values to map onto
	 * @return a set of mappings with the new constellations
	 */
	public static Set<Map<String, String>> permutate(List<List<String>> permutations, List<String> values){
		Set<Map<String, String>> resultSet = new HashSet<>();
		for(List<String> option : permutations){
			Map<String, String> resultMap = new HashMap<>();
			for(int i = 0; i < option.size(); i++){
				if(i < values.size()){
					resultMap.put(values.get(i), option.get(i));
				}
				//Else the values are not for direct interest when building the predicate
			}
			resultSet.add(resultMap);
		}

		return resultSet;
	}

	/**
	 * Compares a trace and a set of traces on the property of their equivalence. Equivalence is archived if the
	 * contained transitions list can be transformed to persistent transitions and those are equivalent
	 * @param t the trace to compare
	 * @param set the set of traces to compare against
	 * @return wherever the set contains the given trace t
	 */
	public static boolean setContainsEqualTrace(Trace t, Set<Trace> set){
		return set.stream().anyMatch(trace -> {
			List<PersistentTransition> transitionList1 = trace.getTransitionList().stream()
					.map(transition -> new PersistentTransition(transition, null)).collect(toList());
			List<PersistentTransition> transitionList2 = t.getTransitionList().stream()
					.map(transition -> new PersistentTransition(transition, null)).collect(toList());

			return transitionList1.equals(transitionList2);
		});
	}


	//Evtl. Return map<PersistentTransition, Transition>
	private static Set<Transition> replayPersistentTransition(Trace t, PersistentTransition persistentTransition) {


		Map<Constraint, GetOperationByPredicateCommand>  possibleSolutions = Arrays.stream(Constraint.values()).collect(toMap(entry -> entry,
				entry ->{
					StateSpace stateSpace = t.getStateSpace();

					final GetOperationByPredicateCommand command =  commandBuild(stateSpace, persistentTransition, t, entry);

					stateSpace.execute(command);

					return command;
				})).entrySet().stream().filter(entry -> !entry.getValue().getNewTransitions().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		Set<Transition> gna = possibleSolutions.entrySet().stream().flatMap(entry -> entry.getValue().getNewTransitions().stream()).collect(Collectors.toSet());

		return gna;
	}


	//Evtl. Return map<PersistentTransition, Transition>
	private static Transition replayPersistentTransition2(Trace t, PersistentTransition persistentTransition) {

		StateSpace stateSpace = t.getStateSpace();

		final GetOperationByPredicateCommand command =  commandBuild(stateSpace, persistentTransition, t, Constraint.PARAMETER_AND_DESTINATION_STATE);

		stateSpace.execute(command);

		return command.getNewTransitions().get(0);
	}

	enum Constraint{
		ONLY_PARAMETER, PARAMETER_AND_DESTINATION_STATE
	}

	public static GetOperationByPredicateCommand commandBuild(StateSpace stateSpace, PersistentTransition persistentTransition,
															  Trace t, Constraint constraint){

		PredicateBuilder predicateBuilder = new PredicateBuilder();

		switch (constraint){
			case ONLY_PARAMETER:
				predicateBuilder.addMap(persistentTransition.getParameters());
				break;
			case PARAMETER_AND_DESTINATION_STATE:
				predicateBuilder.addMap(persistentTransition.getParameters());
				predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());
		}

		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		return new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(),
				persistentTransition.getOperationName(), pred, 1);
	}


	public static List<Transition> resultChecker(ReplayOptions options, List<Transition> possibleTransitions,
												 PersistentTransition persistentTransition, StateSpace stateSpace){

		return possibleTransitions.stream().filter(transition -> {

			if(!options.checkDestState()) return true;

			Map<String, String> results = convertResult(transition);

			Map<String, String> comparator = filterFromTransition(persistentTransition.getDestinationStateVariables(), results);

			return comparator.isEmpty();
		}).filter(transition -> {

			if(!options.checkDestStateNotChanged()) return true;


			Map<String, String> results = convertResult(transition);

			Set<String> comparator = persistentTransition.getDestStateNotChanged().stream()
					.filter(results::containsKey).collect(Collectors.toSet());

			return comparator.isEmpty();
		}).filter(transition -> {

			if(!options.checkOutput()) return true;

			List<String> outputParameterNames = stateSpace.getLoadedMachine().getOperations()
					.get(persistentTransition.getOperationName()).getOutputParameterNames();

			Map<String, String> toCompare = TraceCheckerUtils.zip(outputParameterNames, transition.getReturnValues());

			Map<String , String> comparator = filterFromTransition(persistentTransition.getOutputParameters(), toCompare);

			return comparator.isEmpty();
		}).collect(toList());
	}


	public static Map<String, String> convertResult(Transition transition){
		Map<ClassicalB, EvalResult> converted = transition.getDestination().getVariableValues(FormulaExpand.EXPAND)
				.entrySet().stream().collect(toMap(key -> (ClassicalB) key.getKey(), key -> (EvalResult) key.getValue()));
		return converted.entrySet().stream().collect(toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().getValue()));

	}

	public static Map<String, String> filterFromTransition(Map<String, String> persistentTransition, Map<String, String> toCompare){
		return persistentTransition.entrySet().stream()
				.filter(entry -> toCompare.containsKey(entry.getKey()) &&
						toCompare.get(entry.getKey()).equals(entry.getValue()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
