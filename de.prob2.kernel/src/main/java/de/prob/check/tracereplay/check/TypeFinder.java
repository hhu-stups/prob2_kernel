package de.prob.check.tracereplay.check;


import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.Transition;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 * Performs static checks on a trace and finds potential incompatibilities
 * After running checks this class holds the found incompatibilities
 */
public class TypeFinder {

	private final List<PersistentTransition> trace;
	private final Map<String, OperationInfo> oldMachine;
	private final Map<String, OperationInfo> newMachine;
	private final Set<String> oldVars;
	private final Set<String> newVars;

	private Set<String> typeIorII;
	private Set<String> typeIII;
	private Set<String> typeIV;
	private Map<String, Set<String>> typeIIPermutation;
	private Boolean initIsTypeIorIICandidate;



	/**
	 * @param trace the trace that is currently dealt with
	 * @param oldMachine operation of the old machine with which the trace was created
	 * @param newMachine operations of the new machine
	 * @param oldVars the Variables from the old machine
	 * @param newVars the Variables from the new machine
	 */
	public TypeFinder(List<PersistentTransition> trace, Map<String, OperationInfo> oldMachine, Map<String, OperationInfo> newMachine,
					  Set<String> oldVars, Set<String> newVars){
		this.trace = trace;
		this.newMachine = newMachine;
		this.oldMachine = oldMachine;
		this.oldVars = oldVars;
		this.newVars = newVars;
	}


	public TypeFinder(List<PersistentTransition> trace, Map<String, OperationInfo> newMachine, Set<String> newVars){
		this.trace = trace;
		this.newMachine = newMachine;
		this.oldMachine = emptyMap();
		this.newVars = newVars;
		this.oldVars = emptySet();
	}


