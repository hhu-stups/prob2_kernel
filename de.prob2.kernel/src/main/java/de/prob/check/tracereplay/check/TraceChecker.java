package de.prob.check.tracereplay.check;


import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.statespace.OperationInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Performs static checks on a trace
 */
public class TraceChecker {

	private final PersistentTrace trace;
	private final Map<String, OperationInfo> oldMachine;
	private final Map<String, OperationInfo> newMachine;

	public TraceChecker(PersistentTrace trace, Map<String, OperationInfo> oldMachine, Map<String, OperationInfo> newMachine){
		this.trace = trace;
		this.newMachine = newMachine;
		this.oldMachine = oldMachine;
	}

	/**
	 * See master thesis for reference
	 * We try to narrow down the best candidate for our operation and apply checks to verify our assumption
	 */
	public void check(){
		Set<String> operationNamesTrace = usedOperations(trace);
		Set<String> operationNamesBeta = newMachine.keySet();

		Set<String> operationNamesDoesNoMatch = operationNamesTrace;
		operationNamesDoesNoMatch.removeAll(operationNamesBeta);

		Set<String> operationNamesMatch = operationNamesTrace;
		operationNamesMatch.removeAll(operationNamesDoesNoMatch);

		//Type I/II candidates
		Set<String> operationWithSameParameterLength = findOperationsWithSameParameterLength(operationNamesMatch, oldMachine, newMachine);

		//Type III candidates
		Set<String> operationNotSameParameterLength = operationNamesMatch;
		operationNotSameParameterLength.removeAll(operationWithSameParameterLength);

		//Is there an operation with the same signature?
		//Type II
		Map<String, Set<String>> candidatesForOperation =
				checkIfOperationCandidatesFulfillSuperficialCriteriaForBeingACloneOrRenamed(operationNamesDoesNoMatch, oldMachine, newMachine);


		//Type IV
		Set<String> operationsWithNoFittingCandidate = operationNamesDoesNoMatch;
		operationsWithNoFittingCandidate.removeAll(candidatesForOperation.keySet());


		/**
		 * 1) Run Type I/II check eventually add results to Type IV
		 * 2) Try to find a type III mapping or else type 4
		 * 3) Report all Type IV operations and request permission to try ambigious execution
		 */


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
		Map<String, Set<String>> bla =  candidates.stream().collect(Collectors.toMap(entry -> entry , operation -> {

			OperationInfo operationInfo = oldMachine.get(operation);
			System.out.println(operationInfo);
			int numberOfInputVars = operationInfo.getParameterNames().size();
			int numberOfOutputVars = operationInfo.getOutputParameterNames().size();
			int numberOfWrittenVars = operationInfo.getWrittenVariables().size();
			int numberOfNonDetWrittenVars = operationInfo.getNonDetWrittenVariables().size();
			int numberOfReadVariables = operationInfo.getReadVariables().size();

			Set<String> ga =  findCandidates(numberOfInputVars, numberOfOutputVars, numberOfWrittenVars, numberOfNonDetWrittenVars,
					numberOfReadVariables, newMachine);

			System.out.println(ga);
			return ga;

		}));
				System.out.println(bla);
				return bla.entrySet().stream()
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
		System.out.println(numberOfInputVars);
		System.out.println(numberOfOutputVars);
		System.out.println(numberOfWrittenVars);
		System.out.println(numberOfNonDetWrittenVars);
		System.out.println(numberOfReadVariables);
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
	 * Takes a set of operation names and maps the corresponding used parameters to it
	 * @param operations the operation names to map to
	 * @param loadedMachine the target where one gets the parameters from
	 * @return a map representing a Set of parameter names used by the given operations
	 */
	/*public Map<String, Set<String>> parameterPerOperation(Set<String> operations, LoadedMachine loadedMachine){
		return operations.stream().collect(Collectors.toMap(entry -> entry , entry -> {
			List<String> outputVars = loadedMachine.getOperations().get(entry).getOutputParameterNames();
			List<String> inputVars = loadedMachine.getOperations().get(entry).getParameterNames();
			List<String> all = new ArrayList<>();
			all.addAll(outputVars);
			all.addAll(inputVars);
			return new HashSet<>(all);
		}));
	}*/

	/**
	 * Returns the operations actually used by the trace, contains $initialisation
	 * @param trace the trace to analyse
	 * @return a set of operations used in the trace
	 */
	public Set<String> usedOperations(PersistentTrace trace){
		return trace.getTransitionList().stream().map(PersistentTransition::getOperationName).collect(Collectors.toSet());
	}

}
