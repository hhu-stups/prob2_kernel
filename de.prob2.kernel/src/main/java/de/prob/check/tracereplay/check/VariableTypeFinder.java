package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Variables are either cloned, renamed or replaced
 */
public class VariableTypeFinder {


	Set<String> typeICandidates;
	Set<String> typeIIorIVCandidates;

	public VariableTypeFinder(PersistentTrace trace, Set<String> oldVars, Set<String> newVars){
		typeICandidates = usedVariables(trace).stream().filter(newVars::contains).collect(Collectors.toSet());
		typeIIorIVCandidates = new HashSet<>(oldVars);
		typeIIorIVCandidates.removeAll(typeICandidates);
	}

	public static Set<String> usedVariables(PersistentTrace trace){
		return trace.getTransitionList().stream().map(VariableTypeFinder::getUsedVarsOfTransition)
				.reduce(new HashSet<>(), (acc, next) -> {
			acc.addAll(next);
			return acc;
		});
	}

	public static Set<String> getUsedVarsOfTransition(PersistentTransition transition){
		Set<String> result = new HashSet<>();
		result.addAll(transition.getDestStateNotChanged());
		result.addAll(transition.getDestinationStateVariables().keySet());
		return result;
	}


	public Set<String> getTypeIIorIVCandidates() {
		return typeIIorIVCandidates;
	}

	public Set<String> getTypeICandidates() {
		return typeICandidates;
	}

}
