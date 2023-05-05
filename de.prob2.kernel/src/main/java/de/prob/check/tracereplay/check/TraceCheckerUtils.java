package de.prob.check.tracereplay.check;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.prob.check.tracereplay.PersistentTransition;

public class TraceCheckerUtils {
	/**
	 * Returns the operations actually used by the trace
	 * @param transitionList the trace to analyse
	 * @return a set of operations used in the trace
	 */
	public static Set<String> usedOperations(List<PersistentTransition> transitionList){
		return transitionList.stream().map(PersistentTransition::getOperationName).collect(Collectors.toSet());
	}
}
