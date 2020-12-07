package de.prob.check.tracereplay.check;

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

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class TraceExplorer {



	public static Map<Map<String, Map<String, String>> , List<PersistenceDelta>> replayTrace(List<PersistentTransition> transitionList,
																							 StateSpace stateSpace, Map<String,OperationInfo> operationInfo,
																							 Set<String> typeIIICandidates) {

		Trace trace = new Trace(stateSpace);
		trace.setExploreStateByDefault(true);

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


		return selectedMappingsToResults.entrySet().stream().collect(toMap(variationToDelta ->
				variationToDelta.getKey().entrySet().stream().collect(toMap(Map.Entry::getKey, operationToVariation ->
						operationToVariation.getValue().values().stream().reduce(new HashMap<>(), (acc, current) ->{
							Map<String, String> mapping = new HashMap<>();
							mapping.putAll(acc);
							mapping.putAll(current);
							return mapping;
						}))), Map.Entry::getValue));
	}



	public static Set<Map<String, Map<MappingNames, Map<String, String>>>> generateAllPossibleMappingVariations(
			List<PersistentTransition> transitionList, Map<String, OperationInfo> operationInfo, Set<String> typeIIICandidates){

		if(typeIIICandidates.isEmpty()) {
			Set<Map<String, Map<MappingNames, Map<String, String>>>> result = new HashSet<>();
			result.add(emptyMap());
			return result;
		}

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

	/**
	 * Calculates the cartesian product for the special case of input
	 * @param a the first "vector"
	 * @param b the second "vector"
	 * @return the cartesian product
	 */
	private static List<HashMap<String, Map<MappingNames, Map<String, String>>>> product(
			List<HashMap<String, Map<MappingNames, Map<String, String>>>> a,
			List<HashMap<String, Map<MappingNames, Map<String, String>>>> b) {
		return a.stream().flatMap(entryA -> b.stream().map(entryB -> {
			HashMap<String, Map<MappingNames, Map<String, String>>> result = Maps.newHashMap(entryA);
			result.putAll(entryB);
			return result;
		})).collect(toList());
	}


	/**
	 * Creates all possible pairs between two maps e.g.
	 * [a,b,c] + [x,z]
	 * -> [[a:x, b:z], [a:z, b:x], [a:x, c:z], [a:z, c:x], [c:x, b:z], [b:x, c:z]]
	 * @param oldVars the "old" variables of a persistent transition
	 * @param newVars the "new" variables extracted from the operation info
	 * @return a set of mappings
	 */
	public static Set<Map<String, String>> createAllPossiblePairs(List<String> oldVars, List<String> newVars){
		if(oldVars.isEmpty()) return emptySet();
		if(newVars.isEmpty()) return emptySet();

		List<List<String>> permutationsOld = TraceCheckerUtils.generatePerm(new ArrayList<>(oldVars));

		List<List<String>> permutationsNew = TraceCheckerUtils.generatePerm(new ArrayList<>(newVars));


		return permutationsOld.stream().flatMap(permutationOld -> permutationsNew.stream().map(permutationNew ->
				TraceCheckerUtils.zip(permutationOld, permutationNew))).collect(toSet());

	}

	/**
	 * An helper datatype to better group maps of the form Map<\String, String>
	 */
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

		Map<String, String> destChangedVariables = mapping.get(MappingNames.VARIABLES).entrySet().stream()
				.filter(entry -> !current.getDestStateNotChanged().contains(entry.getKey()))
				.collect(toMap(Map.Entry::getValue, entry -> current.getDestinationStateVariables().get(entry.getKey())));



		Set<String> destNotChangedVariables = mapping.get(MappingNames.VARIABLES).entrySet().stream()
				.filter(entry -> current.getDestStateNotChanged().contains(entry.getKey()))
				.map(Map.Entry::getValue).collect(toSet());


		Map<String, String> resultOutputParameters = mapping.get(MappingNames.OUTPUT_PARAMETERS).entrySet().stream()
				.collect(toMap(Map.Entry::getValue, entry -> current.getOutputParameters().get(entry.getKey())));


		Map<String, String> resultInputParameters = mapping.get(MappingNames.INPUT_PARAMETERS).entrySet().stream()
				.collect(toMap(Map.Entry::getValue, entry -> current.getParameters().get(entry.getKey())));


		return current.copyWithNewDestState(destChangedVariables).copyWithNewParameters(resultInputParameters)
				.copyWithNewOutputParameters(resultOutputParameters).copyWithDestStateNotChanged(destNotChangedVariables);
	}



	/**
	 * Calculates the new all possible mappings for a variable
	 * @param transition the transition to calculate the mappings for
	 * @param operationMapping the corresponding operation
	 * @return a set with all mappings each mapping name represents either input/output/variables and maps to the corresponding
	 * identifiers in the schema old -> new
	 */
	public static Set<Map<MappingNames, Map<String, String>>> calculateVarMappings(PersistentTransition transition,
																				   OperationInfo operationMapping){

		Map<MappingNames, List<String>> operationInfos = new HashMap<>();

		List<String> variables = new ArrayList<>(operationMapping.getWrittenVariables());
		variables.addAll(operationMapping.getNonDetWrittenVariables());
		variables.addAll(operationMapping.getReadVariables());

		operationInfos.put(MappingNames.VARIABLES, variables);
		operationInfos.put(MappingNames.INPUT_PARAMETERS, operationMapping.getParameterNames());
		operationInfos.put(MappingNames.OUTPUT_PARAMETERS, operationMapping.getOutputParameterNames());

		Map<MappingNames, List<String>> mappingHelper1 = new HashMap<>();
		List<String> oldVariables = new ArrayList<>(transition.getDestStateNotChanged());
		oldVariables.addAll(transition.getDestinationStateVariables().keySet());
		mappingHelper1.put(MappingNames.VARIABLES, oldVariables);
		mappingHelper1.put(MappingNames.INPUT_PARAMETERS, new ArrayList<>(transition.getParameters().keySet()));
		mappingHelper1.put(MappingNames.OUTPUT_PARAMETERS, new ArrayList<>(transition.getOutputParameters().keySet()));


		HashMap<MappingNames, Map<String, String>> mappingsHelper = new HashMap<>();

		mappingsHelper.put(MappingNames.VARIABLES, emptyMap());
		mappingsHelper.put(MappingNames.INPUT_PARAMETERS, emptyMap());
		mappingsHelper.put(MappingNames.OUTPUT_PARAMETERS, emptyMap());

		Set<Map<MappingNames, Map<String, String>>> mappings = new HashSet<>();
		mappings.add(mappingsHelper);

		for(MappingNames name : operationInfos.keySet()){

			if(!operationInfos.get(name).isEmpty()) {
				mappings = mappings.stream()
						.flatMap(mapping ->  createAllPossiblePairs(new ArrayList<>(mappingHelper1.get(name)),
								operationInfos.get(name)).stream().map(mappingValue -> {
							HashMap<MappingNames, Map<String, String>> alteredInnerMapping = new HashMap<>(mapping);
							alteredInnerMapping.put(name, mappingValue);
							return alteredInnerMapping;
						})).collect(Collectors.toSet());
			}

			if(mappings.isEmpty()) mappings.add(mappingsHelper);

		}

		return mappings;

	}


	/**
	 * helper for @possibleConstellations
	 * @param permutations a list with all permutations
	 * @param values the values to map onto
	 * @return a set of mappings with the new constellations
	 */
	public static Set<Map<String, String>> permuted(List<List<String>> permutations, List<String> values){
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



}
