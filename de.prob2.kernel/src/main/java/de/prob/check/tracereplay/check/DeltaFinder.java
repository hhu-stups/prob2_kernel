package de.prob.check.tracereplay.check;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.CompareTwoOperations;
import de.prob.animator.command.GetMachineOperationsFull;
import de.prob.animator.command.PrepareOperations;
import de.prob.exception.ProBError;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.statespace.StateSpace;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds a operation if it is renamed or contains renamed variables/parameter
 */
public class DeltaFinder {

	private final Set<String> typeIorII;
	private final Map<String, Set<String>> typeIICandidates;
	private StateSpace stateSpace = null;



	CheckerInterface checkerInterface = (prepareOperationTriple, candidate) -> {
		CompareTwoOperations compareTwoOperations = new CompareTwoOperations(prepareOperationTriple.third,
				candidate, prepareOperationTriple.first, prepareOperationTriple.second, new ObjectMapper());
		try {
			stateSpace.execute(compareTwoOperations);
			return compareTwoOperations.getDelta();
		} catch (ProBError e) {
			return new HashMap<>();
		}
	};


	PrepareOperationsInterface prepareOperationsInterface = (operation) -> {
		PrepareOperations prepareOperations = new PrepareOperations(operation);
		stateSpace.execute(prepareOperations);
		return prepareOperations.asTriple();
	};


	public DeltaFinder(Set<String> typeIorII, Map<String, Set<String>> typeIICandidates, ReusableAnimator animator, StateSpace stateSpace) {
		this.typeIorII = typeIorII;
		this.typeIICandidates = typeIICandidates;
		this.stateSpace = stateSpace;
	}


	/**
	 * For each candidate the possibility of renaming is examined. If there are results aka the was a potential renaming,
	 * a map with a delta of the renamed identifiers is provided. Otherwise the entry is deleted.
	 * @param oldOperation the operations from the old machine
	 * @param newOperation the operations from the new machine
	 * @param candidates the candidates for which a delta has to be found (operation names)
	 * @param checkFunction the function to check if two operations are similar
	 * @param prepareOperationsInterface the function to prepare an operation be replacing the identifiers with free Variables
	 * @return a map where each success is mapped to the delta, unsuccessful candidates are removed.
	 */
	public static Map<String, Map<String, String>> checkDeterministicPairs(Map<String, CompoundPrologTerm> oldOperation,
															 Map<String, CompoundPrologTerm> newOperation,
															 Set<String> candidates,
															 CheckerInterface checkFunction,
															 PrepareOperationsInterface prepareOperationsInterface) {

		return candidates.stream().collect(Collectors.toMap(operation -> operation, operation -> {
			Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> result = prepareOperationsInterface.prepareOperation(oldOperation.get(operation));
			return checkFunction.checkTypeII(result, newOperation.get(operation));
		})).entrySet().stream()
				.filter(stringMapEntry -> !stringMapEntry.getValue().isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Same idea as in checkDeterministicPairs with the limitation that we now make the calculation over a map of sets
	 * rather then only over sets
	 * @param oldOperation the operations from the old machine
	 * @param newOperation the operations from the new machine
	 * @param candidates the candidates for which a delta has to be found (operation names)
	 * @param checkFunction the function to check if two operations are similar
	 * @param prepareOperationsInterface the function to prepare an operation be replacing the identifiers with free Variables
	 * @return a map which contains only successful matches. Each candidate is mapped to suitable operations and their deltas
	 */
	public static Map<String, Map<String, Map<String, String>>> checkNondeterministicPairs(Map<String, CompoundPrologTerm> oldOperation,
																			 Map<String, CompoundPrologTerm> newOperation,
																			 Map<String, Set<String>> candidates,
																			 CheckerInterface checkFunction,
																			 PrepareOperationsInterface prepareOperationsInterface) {

		return candidates.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
			Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> preparedTerm = prepareOperationsInterface.prepareOperation(oldOperation.get(entry.getKey()));
			return entry.getValue().stream().collect(Collectors.toMap(value -> value,
					value -> checkFunction.checkTypeII(preparedTerm, newOperation.get(value))))
					.entrySet().stream().filter(stringMapEntry -> !stringMapEntry.getValue().isEmpty())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		})).entrySet().stream().filter(higherMapEntry -> !higherMapEntry.getValue().isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}


	Map<String, CompoundPrologTerm> getOldOperations() {
		return null;
	}

	Map<String, CompoundPrologTerm> getNewOperations() {
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		stateSpace.execute(getMachineOperationsFull);
		return getMachineOperationsFull.getOperationsWithNames();
	}
}


