package de.prob.check.tracereplay.check;

import com.google.common.collect.Maps;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exceptions.MappingFactoryInterface;
import de.prob.check.tracereplay.check.exceptions.TransitionHasNoSuccessorException;
import de.prob.formula.PredicateBuilder;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.util.*;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class TraceExplorer {


	private final boolean initWasSet;
	private final MappingFactoryInterface mappingFactory;

	public TraceExplorer(boolean initWasSet, MappingFactoryInterface mappingFactory) {
		this.initWasSet = initWasSet;
		this.mappingFactory =mappingFactory;
	}

	public static List<PersistentTransition> transformTransitionList(Map<String, Map<MappingNames, Map<String, String>>> mapping,
																	 List<PersistentTransition> transitionList) {
		return transitionList.stream().map(transition ->
		{
			if (mapping.containsKey(transition.getOperationName())) {
				return createPersistentTransitionFromMapping(mapping.get(transition.getOperationName()), transition);
			}
			return transition;
		}).collect(toList());
	}

	public static Map<Map<String, Map<String, String>>, List<PersistenceDelta>> removeHelperVariableMappings(
			Map<Map<String, Map<MappingNames, Map<String, String>>>, List<PersistenceDelta>> mappings) {
		return mappings.entrySet().stream().collect(toMap(variationToDelta ->
				variationToDelta.getKey().entrySet().stream().collect(toMap(Map.Entry::getKey, operationToVariation ->
						operationToVariation.getValue().values().stream().reduce(new HashMap<>(), (acc, current) -> {
							Map<String, String> mapping = new HashMap<>();
							mapping.putAll(acc);
							mapping.putAll(current);
							return mapping;
						}))), Map.Entry::getValue));
	}

	public static PersistentTransition generateNewTransition(Transition oldTransition, Transition newTransition) {
		if (oldTransition == null) {
			return new PersistentTransition(newTransition, null);
		} else {
			return new PersistentTransition(newTransition, new PersistentTransition(oldTransition, null));
		}
	}

	public Set<Map<String, Map<MappingNames, Map<String, String>>>> generateAllPossibleMappingVariations(
			List<PersistentTransition> transitionList, Map<String, OperationInfo> operationInfo, Set<String> typeIIICandidates,
			Set<String> usedVars, Set<String> usedSets,  Set<String> usedConstants) {

		if (typeIIICandidates.isEmpty()) {
			Set<Map<String, Map<MappingNames, Map<String, String>>>> result = new HashSet<>();
			result.add(emptyMap());
			return result;
		}

		List<PersistentTransition> selectionOfTypeIIITransitions = new ArrayList<>(transitionList
				.stream()
				.filter(transition -> !transition.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME))
				.filter(transition -> !transition.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME))
				.filter(transition -> typeIIICandidates.contains(transition.getOperationName()))
				.collect(toCollection(() -> new TreeSet<>(Comparator.comparing(PersistentTransition::getOperationName)))));


		List<List<HashMap<String, Map<MappingNames, Map<String, String>>>>> listOfMappings = selectionOfTypeIIITransitions
				.stream()
				.map(transition -> calculateVarMappings(transition, operationInfo.get(transition.getOperationName()), usedVars, usedSets, usedConstants)
						.stream()
						.map(mapping -> {
							HashMap<String, Map<MappingNames, Map<String, String>>> result = new HashMap<>();
							result.put(transition.getOperationName(), mapping);
							return result;
						})
						.collect(toList())).collect(toList());


		return new HashSet<>(listOfMappings.stream().reduce(emptyList(), (acc, current) -> {
			if (acc.isEmpty()) return current;
			if (current.isEmpty()) return acc;
			return product(acc, current);
		}));


	}

	/**
	 * Calculates the cartesian product for the special case of input
	 *
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
	 *
	 * @param oldVars the "old" variables of a persistent transition
	 * @param newVars the "new" variables extracted from the operation info
	 * @return a set of mappings
	 */
	public Set<Map<String, String>> createAllPossiblePairs(List<String> oldVars, List<String> newVars, MappingNames currentMapping, String name) {
		if (oldVars.isEmpty()) return emptySet();
		if (newVars.isEmpty()) return emptySet();

		int permutationNewSize = Math.min(oldVars.size(), newVars.size());

		if(oldVars.size() > 9 || newVars.size() > 9 || oldVars.size()+newVars.size() > 9)
		{
			return singleton(mappingFactory.produceMappingManager().askForMapping(oldVars, newVars, name ,currentMapping));
		}
		else
		{
			List<List<String>> permutationsOld = TraceCheckerUtils.generatePerm(new ArrayList<>(oldVars), 0, permutationNewSize, emptyList());
			List<List<String>> permutationsNew = TraceCheckerUtils.generatePerm(new ArrayList<>(newVars), 0, permutationNewSize, emptyList());

			return permutationsOld.stream().flatMap(permutationOld -> permutationsNew.stream().map(permutationNew ->
					TraceCheckerUtils.zip(permutationOld, permutationNew))).collect(toSet());

		}


	}

	/**
	 * Creates a PersistentTransition with an existing mapping
	 *
	 * @param mapping the mapping to create the PT from
	 * @param current the current (old) transition
	 * @return the new transition
	 */
	public static PersistentTransition createPersistentTransitionFromMapping(Map<MappingNames, Map<String, String>> mapping,
																			 PersistentTransition current) {

		Map<String, String> destChangedVariables = mapping.get(MappingNames.VARIABLES_MODIFIED).entrySet().stream()
				.filter(entry -> !current.getDestStateNotChanged().contains(entry.getKey()))
				.collect(toMap(Map.Entry::getValue, entry -> current.getDestinationStateVariables().get(entry.getKey())));


		Set<String> destNotChangedVariables = mapping.get(MappingNames.VARIABLES_READ).entrySet().stream()
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
	 *
	 * @param transition       the transition to calculate the mappings for
	 * @param operationMapping the corresponding operation
	 * @return a set with all mappings each mapping name represents either input/output/variables and maps to the corresponding
	 * identifiers in the schema old -> new
	 */
	public Set<Map<MappingNames, Map<String, String>>> calculateVarMappings(PersistentTransition transition,
																			OperationInfo operationMapping,
																			Set<String> usedVars,
																			Set<String> usedSets,
																			Set<String> usedConstants) {


		Map<MappingNames, List<String>> operationInfos = new HashMap<>();

		operationInfos.put(MappingNames.VARIABLES_MODIFIED, new ArrayList<>(operationMapping.getNonDetWrittenVariables()));
		operationInfos.put(MappingNames.VARIABLES_READ, new ArrayList<>(operationMapping.getReadVariables()));

		operationInfos.get(MappingNames.VARIABLES_MODIFIED).addAll(usedVars);
		operationInfos.get(MappingNames.VARIABLES_READ).addAll(usedVars);
		operationInfos.get(MappingNames.VARIABLES_READ).addAll(usedConstants);
		operationInfos.get(MappingNames.VARIABLES_READ).addAll(usedSets);

		operationInfos.put(MappingNames.INPUT_PARAMETERS, new ArrayList<>(operationMapping.getParameterNames()));
		operationInfos.put(MappingNames.OUTPUT_PARAMETERS, new ArrayList<>(operationMapping.getOutputParameterNames()));

		Map<MappingNames, List<String>> transitionInfos = new HashMap<>();
		transitionInfos.put(MappingNames.VARIABLES_MODIFIED, new ArrayList<>(transition.getDestinationStateVariables().keySet()));
		transitionInfos.put(MappingNames.VARIABLES_READ, new ArrayList<>(transition.getDestStateNotChanged()));
		transitionInfos.put(MappingNames.INPUT_PARAMETERS, new ArrayList<>(transition.getParameters().keySet()));
		transitionInfos.put(MappingNames.OUTPUT_PARAMETERS, new ArrayList<>(transition.getOutputParameters().keySet()));


		HashMap<MappingNames, Map<String, String>> mappingsHelper = new HashMap<>();

		mappingsHelper.put(MappingNames.VARIABLES_MODIFIED, emptyMap());
		mappingsHelper.put(MappingNames.VARIABLES_READ, emptyMap());
		mappingsHelper.put(MappingNames.INPUT_PARAMETERS, emptyMap());
		mappingsHelper.put(MappingNames.OUTPUT_PARAMETERS, emptyMap());

		Set<Map<MappingNames, Map<String, String>>> mappings = new HashSet<>();
		mappings.add(mappingsHelper);

		for (MappingNames name : MappingNames.values()) {

			Set<Map<MappingNames, Map<String, String>>> mappingsCopy = mappings;
			Set<Map<MappingNames, Map<String, String>>> mappingsAppliedToExistingCopy =
					createAllPossiblePairs((new ArrayList<>(transitionInfos.get(name))), operationInfos.get(name), name, transition.getOperationName())
					.stream()
					.flatMap(possiblePair ->{
							return mappingsCopy.stream().map(mapping -> {
								Map<MappingNames, Map<String, String>> alteredInnerMapping = new HashMap<>(mapping);
								alteredInnerMapping.put(name, possiblePair);
								return alteredInnerMapping;
							});})
					.collect(toSet());
			if(!mappingsAppliedToExistingCopy.isEmpty()){
				mappings = mappingsAppliedToExistingCopy;
			}
		}


		return mappings;


	}

	/**
	 * helper for @possibleConstellations
	 *
	 * @param permutations a list with all permutations
	 * @param values       the values to map onto
	 * @return a set of mappings with the new constellations
	 */
	public static Set<Map<String, String>> permuted(List<List<String>> permutations, List<String> values) {
		Set<Map<String, String>> resultSet = new HashSet<>();
		for (List<String> option : permutations) {
			Map<String, String> resultMap = new HashMap<>();
			for (int i = 0; i < option.size(); i++) {
				if (i < values.size()) {
					resultMap.put(values.get(i), option.get(i));
				}
				//Else the values are not for direct interest when building the predicate
			}
			resultSet.add(resultMap);
		}

		return resultSet;
	}

	public static GetOperationByPredicateCommand buildTransition(StateSpace stateSpace, PersistentTransition persistentTransition,
																 Trace t) {

		PredicateBuilder predicateBuilder = new PredicateBuilder();


		predicateBuilder.addMap(persistentTransition.getParameters());
		predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());


		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		return new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(),
				persistentTransition.getOperationName(), pred, 1);
	}

	public Map<Map<String, Map<String, String>>, List<PersistenceDelta>> replayTrace(List<PersistentTransition> transitionList,
																					 StateSpace stateSpace,
																					 Map<String, OperationInfo> operationInfo,
																					 Set<String> typeIIICandidates,
																					 Set<String> usedVars,
																					 Set<String> usedSets,
																					 Set<String> usedConst) {

		Trace trace = new Trace(stateSpace);
		trace.setExploreStateByDefault(true);

		Set<Map<String, Map<MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				generateAllPossibleMappingVariations(transitionList, operationInfo, typeIIICandidates, usedVars, usedSets, usedConst);

		Map<Map<String, Map<MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = selectedMappingsToResultsKeys.stream()
				.map(mapping -> {
					List<PersistentTransition> newTransitions = transformTransitionList(mapping, transitionList);
					List<PersistenceDelta> preparedDelta = TraceCheckerUtils.zipPreserveOrder(transitionList, newTransitions)
							.entrySet().stream()
							.map(entry -> new PersistenceDelta(entry.getKey(), singletonList(entry.getValue())))
							.collect(toList());
					return new AbstractMap.SimpleEntry<>(mapping, createNewTransitionList(preparedDelta, trace));
				})
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


		return removeHelperVariableMappings(result);
	}


	public List<PersistenceDelta> createNewTransitionList(List<PersistenceDelta> persistentTransitions, Trace oldState) {
		Trace currentState = oldState;
		List<PersistenceDelta> newTransitions = new ArrayList<>();
		for (PersistenceDelta oldTPersistentTransition : persistentTransitions) {
			try {
				Transition oldTransition = oldState.getCurrentTransition();
				Transition newTransition = replayPersistentTransition(currentState, oldTPersistentTransition.getNewTransitions().get(0));
				currentState = currentState.add(newTransition);
				PersistentTransition newPersistentTransition = generateNewTransition(oldTransition, newTransition);
				newTransitions.add(new PersistenceDelta(oldTPersistentTransition.getOldTransition(), singletonList(newPersistentTransition)));
			} catch (TransitionHasNoSuccessorException exception) {
				return emptyList();
			}
		}
		return newTransitions;
	}

	//Evtl. Return map<PersistentTransition, Transition>
	private Transition replayPersistentTransition(Trace t, PersistentTransition persistentTransition) throws TransitionHasNoSuccessorException {

		StateSpace stateSpace = t.getStateSpace();

		final GetOperationByPredicateCommand command;

		if (persistentTransition.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME)) {
			command = buildInit(stateSpace, persistentTransition, t);
		} else if(persistentTransition.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME)){
			command = buildSC(stateSpace, persistentTransition, t);
		} else {
			command = buildTransition(stateSpace, persistentTransition, t);
		}

		stateSpace.execute(command);

		if (command.getNewTransitions().size() >= 1) {
			return command.getNewTransitions().get(0);
		}
		throw new TransitionHasNoSuccessorException(persistentTransition);
	}

	public GetOperationByPredicateCommand buildInit(StateSpace stateSpace, PersistentTransition persistentTransition,
													Trace t) {

		PredicateBuilder predicateBuilder = new PredicateBuilder();

		if (initWasSet) {
			predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());
		}

		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		return new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(),
				persistentTransition.getOperationName(), pred, 1);
	}

	public GetOperationByPredicateCommand buildSC(StateSpace stateSpace, PersistentTransition persistentTransition,
													Trace t) {

		PredicateBuilder predicateBuilder = new PredicateBuilder();

		predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());

		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		return new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(),
				persistentTransition.getOperationName(), pred, 1);
	}

	/**
	 * An helper datatype to better group maps of the form Map<\String, String>
	 */
	public enum MappingNames {
		 INPUT_PARAMETERS, OUTPUT_PARAMETERS, VARIABLES_MODIFIED, VARIABLES_READ
	}



}
