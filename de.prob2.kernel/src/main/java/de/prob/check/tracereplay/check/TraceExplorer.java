package de.prob.check.tracereplay.check;

import com.google.common.collect.Maps;
import de.prob.animator.command.ConstructTraceCommand;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exceptions.MappingFactoryInterface;
import de.prob.check.tracereplay.check.exceptions.TransitionHasNoSuccessorException;
import de.prob.formula.PredicateBuilder;
import de.prob.statespace.*;

import java.util.*;

import static de.prob.check.tracereplay.check.TraceCheckerUtils.firstOrEmpty;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class TraceExplorer {


	private final boolean initWasSet;
	private final MappingFactoryInterface mappingFactory;
	private final Map< Map<String, Map<MappingNames, Map<String, String>>>, Set<String>> updatedTypeIV = new HashMap<>();

	public TraceExplorer(boolean initWasSet, MappingFactoryInterface mappingFactory) {
		this.initWasSet = initWasSet;
		this.mappingFactory = mappingFactory;
	}

	/**
	 * Gets a list of transition and a variable mapping and applies the mapping to all transitions in the list
	 * @param mapping the mapping to be applied
	 * @param transitionList the list to apply to
	 * @return the changed transition list
	 */
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

	/**
	 * Removes the Enum Constans that were used to reduce the overhead for calculations by splitting the work in smaller pieces
	 * @param mappings the mapping to clean up
	 * @return the cleansed mapping
	 */
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

	/**
	 * Creates a new persistent transition with the help of two transition
	 * @param oldTransition the last seen transition or null when none was last
	 * @param newTransition the current transition from which the persistent transition should be created from
	 * @return the persistent transition
	 */
	public static PersistentTransition generateNewTransition(Transition oldTransition, Transition newTransition) {
		if (oldTransition == null) {
			return new PersistentTransition(newTransition, null);
		} else {
			return new PersistentTransition(newTransition, new PersistentTransition(oldTransition, null));
		}
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
	 * Creates a PersistentTransition with an existing mapping
	 *
	 * @param mapping the mapping to create the PT from
	 * @param current the current (old) transition
	 * @return the new transition
	 */
	public static PersistentTransition createPersistentTransitionFromMapping(Map<MappingNames, Map<String, String>> mapping,
																			 PersistentTransition current) {

		Map<String, String> destChangedVariables =
				mapping.get(MappingNames.VARIABLES_MODIFIED).entrySet().stream()
						.filter(entry -> current.getDestinationStateVariables().containsKey(entry.getKey()))
						.collect(toMap(Map.Entry::getValue, entry -> current.getDestinationStateVariables().get(entry.getKey())));


		Set<String> destNotChangedVariables =
				mapping.get(MappingNames.VARIABLES_READ).entrySet().stream()
						.filter(entry -> current.getDestStateNotChanged().contains(entry.getKey()))
						.map(Map.Entry::getValue).collect(toSet());


		Map<String, String> resultOutputParameters =
				mapping.get(MappingNames.OUTPUT_PARAMETERS).entrySet().stream()
						.collect(toMap(Map.Entry::getValue, entry -> current.getOutputParameters().get(entry.getKey())));


		Map<String, String> resultInputParameters =
				mapping.get(MappingNames.INPUT_PARAMETERS).entrySet().stream()
						.collect(toMap(Map.Entry::getValue, entry -> current.getParameters().get(entry.getKey())));


		return current.copyWithNewDestState(destChangedVariables).copyWithNewParameters(resultInputParameters)
				.copyWithNewOutputParameters(resultOutputParameters).copyWithDestStateNotChanged(destNotChangedVariables);
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

	public static ConstructTraceCommand buildTransitions(String name, Trace t, PersistentTransition current) {
		return new ConstructTraceCommand(
				t.getStateSpace(),
				t.getCurrentState(),
				Arrays.asList(name, current.getOperationName()),
				Arrays.asList(
						new ClassicalB("1=1", FormulaExpand.EXPAND),
						new ClassicalB(new PredicateBuilderFactory().createPredicateBuilder(current).toString(), FormulaExpand.EXPAND)));
	}

	/**
	 * It can have multiple causes that a transition is not active as expected:
	 * 1) The operation with this name does not longer exists
	 * 2) The operation with this name is guarded behind an other operation
	 * Either way we need to find our "way" to this transition - this is the way
	 * In order to archive this goal we first "skipping" a step and see if the transition is enabled, else we assume
	 * the transition is renamed and execute the transition which will satisfy our predicates
	 *
	 * @param t              the trace
	 * @param transition     the transition we are looking for
	 * @return a list of transition showing the way
	 */
	private static List<Transition> findPath(Trace t, PersistentTransition transition) {

		List<String> possibleTransitions = enabledOperations(t);
		if(possibleTransitions.contains(transition.getOperationName())){
			return replayTransition(t, transition.getOperationName(), new PredicateBuilder());
		}

		List<Transition> lookAheadResult = lookAhead(t, transition);

		if (!lookAheadResult.isEmpty()) {
			return lookAheadResult;
		}

		return renamedTransition(t, transition);

	}



	/**
	 * Skips a step and look if the current transition is enabled afterwards
	 *
	 * @param t              the trace
	 * @param current        the current transition
	 * @return a list of transitions that can be taken to skip the transition
	 */
	public static List<Transition> lookAhead(Trace t, PersistentTransition current) {
		List<String> possibleTransitions = enabledOperations(t);

		List<ConstructTraceCommand> commands = possibleTransitions
				.stream()
				.map(name -> buildTransitions(name, t, current))
				.collect(toList());

		StateSpace stateSpace = t.getStateSpace();

		for (ConstructTraceCommand command : commands) {
			stateSpace.execute(command);
			if (!command.getNewTransitions().isEmpty() && command.getErrors().isEmpty()) {
				return command.getNewTransitions();
			}
		}

		return emptyList();
	}

	/**
	 * Returns a list with all transitions enabled
	 *
	 * @param t the trace
	 * @return return a list with all enabled transitions by name
	 */
	public static List<String> enabledOperations(Trace t) {
		return t.getCurrentState().getOutTransitions()
				.stream()
				.collect(toCollection(() -> new TreeSet<>(Comparator.comparing(Transition::getName))))
				.stream()
				.map(Transition::getName)
				.collect(toList());
	}


	/**
	 * The current operation cannot be found, it seems like it was renamed or removed, lets try to execute operations
	 * and hope we can satisfy the original predicates dealing with variables
	 *
	 * @param t              the trace
	 * @param current        the current transition
	 * @return a list of transitions that satisfy the privilige
	 */
	public static List<Transition> renamedTransition(Trace t, PersistentTransition current) {
		List<String> enabledOperations = enabledOperations(t);

		List<List<Transition>> result = enabledOperations.stream()
				.map(entry -> replayTransition(t, entry, new PredicateBuilderFactory(true, false, false, emptySet())
						.createPredicateBuilder(current)))
				.filter(entry -> !entry.isEmpty())
				.collect(toList());


		return firstOrEmpty(result);
	}

	/**
	 * Attempts to replay the transition with the given name
	 *
	 * @param t                the trace
	 * @param name             the name of the transition to be replayed
	 * @param predicateBuilder the predicates to be enforced onto the the replay
	 * @return a successful transition
	 */
	public static List<Transition> replayTransition(Trace t, String name, PredicateBuilder predicateBuilder) {

		StateSpace stateSpace = t.getStateSpace();

		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		GetOperationByPredicateCommand command =
				new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(), name, pred, 1);

		stateSpace.execute(command);

		return command.getNewTransitions();
	}

	/**
	 * Gets a trace and the operation information from new and old machine. for every operation used in the trace and
	 * destined as type III this method will calculate all possible coconstellationsf the identifiers used in the operation
	 * and then build the co product over all operations
	 * @param transitionList the trace
	 * @param operationInfoNew the infos from the current machine
	 * @param operationInfoOld the infos from the machine which generated the trace
	 * @param typeIIICandidates the operations filterd as type 3
	 * @return the co product from all operations
	 */
	public Set<Map<String, Map<MappingNames, Map<String, String>>>> generateAllPossibleMappingVariations(
			List<PersistentTransition> transitionList,
			Map<String, OperationInfo> operationInfoNew,
			Map<String, OperationInfo> operationInfoOld,
			Set<String> typeIIICandidates) {

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
				.map(transition -> calculateVarMappings(transition.getOperationName(), operationInfoNew.get(transition.getOperationName()), operationInfoOld.get(transition.getOperationName()))
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
	 * Gets identifiers an their types for a certain section of the operation represented as current mapping.
	 * calculates all possible possible (type is correct) constellations from old to new
	 * @param newTypes the current identifiers with types
	 * @param oldTypes the old identifiers with types
	 * @param currentMapping the section under work
	 * @param name the name of the operation under work
	 * @return all possible mappings for this section
	 */
	public Set<Map<String, String>> createAllPossiblePairs(Map<String, String> newTypes, Map<String, String> oldTypes,
														   MappingNames currentMapping,
														   String name) {


		if(oldTypes.isEmpty() || newTypes.isEmpty()) return emptySet();

		Map<String, List<String>> oldTypesSorted = sortByValue(oldTypes);
		Map<String, List<String>> newTypesSorted = sortByValue(newTypes);

		Map<String, Integer> minMap = oldTypesSorted.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getKey, entry -> Math.min(entry.getValue().size(), newTypesSorted.getOrDefault(entry.getKey(), emptyList()).size())));


		Map<String, Integer> manualCandidates = minMap.entrySet()
				.stream()
				.filter(entry -> entry.getValue() > 9)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		Map<String, Set<Map<String, String>>> manualCandidatesDecided = manualCandidates.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getKey, entry -> {
					String key = entry.getKey();
					Map<String, String> result = mappingFactory.produceMappingManager().askForMapping(oldTypesSorted.get(key), newTypesSorted.get(key), name, currentMapping);
					HashSet<Map<String, String>> set = new HashSet<>();
					set.add(result);
					return set;
				}));

		Map<String, List<String>> oldTypesSortedAutomatic = removeKeys(oldTypesSorted, manualCandidates.keySet());
		Map<String, List<String>> newTypesSortedAutomatic = removeKeys(newTypesSorted, manualCandidates.keySet());
		
		Map<String, List<String>> newSortedCleansed = cleanse(newTypesSorted, oldTypesSortedAutomatic.keySet());
		Map<String, List<String>> oldSortedCleansed = cleanse(oldTypesSorted, newTypesSortedAutomatic.keySet());
		
		Map<String, List<List<String>>> oldPermuted = permutedMap(oldSortedCleansed, minMap);
		Map<String, List<List<String>>> newPermuted = permutedMap(newSortedCleansed, minMap);

		Map<String, Set<Map<String, String>>> molten = melt(oldPermuted, newPermuted);
		molten.putAll(manualCandidatesDecided);

		return reduceSet(molten);
	}

	public static Map<String, List<String>> removeKeys(Map<String, List<String>> map, Set<String> set){
		return map.entrySet().stream().filter(entry -> !set.contains(entry.getKey())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Removes all entries from the map not given in the set
	 * @param map the map to cleanse
	 * @param key the set to lookup
	 * @return the cleansed map
	 */
	public static Map<String, List<String>> cleanse(Map<String, List<String>> map, Set<String> key){
		return map.entrySet()
				.stream()
				.filter(entry -> key.contains(entry.getKey()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Removes all entries from the map not given in the set
	 * @param map the map to cleanse
	 * @param key the set to lookup
	 * @return the cleansed map
	 */
	public static Map<String, String> cleansePlain(Map<String, String> map, Set<String> key){
		return map.entrySet()
				.stream()
				.filter(entry -> key.contains(entry.getKey()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Gets a map containing a set of mappings and unifies these set of mappings to one map
	 * @param map the map to reduce
	 * @return the reduced map
	 */
	public static Set<Map<String, String>> reduceSet(Map<String, Set<Map<String, String>>> map){
		return map.values()
				.stream()
				.reduce(emptySet(), TraceExplorer::applyProduct);
	}

	/**
	 * Gets to sets of maps and creates the co product of both together with @see reduceSet a x b x c ....
	 * @param current the current set
	 * @param acc the already evaluated sets
	 * @return the coproduct
	 */
	public static Set<Map<String, String>> applyProduct(Set<Map<String, String>> acc, Set<Map<String, String>> current) {
		if(acc.isEmpty() ) return current;
		return current.stream().flatMap(currentEntry -> acc.stream()
				.map(accEntry -> {
					Map<String, String> result = new HashMap<>();
					result.putAll(accEntry);
					result.putAll(currentEntry);
					return result;
				}))
				.collect(toSet());
	}
	
	/**
	 * create the coproduct over the "vectors" of lists
	 * @param permutationsOld the first "vector"
	 * @param permutationsNew the second "vector"
	 * @return the coproduct
	 */
	public static Set<Map<String, String>> productCombination(List<List<String>> permutationsOld, List<List<String>> permutationsNew){
		return permutationsOld.stream().flatMap(permutationOld -> permutationsNew.stream().map(permutationNew ->
				TraceCheckerUtils.zip(permutationOld, permutationNew))).collect(toSet());
	}

	/**
	 * Gets two maps of equal making and unifies them
	 * @param oldMap the first map
	 * @param newMap the second map
	 * @return the unified map containing a mapping from first to second value
	 */
	public static Map<String, Set<Map<String, String>>> melt(Map<String, List<List<String>>> oldMap, Map<String, List<List<String>>> newMap){
		return oldMap.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getKey, entry -> productCombination(entry.getValue(), newMap.getOrDefault(entry.getKey(), emptyList()))));
	}

	/**
	 * Gets a list which is mapping data types to identifiers and a map mapping data types to the maximum length they are permuted to
	 * @param map the map where the entries have to be permuted
	 * @param maxsize the max size for each entry
	 * @return a map which contains all permutations of the given length per entry
	 */
	public static Map<String, List<List<String>>> permutedMap(Map<String, List<String>> map, Map<String, Integer> maxsize){
		return map.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> TraceCheckerUtils.generatePerm(new ArrayList<>(entry.getValue()), 0, maxsize.get(entry.getKey()), emptyList())));
	}

	/**
	 * Gets a map and reverses keys and values
	 * @param map the input map, mapping identifiers to types
	 * @return the output mapping a type to a list of identifiers
	 */
	public static Map<String, List<String>> sortByValue(Map<String, String> map){
		Set<String> newKeys = new HashSet<>(map.values());
		return newKeys.stream()
				.collect(toMap(entry -> entry, entry -> map.entrySet()
				.stream()
				.filter(innerEntry -> innerEntry.getValue().equals(entry))
				.map(Map.Entry::getKey)
				.collect(toList())));
	}


	/**
	 * Gets two versions of the same operation a and b. a is from the machine that created the trace file b is from the
	 * current machine (old and new). Create a mapping of all identifiers in a to b. Will dismiss all identifiers with no
	 * counterpart.
	 *
	 * First the operation infos will be divided in their four main parts to reduce overhead, for each part the
	 * identifier constellations will be calculated independent and the results are then apllied to each other
	 *
	 * @param name the name of the operation
	 * @param operationMappingNew the operation infos of the currently loaded machine
	 * @param operationMappingOld the infos from the trace file
	 * @return a set containing all possible mappings for each section of the machine
	 */
	public Set<Map<MappingNames, Map<String, String>>> calculateVarMappings(String name,
																			OperationInfo operationMappingNew,
																			OperationInfo operationMappingOld) {

		Map<MappingNames, List<String>> newOperationInfos = fillMapping(operationMappingNew);
		Map<MappingNames, List<String>> oldOperationInfos = fillMapping(operationMappingOld);
				
		HashMap<MappingNames, Map<String, String>> mappingsHelper = new HashMap<>();

		mappingsHelper.put(MappingNames.VARIABLES_MODIFIED, emptyMap());
		mappingsHelper.put(MappingNames.VARIABLES_READ, emptyMap());
		mappingsHelper.put(MappingNames.INPUT_PARAMETERS, emptyMap());
		mappingsHelper.put(MappingNames.OUTPUT_PARAMETERS, emptyMap());

		Set<Map<MappingNames, Map<String, String>>> mappings = new HashSet<>();
		mappings.add(mappingsHelper);

		for (MappingNames mappingName : MappingNames.values()) {

			Set<Map<MappingNames, Map<String, String>>> mappingsCopy = mappings;

			Map<String, String> preparedMapOld = cleansePlain(operationMappingOld.getTypeMap(), new HashSet<>(oldOperationInfos.get(mappingName)));
			Map<String, String> preparedMapNew = cleansePlain(operationMappingNew.getTypeMap(), new HashSet<>(newOperationInfos.get(mappingName)));

			
			Set<Map<MappingNames, Map<String, String>>> mappingsAppliedToExistingCopy =
					createAllPossiblePairs(preparedMapNew, preparedMapOld, mappingName, name)
							.stream()
							.flatMap(possiblePair -> mappingsCopy.stream().map(mapping -> {
								Map<MappingNames, Map<String, String>> alteredInnerMapping = new HashMap<>(mapping);
								alteredInnerMapping.put(mappingName, possiblePair);
								return alteredInnerMapping;
							}))
							.collect(toSet());
			if (!mappingsAppliedToExistingCopy.isEmpty()) {
				mappings = mappingsAppliedToExistingCopy;
			}
		}
		
		return mappings;
	}

	/**
	 * helper for @see caluclateVarMappings, splits a operation info in four parts
	 * @param info the operation info to be splitted
	 * @return the splitted operation info
	 */
	public static Map<MappingNames, List<String>> fillMapping(OperationInfo info){
		Map<MappingNames, List<String>> operationInfos = new HashMap<>();
		ArrayList<String> variablesModified = new ArrayList<>(info.getNonDetWrittenVariables());
		variablesModified.addAll(info.getWrittenVariables());
		operationInfos.put(MappingNames.VARIABLES_MODIFIED, variablesModified);
		operationInfos.put(MappingNames.VARIABLES_READ, new ArrayList<>(info.getReadVariables()));
		operationInfos.put(MappingNames.INPUT_PARAMETERS, new ArrayList<>(info.getParameterNames()));
		operationInfos.put(MappingNames.OUTPUT_PARAMETERS, new ArrayList<>(info.getOutputParameterNames()));
		return operationInfos;

	}

	/**
	 * The main logic component of this class. Gets a trace and information extracted previously and tries to run the
	 * trace under the current conditions. Problems will be caught and "fixed" on the fly if sensebill and logical.
	 * @param transitionList the trace to be replayed
	 * @param stateSpace the statesman where the current machine lives in
	 * @param operationInfoNew the infos of the current machine
	 * @param operationInfoOld the infos of the machine that created the trace
	 * @param typeIIICandidates candidates where the signature might have changed
	 * @param typeIVCandidates candidates that may not accessible, removed, or renamed...
	 * @return the replayed trace in dependence to the identifier selection regariding typeIIICandidates
	 */
	public Map<Map<String, Map<MappingNames, Map<String, String>>>, List<PersistenceDelta>> replayTrace(List<PersistentTransition> transitionList,
																										StateSpace stateSpace,
																										Map<String, OperationInfo> operationInfoNew,
																										Map<String, OperationInfo> operationInfoOld,
																										Set<String> typeIIICandidates,
																										Set<String> typeIVCandidates) {

		Trace trace = new Trace(stateSpace);
		trace.setExploreStateByDefault(true);

		Set<Map<String, Map<MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				generateAllPossibleMappingVariations(transitionList, operationInfoNew, operationInfoOld, typeIIICandidates);

		Map<Map<String, Map<MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = selectedMappingsToResultsKeys.stream()
				.map(mapping -> {
					List<PersistentTransition> newTransitions = transformTransitionList(mapping, transitionList);
					List<PersistenceDelta> preparedDelta = TraceCheckerUtils.zipPreserveOrder(transitionList, newTransitions)
							.entrySet().stream()
							.map(entry -> new PersistenceDelta(entry.getKey(), singletonList(entry.getValue())))
							.collect(toList());
					return new AbstractMap.SimpleEntry<>(mapping, createNewTransitionList(preparedDelta, trace, typeIVCandidates, mapping));
				})
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


		return result;
	}


	/**
	 * Gets a prepared transition list and tries to replay the trace. Eventual problems are caught and it will be tried
	 * to find a new path to replay the trace with search depth 1. Errors when replaying are caught and a empty list of
	 * as new transition lists is returned
	 * @param persistentTransitions the prepared transition list
	 * @param t the initial state of the loaded machine
	 * @param typeIV all operations that are identified as type IV
	 * @param currentTypeIIIMapping the currently used variable mapping
	 * @return the new transition list either containing a correct pathing or is empty when no pathing was found
	 */
	public List<PersistenceDelta> createNewTransitionList(List<PersistenceDelta> persistentTransitions, Trace t, Set<String> typeIV, Map<String, Map<MappingNames, Map<String, String>>> currentTypeIIIMapping){
		Trace currentState = t;
		List<PersistenceDelta> newTransitions = new ArrayList<>();


		Set<String> usedTypeIV = new HashSet<>(typeIV);
		for (PersistenceDelta oldTPersistentTransition : persistentTransitions) {
			boolean isDirty = false;
			PersistentTransition oldPTransition = oldTPersistentTransition.getOldTransition();
			if (!usedTypeIV.contains(oldPTransition.getOperationName())) {
				try {
					Transition oldTransition = currentState.getCurrentTransition();
					Transition newTransition = replayPersistentTransition(currentState, oldTPersistentTransition.getNewTransitions().get(0));
					currentState = currentState.add(newTransition);
					PersistentTransition newPersistentTransition = generateNewTransition(oldTransition, newTransition);
					newTransitions.add(new PersistenceDelta(oldTPersistentTransition.getOldTransition(), singletonList(newPersistentTransition)));

				} catch (TransitionHasNoSuccessorException ignored) {
					isDirty = true;
					//return emptyList();
				}
			}
			if(usedTypeIV.contains(oldPTransition.getOperationName()) || isDirty){
				List<Transition> result = findPath(currentState, oldPTransition);
				if(result.isEmpty()) {
					//return newTransitions;
					return emptyList();
				}else {
					currentState = currentState.addTransitions(result);
					newTransitions.add(new PersistenceDelta(oldPTransition, PersistentTransition.createFromList(result, currentState.getCurrentTransition())));
					usedTypeIV.add(oldPTransition.getOperationName()); //Careful! pass by reference, the global state is a reference to the passed parameter in the top function, better rework this
				}

			}

		}
		updatedTypeIV.put(currentTypeIIIMapping, usedTypeIV);

		return newTransitions;
	}


	//Evtl. Return map<PersistentTransition, Transition>
	private Transition replayPersistentTransition(Trace t, PersistentTransition persistentTransition) throws TransitionHasNoSuccessorException {

		StateSpace stateSpace = t.getStateSpace();

		final GetOperationByPredicateCommand command;

		if (persistentTransition.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME)) {
			command = buildInit(stateSpace, persistentTransition, t);
		} else if (persistentTransition.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME)) {
			command = buildSC(stateSpace, persistentTransition, t);
		} else {
			command = buildTransition(stateSpace, persistentTransition, t);
		}

		Map<String, OperationInfo> ga = stateSpace.getLoadedMachine().getOperations();
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

	public static GetOperationByPredicateCommand buildSC(StateSpace stateSpace, PersistentTransition persistentTransition,
												  Trace t) {

		PredicateBuilder predicateBuilder = new PredicateBuilder();

		predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());

		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		return new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(),
				persistentTransition.getOperationName(), pred, 1);
	}


	/**
	 * An helper datatype to better split operation infos
	 */
	public enum MappingNames {
		INPUT_PARAMETERS, OUTPUT_PARAMETERS, VARIABLES_MODIFIED, VARIABLES_READ
	}



	public Map<Map<String, Map<MappingNames, Map<String, String>>>, Set<String>> getUpdatedTypeIV() {
		return updatedTypeIV;
	}

}
