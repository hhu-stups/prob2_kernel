package de.prob.check.tracereplay.check;


import de.prob.check.tracereplay.PersistentTransition;
import de.prob.statespace.OperationInfo;

import java.util.*;
import java.util.stream.Collectors;

import static de.prob.check.tracereplay.check.TraceCheckerUtils.stripNonOpClause;
import static de.prob.check.tracereplay.check.TraceCheckerUtils.usedOperations;
import static java.util.Collections.emptySet;

/**
 * Performs static checks on a trace and finds potential incompatibilities
 * After running checks this class holds the found incompatibilities
 */
@Deprecated
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
	//	typeIorII = operationWithSameParameterLength;

		typeIorII = operationNamesMatch.stream().filter(entry -> operationInfosAreSimilar(oldMachine.get(entry), newMachine.get(entry))).collect(Collectors.toSet());

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
		return candidates.stream()
				.filter(newMachine::containsKey)
				.filter(oldMachine::containsKey)
				.filter(operation ->
				newMachine.get(operation).getOutputParameterNames().size() + newMachine.get(operation).getParameterNames().size()
						==
						oldMachine.get(operation).getOutputParameterNames().size() + oldMachine.get(operation).getParameterNames().size()
		).collect(Collectors.toSet());
	}


	/**
	 * Gets two operationInfos and return true whenever both operation infos work with the same amount of variables which have the same type
	 * @param oldInfo the infos of the old operation
	 * @param newInfo the infos of the new operation
	 * @return the comparison result
	 */
	public static boolean operationInfosAreSimilar(OperationInfo oldInfo, OperationInfo newInfo){

		Map<OperationInfo.ContentType, List<String>> allIdsOld = oldInfo.getIdentifiersAsSortedCollection();
		Map<OperationInfo.ContentType, List<String>> allIdsNew = newInfo.getIdentifiersAsSortedCollection();

		return Arrays.stream(OperationInfo.ContentType.values())
				.allMatch(type -> sameLength(allIdsOld.get(type), allIdsNew.get(type)) &&
						utilizesSameType(allIdsOld.get(type), oldInfo.getTypeMap(), allIdsNew.get(type), newInfo.getTypeMap()));

	}


	public static boolean sameLength(List<String> list1, List<String> list2){
		return list1.size() == list2.size();
	}

	public static boolean utilizesSameType(List<String> list1, Map<String, String> typeMap1, List<String> list2, Map<String, String> typeMap2){
		List<String> listR1 = reduceToType(list1, typeMap1);
		List<String> listR2 = reduceToType(list2, typeMap2);
		return listR2.containsAll(listR1) && listR1.containsAll(listR2);
	}

	public static List<String> reduceToType(List<String> list1, Map<String, String> typeMap1){
		return typeMap1.entrySet().stream().filter(entry -> list1.contains(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
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
