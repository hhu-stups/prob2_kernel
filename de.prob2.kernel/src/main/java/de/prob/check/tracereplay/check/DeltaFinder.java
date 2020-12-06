package de.prob.check.tracereplay.check;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.CompareTwoOperations;
import de.prob.animator.command.GetMachineOperationsFull;
import de.prob.animator.command.PrepareOperations;
import de.prob.exception.ProBError;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Finds a operation if it is renamed or contains renamed variables/parameter
 */
public class DeltaFinder {

	private final Set<String> typeIorII;
	private final Map<String, Set<String>> typeIICandidates;
	private ReusableAnimator animator;
	private final String oldMachine;
	private final String newMachine;
	private final Injector injector;
	private final boolean typeIOrIICandidate;


	private Map<String, Map<String, String>> resultTypeIorII;
	private Map<String, Map<String, Map<String, String>>> resultTypeIIwithCandidates;
	private Map<String, String> resultInitTypeIorII;


	/**
	 * Wraps a stateful call and make it replaceable with a stateless call
	 */
	public final CheckerInterface checkerInterface = (prepareOperationTriple, candidate) -> {
		CompareTwoOperations compareTwoOperations = new CompareTwoOperations(prepareOperationTriple.third,
				candidate, prepareOperationTriple.first, prepareOperationTriple.second, new ObjectMapper());
		try {
			animator.execute(compareTwoOperations);
			return compareTwoOperations.getDelta();
		} catch (ProBError e) {
			return new HashMap<>();
		}
	};

	/**
	 * Wraps a stateful call and make it replaceable with a stateless call
	 */
	public final PrepareOperationsInterface prepareOperationsInterface = (operation) -> {
		PrepareOperations prepareOperations = new PrepareOperations(operation);
		animator.execute(prepareOperations);
		return prepareOperations.asTriple();
	};


	public DeltaFinder(Set<String> typeIorII, Map<String, Set<String>> typeIICandidates, boolean typeIorIICandidate, ReusableAnimator animator,
					   String oldMachine,
					   String newMachine,
					   Injector injector) {
		this.typeIorII = typeIorII;
		this.typeIICandidates = typeIICandidates;
		this.oldMachine = oldMachine;
		this.newMachine = newMachine;
		this.injector = injector;
		this.animator = animator;
		this.typeIOrIICandidate = typeIorIICandidate;
	}


	/**
	 * Wraps the steps necessary to calculate the two deltas
	 * @throws IOException something went wrong when reading machine files
	 * @throws ModelTranslationError the machine files contain errors
	 */
	public void calculateDelta() throws IOException, ModelTranslationError {
		Map<String, CompoundPrologTerm> newOperations = getOperations(newMachine);
		Map<String, CompoundPrologTerm> oldOperations = getOperations(oldMachine);

		Map<String, CompoundPrologTerm> initOld = oldOperations.entrySet().stream()
				.filter(entry-> entry.getKey().equals(Transition.INITIALISE_MACHINE_NAME))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		Map<String, CompoundPrologTerm> initNew = newOperations.entrySet().stream()
				.filter(entry-> entry.getKey().equals(Transition.INITIALISE_MACHINE_NAME))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		if(typeIOrIICandidate){
			resultInitTypeIorII = checkDeterministicPairs(initOld, initNew,
					Collections.singleton(Transition.INITIALISE_MACHINE_NAME), checkerInterface,
					prepareOperationsInterface).get(Transition.INITIALISE_MACHINE_NAME);

		}else{
			resultInitTypeIorII = Collections.emptyMap();
		}


		resultTypeIorII = checkDeterministicPairs(oldOperations, newOperations, typeIorII, checkerInterface, prepareOperationsInterface);
		Map<String, Map<String, Map<String, String>>> typeIIWithCandidates =
				checkNondeterministicPairs(oldOperations, newOperations, typeIICandidates, checkerInterface, prepareOperationsInterface);

		Map<String, Map<String, String>> trueTypeIINonAmbiguous = typeIIWithCandidates.entrySet().stream()
				.filter(stringMapMap -> stringMapMap.getValue().size() == 1)
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().entrySet().stream().findFirst().get().getValue()));

		trueTypeIINonAmbiguous.forEach((key, value) -> resultTypeIorII.put(key, value));

		resultTypeIIwithCandidates = typeIIWithCandidates.entrySet().stream()
				.filter(stringMapMap -> stringMapMap.getValue().size() > 1).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));


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


	/**
	 * Loads the old referenced machine into the statespace and retracts its operations,
	 * then loads the current machine back into the animator
	 * @param path the path of the currently not loaded machine
	 * @return a map of operations
	 * @throws IOException file not found
	 * @throws ModelTranslationError error when loading the machine
	 */
	public Map<String, CompoundPrologTerm> getOperations(String path) throws IOException, ModelTranslationError {
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(path.substring(path.lastIndexOf(".")+1)));
		if(animator.getCurrentStateSpace()!=null)
		{
			animator.getCurrentStateSpace().kill();
		}
		StateSpace stateSpace = animator.createStateSpace();
		factory.extract(path).loadIntoStateSpace(stateSpace);
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		animator.execute(getMachineOperationsFull);
		animator.getCurrentStateSpace().kill();
		return getMachineOperationsFull.getOperationsWithNames();
	}


	public Map<String, Map<String, String>> getResultTypeIorII() {
		return resultTypeIorII;
	}


	public Map<String, Map<String, String>> getResultTypeII() {
		return resultTypeIorII.entrySet().stream().filter(entry-> !entry.getValue().entrySet().stream()
				.filter(innerEntry -> !innerEntry.getValue().equals(innerEntry.getKey())).collect(Collectors.toSet()).isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}


	public Map<String, Map<String, Map<String, String>>> getResultTypeIIWithCandidates() {
		return resultTypeIIwithCandidates;
	}

	public Map<String, String> getResultTypeIIInit() {
		return resultInitTypeIorII;
	}
}


