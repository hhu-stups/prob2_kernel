package de.prob.check.tracereplay.check;

import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class TraceCheckerUtils {

	/**
	 * Zips two lists to a map - return an empty list if list have different sizes, naturally equal elements will override
	 * each other; orders will not preserved
	 * @param list1 the first list
	 * @param list2 the second list
	 * @param <T> Type of the first List
	 * @param <U> Type of the second list
	 * @return a Map of the Type <T, U>
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
	 * @return a Map of the Type <T, U>
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

	/**
	 * Generate a permutation of a list
	 * https://stackoverflow.com/questions/10305153/generating-all-possible-permutations-of-a-list-recursively
	 * @param original the input ist
	 * @param <E> the type of the permuation
	 * @return returns all permutations
	 */
	public static  <E> List<List<E>> generatePerm(List<E> original) {
		if (original.isEmpty()) {
			List<List<E>> result = new ArrayList<>();
			result.add(new ArrayList<>());
			return result;
		}
		E firstElement = original.remove(0);
		List<List<E>> returnValue = new ArrayList<>();
		List<List<E>> permutations = generatePerm(original);
		for (List<E> smallerPermutated : permutations) {
			for (int index=0; index <= smallerPermutated.size(); index++) {
				List<E> temp = new ArrayList<>(smallerPermutated);
				temp.add(index, firstElement);
				returnValue.add(temp);
			}
		}
		return returnValue;
	}


	public static  <E> List<List<E>> generatePerm(List<E> original, int pos, int length, List<E> perm) {
		List<List<E>> collectedResult = new ArrayList<>();
		if (pos == length) {
			collectedResult.add(perm);
		} else {
			for (int i = 0 ; i < original.size() ; i++) {
				ArrayList<E> permCopy = new ArrayList<>(perm);
				E ithChrackter  = original.get(i);

				List<E> newOriginal = new ArrayList<>(original);
				newOriginal.remove(ithChrackter);

				permCopy.add(pos, ithChrackter);
				collectedResult.addAll(generatePerm(newOriginal, pos+1, length, permCopy));
			}
		}
		return collectedResult;
	}




	private static ReusableAnimator reusableAnimator;
	public static ReusableAnimator getReusableAnimator(Injector injector){
		if (reusableAnimator==null){
			reusableAnimator = injector.getInstance(ReusableAnimator.class);
		}
		return reusableAnimator;
	}

	public static StateSpace createStateSpace(String path, Injector injector) throws IOException, ModelTranslationError {
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
	 * Wild cards are those where in the old operation the identifier did not exist - those don't show up in the mapping
	 * @param newInfo the operation infos from the new operation
	 * @param currentMappings the current mapping old -> new
	 */
	public static Set<String> calculateAllWildCards(OperationInfo newInfo, Map<String, String> currentMappings){
		Set<String> result = newInfo.getAllIdentifier();
		result.removeAll(currentMappings.values());
		return result;
	}


	/**
	 * Void cards are those where in the new operation the identifier did not exist - those don't show up in the mapping
	 * @param oldInfo the operation infos from the old operation
	 * @param currentMappings the current mapping old -> new
	 */
	public static Set<String> calculateAllVoidCards(OperationInfo oldInfo, Map<String, String> currentMappings){
		Set<String> result = oldInfo.getAllIdentifier();
		result.removeAll(currentMappings.keySet());
		return result;
	}


	/**
	 * Wild cards are those where in the old operation the identifier did not exist - those don't show up in the mapping
	 * @param newInfo the operation infos from the new operation
	 * @param currentMappings the current mapping old -> new
	 */
	public static Set<String> calculateVarWildCards(OperationInfo newInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(newInfo.getAllVariables());
		result.removeAll(currentMappings.values());
		return result;
	}


	/**
	 * Void cards are those where in the new operation the identifier did not exist - those don't show up in the mapping
	 * @param oldInfo the operation infos from the old operation
	 * @param currentMappings the current mapping old -> new
	 */
	public static Set<String> calculateVarVoidCards(OperationInfo oldInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(oldInfo.getAllVariables());
		result.removeAll(currentMappings.keySet());
		return result;
	}


	/**
	 * Wild cards are those where in the old operation the identifier did not exist - those don't show up in the mapping
	 * @param newInfo the operation infos from the new operation
	 * @param currentMappings the current mapping old -> new
	 */
	public static Set<String> calculateInWildCards(OperationInfo newInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(newInfo.getParameterNames());
		result.removeAll(currentMappings.values());
		return result;
	}


	/**
	 * Void cards are those where in the new operation the identifier did not exist - those don't show up in the mapping
	 * @param oldInfo the operation infos from the old operation
	 * @param currentMappings the current mapping old -> new
	 */
	public static Set<String> calculateInVoidCards(OperationInfo oldInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(oldInfo.getParameterNames());
		result.removeAll(currentMappings.keySet());
		return result;
	}


	/**
	 * Wild cards are those where in the old operation the identifier did not exist - those don't show up in the mapping
	 * @param newInfo the operation infos from the new operation
	 * @param currentMappings the current mapping old -> new
	 */
	public static Set<String> calculateOutWildCards(OperationInfo newInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(newInfo.getOutputParameterNames());
		result.removeAll(currentMappings.values());
		return result;
	}


	/**
	 * Void cards are those where in the new operation the identifier did not exist - those don't show up in the mapping
	 * @param oldInfo the operation infos from the old operation
	 * @param currentMappings the current mapping old -> new
	 */
	public static Set<String> calculateOutVoidCards(OperationInfo oldInfo, Map<String, String> currentMappings){
		Set<String> result = new HashSet<>(oldInfo.getOutputParameterNames());
		result.removeAll(currentMappings.keySet());
		return result;
	}


}
