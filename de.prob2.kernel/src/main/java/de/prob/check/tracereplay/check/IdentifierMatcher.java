package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.ui.MappingFactoryInterface;
import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.Transition;

import java.util.*;

import static de.prob.check.tracereplay.check.TraceCheckerUtils.product;
import static java.util.Collections.*;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.*;

public class IdentifierMatcher {




	/**
	 * Gets a trace and the operation information from new and old machine. for every operation used in the trace and
	 * destined as type III this method will calculate all possible coconstellationsf the identifiers used in the operation
	 * and then build the co product over all operations
	 *
	 * @param transitionList    the trace
	 * @param operationInfoNew  the infos from the current machine
	 * @param operationInfoOld  the infos from the machine which generated the trace
	 * @param typeIIICandidates the operations filterd as type 3
	 * @return the co product from all operations
	 */
	public static Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> generateAllPossibleMappingVariations(
			List<PersistentTransition> transitionList,
			Map<String, OperationInfo> operationInfoNew,
			Map<String, OperationInfo> operationInfoOld,
			Set<String> typeIIICandidates,
			MappingFactoryInterface mappingFactory) {

		if (typeIIICandidates.isEmpty()) {
			Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> result = new HashSet<>();
			result.add(emptyMap());
			return result;
		}

		List<PersistentTransition> selectionOfTypeIIITransitions = new ArrayList<>(transitionList
				.stream()
				.filter(transition -> !transition.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME))
				.filter(transition -> !transition.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME))
				.filter(transition -> typeIIICandidates.contains(transition.getOperationName()))
				.collect(toCollection(() -> new TreeSet<>(Comparator.comparing(PersistentTransition::getOperationName)))));


		List<List<HashMap<String, Map<TraceExplorer.MappingNames, Map<String, String>>>>> listOfMappings = selectionOfTypeIIITransitions
				.stream()
				.map(transition -> calculateVarMappings(transition.getOperationName(), operationInfoNew.get(transition.getOperationName()), operationInfoOld.get(transition.getOperationName()), mappingFactory)
						.stream()
						.map(mapping -> {
							HashMap<String, Map<TraceExplorer.MappingNames, Map<String, String>>> result = new HashMap<>();
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
	 * Gets two versions of the same operation a and b. a is from the machine that created the trace file b is from the
	 * current machine (old and new). Create a mapping of all identifiers in a to b. Will dismiss all identifiers with no
	 * counterpart.
	 *
	 * First the operation infos will be divided in their four main parts to reduce overhead, for each part the
	 * identifier constellations will be calculated independent and the results are then apllied to each other
	 *
	 * @param name                the name of the operation
	 * @param operationMappingNew the operation infos of the currently loaded machine
	 * @param operationMappingOld the infos from the trace file
	 * @return a set containing all possible mappings for each section of the machine
	 */
	public static Set<Map<TraceExplorer.MappingNames, Map<String, String>>> calculateVarMappings(String name,
																								 OperationInfo operationMappingNew,
																								 OperationInfo operationMappingOld,
																								 MappingFactoryInterface mappingFactory) {

		Map<TraceExplorer.MappingNames, List<String>> newOperationInfos = fillMapping(operationMappingNew);
		Map<TraceExplorer.MappingNames, List<String>> oldOperationInfos = fillMapping(operationMappingOld);

		HashMap<TraceExplorer.MappingNames, Map<String, String>> mappingsHelper = new HashMap<>();

		mappingsHelper.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, emptyMap());
		mappingsHelper.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());
		mappingsHelper.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, emptyMap());
		mappingsHelper.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> mappings = new HashSet<>();
		mappings.add(mappingsHelper);

		for (TraceExplorer.MappingNames mappingName : TraceExplorer.MappingNames.values()) {

			Set<Map<TraceExplorer.MappingNames, Map<String, String>>> mappingsCopy = mappings;

			Map<String, String> preparedMapOld = cleansePlain(operationMappingOld.getTypeMap(), new HashSet<>(oldOperationInfos.get(mappingName)));
			Map<String, String> preparedMapNew = cleansePlain(operationMappingNew.getTypeMap(), new HashSet<>(newOperationInfos.get(mappingName)));


			Set<Map<TraceExplorer.MappingNames, Map<String, String>>> mappingsAppliedToExistingCopy =
					createAllPossiblePairs(preparedMapNew, preparedMapOld, mappingName, name, mappingFactory)
							.stream()
							.flatMap(possiblePair -> mappingsCopy.stream().map(mapping -> {
								Map<TraceExplorer.MappingNames, Map<String, String>> alteredInnerMapping = new HashMap<>(mapping);
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
	 *
	 * @param info the operation info to be splitted
	 * @return the splitted operation info
	 */
	public static Map<TraceExplorer.MappingNames, List<String>> fillMapping(OperationInfo info) {
		Map<TraceExplorer.MappingNames, List<String>> operationInfos = new HashMap<>();
		ArrayList<String> variablesModified = new ArrayList<>(info.getNonDetWrittenVariables());
		variablesModified.addAll(info.getWrittenVariables());
		operationInfos.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, variablesModified);
		operationInfos.put(TraceExplorer.MappingNames.VARIABLES_READ, new ArrayList<>(info.getReadVariables()));
		operationInfos.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, new ArrayList<>(info.getParameterNames()));
		operationInfos.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, new ArrayList<>(info.getOutputParameterNames()));
		return operationInfos;

	}

	/**
	 * Removes all entries from the map not given in the set
	 *
	 * @param map the map to cleanse
	 * @param key the set to lookup
	 * @return the cleansed map
	 */
	public static Map<String, String> cleansePlain(Map<String, String> map, Set<String> key) {
		return map.entrySet()
				.stream()
				.filter(entry -> key.contains(entry.getKey()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}


	/**
	 * Gets identifiers an their types for a certain section of the operation represented as current mapping.
	 * calculates all possible possible (type is correct) constellations from old to new
	 *
	 * @param newTypes       the current identifiers with types
	 * @param oldTypes       the old identifiers with types
	 * @param currentMapping the section under work
	 * @param name           the name of the operation under work
	 * @return all possible mappings for this section
	 */
	public static Set<Map<String, String>> createAllPossiblePairs(Map<String, String> newTypes, Map<String, String> oldTypes,
																  TraceExplorer.MappingNames currentMapping,
																  String name, MappingFactoryInterface mappingFactory) {


		if (oldTypes.isEmpty() || newTypes.isEmpty()) return emptySet();


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

		Map<String, Set<Map<String, String>>> allPairs = newSortedCleansed.entrySet().stream()
				.map(entry -> {
					List<String> oldSorted = oldSortedCleansed.get(entry.getKey());
					List<String> newSorted = entry.getValue();
					if(oldSorted.containsAll(newSorted) && newSorted.containsAll(oldSorted)){
						return new AbstractMap.SimpleEntry<>(entry.getKey(), singleton(createIdentity(oldSorted))); //Shortcut if there are no apparent renmaings
					}else {
						return new AbstractMap.SimpleEntry<>(entry.getKey(), TraceCheckerUtils.allDiagonals(oldSortedCleansed.get(entry.getKey()), entry.getValue()));
					}
				})
				.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

		allPairs.putAll(manualCandidatesDecided);

		return reduceSet(allPairs);
	}

	/**
	 * Gets a map and reverses keys and values
	 *
	 * @param map the input map, mapping identifiers to types
	 * @return the output mapping a type to a list of identifiers
	 */
	public static Map<String, List<String>> sortByValue(Map<String, String> map) {
		Set<String> newKeys = new HashSet<>(map.values());
		return newKeys.stream()
				.collect(toMap(entry -> entry, entry -> map.entrySet()
						.stream()
						.filter(innerEntry -> innerEntry.getValue().equals(entry))
						.map(Map.Entry::getKey)
						.collect(toList())));
	}


	/**
	 * Gets a map containing a set of mappings and unifies these set of mappings to one map
	 *
	 * @param map the map to reduce
	 * @return the reduced map
	 */
	public static Set<Map<String, String>> reduceSet(Map<String, Set<Map<String, String>>> map) {
		return map.values()
				.stream()
				.reduce(emptySet(), IdentifierMatcher::applyProduct);
	}


	public static Map<String, List<String>> removeKeys(Map<String, List<String>> map, Set<String> set) {
		return map.entrySet().stream().filter(entry -> !set.contains(entry.getKey())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Removes all entries from the map not given in the set
	 *
	 * @param map the map to cleanse
	 * @param key the set to lookup
	 * @return the cleansed map
	 */
	public static Map<String, List<String>> cleanse(Map<String, List<String>> map, Set<String> key) {
		return map.entrySet()
				.stream()
				.filter(entry -> key.contains(entry.getKey()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}




	/**
	 * Gets to sets of maps and creates the co product of both together with @see reduceSet a x b x c ....
	 *
	 * @param current the current set
	 * @param acc     the already evaluated sets
	 * @return the coproduct
	 */
	public static Set<Map<String, String>> applyProduct(Set<Map<String, String>> acc, Set<Map<String, String>> current) {
		if (acc.isEmpty()) return current;
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
	 *
	 * @param permutationsOld the first "vector"
	 * @param permutationsNew the second "vector"
	 * @return the coproduct
	 */
	public static Set<Map<String, String>> productCombination(List<List<String>> permutationsOld, List<List<String>> permutationsNew) {
		return permutationsOld.stream().flatMap(permutationOld -> permutationsNew.stream().map(permutationNew ->
				TraceCheckerUtils.zip(permutationOld, permutationNew))).collect(toSet());
	}

	/**
	 * Gets two maps of equal making and unifies them
	 *
	 * @param oldMap the first map
	 * @param newMap the second map
	 * @return the unified map containing a mapping from first to second value
	 */
	public static Map<String, Set<Map<String, String>>> melt(Map<String, List<List<String>>> oldMap, Map<String, List<List<String>>> newMap) {
		return oldMap.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getKey, entry -> productCombination(entry.getValue(), newMap.getOrDefault(entry.getKey(), emptyList()))));
	}



	/**
	 * @param oldTypes the mapping of the old machine
	 * @return return the keys of both maps mapped to each other
	 */
	public static Map<String, String> createIdentity(List<String> oldTypes){
		return oldTypes.stream().collect(toMap(entry -> entry, entry -> entry));
	}

	/**
	 * @param oldTypes the mapping of the old machine
	 * @return return the keys of both maps mapped to each other
	 */
	public static Map<String, String> createIdentity(Map<String, String> oldTypes){
		return oldTypes.entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getKey));
	}

}