	public static List<PersistentTransition> stripInitClause(List<PersistentTransition> transitionList){
		return transitionList.stream()
				.filter(element -> !element.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME))
				.collect(Collectors.toList());
	}

	public static List<PersistentTransition> stripNonOpClause(List<PersistentTransition> transitionList){
		return transitionList.stream()
				.filter(element -> !element.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME))
				.filter(element -> !element.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME))
				.filter(element -> !element.getOperationName().equals(Transition.PARTIAL_SETUP_CONSTANTS_NAME))
				.collect(Collectors.toList());
	}

	/**
	 * See master thesis for reference
	 * We try to narrow down the best candidate for our operation and apply checks to verify our assumption
	 */
	public void check(){
		Set<String> operationNamesTrace = usedOperations(stripNonOpClause(trace));
		Set<String> operationNamesBeta = newMachine.keySet();

		Set<String> operationNamesDoesNotMatch = new HashSet<>(operationNamesTrace);
		operationNamesDoesNotMatch.removeAll(operationNamesBeta);

		Set<String> operationNamesMatch = new HashSet<>(operationNamesTrace);
		operationNamesMatch.removeAll(operationNamesDoesNotMatch);

		//Type I/II candidates
		Set<String> operationWithSameParameterLength = findOperationsWithSameParameterLength(operationNamesMatch, oldMachine, newMachine);
		typeIorII = operationWithSameParameterLength;

		//Type III candidates
		Set<String> operationNotSameParameterLength = new HashSet<>(operationNamesMatch);
		operationNotSameParameterLength.removeAll(operationWithSameParameterLength);
		typeIII = operationNotSameParameterLength;

		//Is there an operation with the same signature?
		//Type II
		Map<String, Set<String>> candidatesForOperation =
				checkIfOperationCandidatesFulfillSuperficialCriteriaForBeingACloneOrRenamed(operationNamesDoesNotMatch, oldMachine, newMachine);
		typeIIPermutation = candidatesForOperation;


		//Type IV
		Set<String> operationsWithNoFittingCandidate = new HashSet<>(operationNamesDoesNotMatch);
		operationsWithNoFittingCandidate.removeAll(candidatesForOperation.keySet());
		typeIV = operationsWithNoFittingCandidate;


		initIsTypeIorIICandidate = oldVars.size() == newVars.size();
	}

	

	/**
	 * Provided with a Set of operation names, this function will return a map, mapping the names of the provided operation
	 * names to possible candidates in the new machine. Operations without suitable candiates are removed.
	 * amount of variables/parameters in each machine
	 * @param candidates the operation names to check
	 * @param oldMachine the old machine
	 * @param newMachine the new machine
	 * @return a map, mapping operation names from the old machine to candidates in the new machine
	 */
	public static Map<String, Set<String>> checkIfOperationCandidatesFulfillSuperficialCriteriaForBeingACloneOrRenamed(
			final Set<String> candidates, final Map<String, OperationInfo> oldMachine, final Map<String, OperationInfo> newMachine){
		return candidates.stream().filter(oldMachine::containsKey).collect(Collectors.toMap(entry -> entry , operation -> {
			OperationInfo operationInfo = oldMachine.get(operation);
			int numberOfInputVars = operationInfo.getParameterNames().size();
			int numberOfOutputVars = operationInfo.getOutputParameterNames().size();
			int numberOfWrittenVars = operationInfo.getWrittenVariables().size();
			int numberOfNonDetWrittenVars = operationInfo.getNonDetWrittenVariables().size();
			int numberOfReadVariables = operationInfo.getReadVariables().size();

			return findCandidates(numberOfInputVars, numberOfOutputVars, numberOfWrittenVars, numberOfNonDetWrittenVars,
					numberOfReadVariables, newMachine);

		})).entrySet().stream()
				.filter(stringSetEntry -> !stringSetEntry.getValue().isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	//TODO Fix that - lol
	/**
	 * Gets a set with operation names that exists in the old and the new machine and returns a filtered set with the names
	 * where the number of input and output parameters from the old machine is the same as in the new machine
	 * @param candidates the set with operation names being suitable candidates
	 * @param oldMachine the old machine
	 * @param newMachine the new machine
	 * @return the filtered list - operations that have the same parameter size in old and new machine
	 */
	public static Set<String> findOperationsWithSameParameterLength(final Set<String> candidates, final Map<String, OperationInfo> oldMachine,
													  final Map<String, OperationInfo> newMachine){

		if(oldMachine.isEmpty()) return emptySet();
		return candidates.stream().filter(operation ->
				newMachine.get(operation).getOutputParameterNames().size() + newMachine.get(operation).getParameterNames().size()
						==
						oldMachine.get(operation).getOutputParameterNames().size() + oldMachine.get(operation).getParameterNames().size()
		).collect(Collectors.toSet());
	}

	/**
	 * Provided with information about the number of manipulated variables/parameters this function returns operation names
	 * that manipulated an equal amount
	 * @param numberOfInputVars number of input parameter
	 * @param numberOfOutputVars number of output parameter
	 * @param numberOfWrittenVars number deterministic written variables
	 * @param numberOfNonDetWrittenVars number nondeterministic written variables
	 * @param numberOfReadVariables number read  variables
	 * @param newMachine the machine from where we want to take the candidates
	 * @return a set of candidates for the provided numbers
	 */
	public static Set<String> findCandidates(final int numberOfInputVars, final int numberOfOutputVars, final int numberOfWrittenVars,
							   final int numberOfNonDetWrittenVars, final int numberOfReadVariables, final Map<String, OperationInfo> newMachine){
		return newMachine.values().stream().filter(operationInfo ->
			operationInfo.getParameterNames().size() == numberOfInputVars &&
					operationInfo.getOutputParameterNames().size() == numberOfOutputVars &&
					operationInfo.getWrittenVariables().size() == numberOfWrittenVars &&
					operationInfo.getNonDetWrittenVariables().size() == numberOfNonDetWrittenVars &&
					operationInfo.getReadVariables().size() == numberOfReadVariables)
				.map(OperationInfo::getOperationName)
				.collect(Collectors.toSet());

	}

	/**
	 * Returns the operations actually used by the trace
	 * @param transitionList the trace to analyse
	 * @return a set of operations used in the trace
	 */
	public static Set<String> usedOperations(List<PersistentTransition> transitionList){
		return transitionList.stream().map(PersistentTransition::getOperationName).collect(Collectors.toSet());
	}

	public Set<String> getTypeIorII() {
		return typeIorII;
	}

	public Set<String> getTypeIII() {
		return typeIII;
	}

	public Set<String> getTypeIV() {
		return typeIV;
	}

	public Map<String, Set<String>> getTypeIIPermutation() {
		return typeIIPermutation;
	}

	public Boolean getInitIsTypeIorIICandidate() {
		return initIsTypeIorIICandidate;
	}

}
