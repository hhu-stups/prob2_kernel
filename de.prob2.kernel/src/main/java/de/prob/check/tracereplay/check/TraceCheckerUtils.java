package de.prob.check.tracereplay.check;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class TraceCheckerUtils {

	/**
	 * Zips two lists to a map - return an empty list if list have different sizes, naturally equal elements will override
	 * each other; orders will not preserved
	 * @param list1 the first list
	 * @param list2 the second list
	 * @param <T> Type of the first List
	 * @param <U> Type of the second list
	 * @return a Map of the Type {@code <T, U>}
	 */
	public static <T, U> Map<T, U> zip(List<T> list1, List<U> list2){
		Map<T, U> sideResult = new HashMap<>();
		if(list1.size() <= list2.size()){
			for(int i = 0;  i < list1.size(); i++){
				sideResult.put(list1.get(i), list2.get(i));
			}
		}else {

			for(int i = 0;  i < list2.size(); i++){
				sideResult.put(list1.get(i), list2.get(i));
			}
		}
		return sideResult;
	}


	/**
	 * Zips two lists to a map - return an empty list if list have different sizes, naturally equal elements will override
	 * each other
	 * @param list1 the first list
	 * @param list2 the second list
	 * @param <T> Type of the first List
	 * @param <U> Type of the second list
	 * @return a Map of the Type {@code <T, U>}
	 */
	public static <T, U> LinkedHashMap<T, U> zipPreserveOrder(List<T> list1, List<U> list2){
		LinkedHashMap<T, U> sideResult = new LinkedHashMap<>();
		if(list1.size() <= list2.size()){
			for(int i = 0;  i < list1.size(); i++){
				sideResult.put(list1.get(i), list2.get(i));
			}
		}else {

			for(int i = 0;  i < list2.size(); i++){
				sideResult.put(list1.get(i), list2.get(i));
			}
		}
		return sideResult;
	}


	private static ReusableAnimator reusableAnimator;
	public static ReusableAnimator getReusableAnimator(Injector injector){
		if (reusableAnimator==null){
			reusableAnimator = injector.getInstance(ReusableAnimator.class);
		}
		return reusableAnimator;
	}

	public static StateSpace createStateSpace(String path, Injector injector) throws IOException {
		ReusableAnimator animator = getReusableAnimator(injector);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(path.substring(path.lastIndexOf(".")+1)));
		if(animator.getCurrentStateSpace()!=null)
		{
			animator.getCurrentStateSpace().kill();
		}
		StateSpace stateSpace = animator.createStateSpace();
		factory.extract(path).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}


	/**
	 * Returns the operations actually used by the trace
	 * @param transitionList the trace to analyse
	 * @return a set of operations used in the trace
	 */
	public static Set<String> usedOperations(List<PersistentTransition> transitionList){
		return transitionList.stream().map(PersistentTransition::getOperationName).collect(Collectors.toSet());
	}



	/**
	 * Removes all non Op clauses from a transition List
	 * @param transitionList the transition list
	 * @return the stripped list
	 */
	public static List<PersistentTransition> stripNonOpClause(List<PersistentTransition> transitionList){
		return transitionList.stream()
				.filter(element -> !element.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME))
				.filter(element -> !element.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME))
				.filter(element -> !element.getOperationName().equals(Transition.PARTIAL_SETUP_CONSTANTS_NAME))
				.collect(Collectors.toList());
	}

	/**
	 * Wild cards are those where in the old operation the identifier did not exist - those don't show up in the mapping
	 * @param newInfo the operation infos from the new operation
	 * @param currentMappings the current mapping old -&gt; new
	 */
	public static Set<String> calculateAllWildCards(OperationInfo newInfo, Map<String, String> currentMappings){
		Set<String> result = newInfo.getAllIdentifier();
		result.removeAll(currentMappings.values());
		return result;
	}


	/**
	 * Void cards are those where in the new operation the identifier did not exist - those don't show up in the mapping
	 * @param oldInfo the operation infos from the old operation
	 * @param currentMappings the current mapping old -&gt; new
	 */
	public static Set<String> calculateAllVoidCards(OperationInfo oldInfo, Map<String, String> currentMappings){
		Set<String> result = oldInfo.getAllIdentifier();
		result.removeAll(currentMappings.keySet());
		return result;
	}


	/**
	 * Wild cards are those where in the old operation the identifier did not exist - those don't show up in the mapping
	 * @param newInfo the operation infos from the new operation
	 * @param currentMappings the current mapping old -&gt; new
	 */
	public static Set<String> calculateVarWildCards(OperationInfo newInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(newInfo.getAllVariables());
		result.removeAll(currentMappings.values());
		return result;
	}


	/**
	 * Void cards are those where in the new operation the identifier did not exist - those don't show up in the mapping
	 * @param oldInfo the operation infos from the old operation
	 * @param currentMappings the current mapping old -&gt; new
	 */
	public static Set<String> calculateVarVoidCards(OperationInfo oldInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(oldInfo.getAllVariables());
		result.removeAll(currentMappings.keySet());
		return result;
	}


	/**
	 * Wild cards are those where in the old operation the identifier did not exist - those don't show up in the mapping
	 * @param newInfo the operation infos from the new operation
	 * @param currentMappings the current mapping old -&gt; new
	 */
	public static Set<String> calculateInWildCards(OperationInfo newInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(newInfo.getParameterNames());
		result.removeAll(currentMappings.values());
		return result;
	}


	/**
	 * Void cards are those where in the new operation the identifier did not exist - those don't show up in the mapping
	 * @param oldInfo the operation infos from the old operation
	 * @param currentMappings the current mapping old -&gt; new
	 */
	public static Set<String> calculateInVoidCards(OperationInfo oldInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(oldInfo.getParameterNames());
		result.removeAll(currentMappings.keySet());
		return result;
	}


	/**
	 * Wild cards are those where in the old operation the identifier did not exist - those don't show up in the mapping
	 * @param newInfo the operation infos from the new operation
	 * @param currentMappings the current mapping old -&gt; new
	 */
	public static Set<String> calculateOutWildCards(OperationInfo newInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(newInfo.getOutputParameterNames());
		result.removeAll(currentMappings.values());
		return result;
	}


	/**
	 * Void cards are those where in the new operation the identifier did not exist - those don't show up in the mapping
	 * @param oldInfo the operation infos from the old operation
	 * @param currentMappings the current mapping old -&gt; new
	 */
	public static Set<String> calculateOutVoidCards(OperationInfo oldInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(oldInfo.getOutputParameterNames());
		result.removeAll(currentMappings.keySet());
		return result;
	}


	public static <U> List<U>  firstOrEmpty(List<List<U>> list){
		if(list.isEmpty()){
			return emptyList();
		}
		return list.get(0);
	}


	/**
	 * Extract diagonals and anti diagonals of a matrix, preserves order
	 * @param m the first vector
	 * @param n the second vector
	 * @return all diagonals mapping of the form m:n
	 */
	public static Set<Map<String, String>> allDiagonals(List<String> m, List<String> n){
		Set<Map<String, String>> first = extractDiagonals(m,n);
		Set<Map<String, String>> second = extractDiagonals(Lists.reverse(m), n);
		return Stream.of(first, second).flatMap(Collection::stream).collect(Collectors.toSet());

	}

	/**
	 * Extracts all diagonals of a matrix, preserves order
	 * @param m the first vector
	 * @param n the second vector
	 * @return all diagonals
	 */
	public static Set<Map<String, String>> extractDiagonals(List<String> m, List<String> n){

		int sizeM = m.size();
		int sizeN = n.size();
		int offSetX = 0;
		int offSetY = 0;
		Set<Map<String, String>> result = new HashSet<>();
		while( offSetX < sizeM && offSetY < sizeN) {
			Map<String, String> sideResult = new HashMap<>();

			int x = 0;
			int y = 0;
			while (x < sizeM && y < sizeN) {
				sideResult.put(m.get((x + offSetX)% sizeM), n.get((y +offSetY) %sizeN));
				x++;
				y++;

			}

			if(sizeM > sizeN){
				offSetX ++;
			}else{
				offSetY++;
			}
			result.add(sideResult);
		}

		return result;
	}


	/**
	 * Calculates the cartesian product for the special case of input
	 *
	 * @param a the first "vector"
	 * @param b the second "vector"
	 * @return the cartesian product
	 */
	@Deprecated
	public static List<HashMap<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> product(
			List<HashMap<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> a,
			List<HashMap<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> b) {
		return a.stream().flatMap(entryA -> b.stream().map(entryB -> {
			HashMap<String, Map<TraceExplorer.MappingNames, Map<String, String>>> result = Maps.newHashMap(entryA);
			result.putAll(entryB);
			return result;
		})).collect(toList());
	}


}
