package de.prob.check.tracereplay.check;

import java.util.*;

public class TraceCheckerUtils {

	/**
	 * Zips two lists to a map - return an empty list if list have different sizes, naturally equal elements will override
	 * each other
	 * @param list1 the first list
	 * @param list2 the second list
	 * @param <T> Type of the first List
	 * @param <U> Type of the second list
	 * @return a Map of the Type <T, U>
	 */
	public static <T, U> Map<T, U> zip(List<T> list1, List<U> list2){
		if(list1.size() == list2.size()){
			Map<T, U> sideResult = new HashMap<>();
			for(int i = 0;  i < list1.size(); i++){
				sideResult.put(list1.get(i), list2.get(i));
			}
			return sideResult;
		}
		return Collections.emptyMap();
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

}
