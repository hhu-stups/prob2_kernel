package de.prob.check.tracereplay.check;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.CompareTwoOperations;
import de.prob.animator.command.GetMachineOperationsFull;
import de.prob.animator.command.PrepareOperations;
import de.prob.check.tracereplay.check.exceptions.DeltaCalculationException;
import de.prob.check.tracereplay.check.exceptions.PrologTermNotDefinedException;
import de.prob.exception.ProBError;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.*;

/**
 * Finds a operation if it is renamed or contains renamed variables/parameter
 */
public class RenamingAnalyzer implements RenamingAnalyzerInterface {

	private final Set<String> typeIorII;
	private final Map<String, Set<String>> typeIICandidates;
	private ReusableAnimator animator;
	private final String oldMachine;
	private final String newMachine;
	private final Injector injector;
	private final boolean typeIOrIICandidate;
	private final Map<String, OperationInfo> oldMachineInfos;

	private Map<String, List<RenamingDelta>> typeIIWithCandidatesAsDeltaMap;
	private List<RenamingDelta> typeIIAsRenamingDeltaList;
	private Map<String, Map<String, String>> resultTypeII;
	private Map<String, String> resultInitTypeII;


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
		if(!prepareOperations.getNotReachableNodes().isEmpty()) {
			throw new PrologTermNotDefinedException(prepareOperations.getNotReachableNodes());
		}
		return prepareOperations.asTriple();
	};


	public RenamingAnalyzer(Set<String> typeIorII, Map<String, Set<String>> typeIICandidates, boolean typeIorIICandidate,
							String oldMachine,
							String newMachine,
							Injector injector,
							Map<String, OperationInfo> oldMachineInfos) {
		this.typeIorII = typeIorII;
		this.typeIICandidates = typeIICandidates;
		this.oldMachine = oldMachine;
		this.newMachine = newMachine;
		this.injector = injector;
		this.animator = injector.getInstance(ReusableAnimator.class);
		this.typeIOrIICandidate = typeIorIICandidate;
		this.oldMachineInfos = oldMachineInfos;
	}


	/**
	 * Wraps the steps necessary to calculate the two deltas
	 * @throws DeltaCalculationException something went wrong, we wrapped it
	 */
	public void calculateDelta() throws DeltaCalculationException {
		try {
			Map<String, CompoundPrologTerm> newOperations = getOperations(newMachine);
			Map<String, CompoundPrologTerm> oldOperations = getOperations(oldMachine);

			resultInitTypeII = createInitMapping(oldOperations, newOperations, typeIOrIICandidate, checkerInterface, prepareOperationsInterface);

			Map<String, Map<String, String>> resultTypeIorII =
					checkDeterministicPairs(oldOperations, newOperations, typeIorII, checkerInterface, prepareOperationsInterface);

			resultTypeII = getResultTypeII(resultTypeIorII);


			Map<String, Map<String, Map<String, String>>> typeIIWithCandidates =
					checkNondeterministicPairs(oldOperations, newOperations, typeIICandidates, checkerInterface, prepareOperationsInterface);

			Map<String, Map<String, String>> trueTypeIINonAmbiguous = filterTrueDeterministic(typeIIWithCandidates);

			resultTypeII.putAll(trueTypeIINonAmbiguous);

			typeIIAsRenamingDeltaList = transformResultTypeIIToDeltaList(resultTypeII);

			Map<String, Map<String, Map<String, String>>> resultTypeIIWithCandidates = filterTrueNonDeterministic(typeIIWithCandidates);

			typeIIWithCandidatesAsDeltaMap = transformToDeltaMap(resultTypeIIWithCandidates);

		}catch (IOException | ProBError | PrologTermNotDefinedException e){
			throw new DeltaCalculationException(e);
		}

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
															 PrepareOperationsInterface prepareOperationsInterface) throws PrologTermNotDefinedException {

		Map<String,  Map<String, String>> sideResult = new HashMap<>();

		for(String entry : candidates) {

			Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> result = prepareOperationsInterface.prepareOperation(oldOperation.get(entry));

			sideResult.put(entry, checkFunction.checkTypeII(result, newOperation.get(entry)));

		}


		return sideResult.entrySet().stream()
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
																			 PrepareOperationsInterface prepareOperationsInterface) throws PrologTermNotDefinedException {

		Map<String, Map<String, Map<String, String>>> sideResult = new HashMap<>();

		for(Map.Entry<String, Set<String>> entry : candidates.entrySet()){
			Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> preparedTerm =  prepareOperationsInterface
					.prepareOperation(oldOperation.get(entry.getKey()));
			Map<String, Map<String, String>> newValue = entry.getValue()
					.stream()
					.collect(toMap(value -> value, value -> checkFunction
							.checkTypeII(preparedTerm, newOperation.get(value))))
					.entrySet()
					.stream()
					.filter(stringMapEntry -> !stringMapEntry.getValue().isEmpty())
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

			sideResult.put(entry.getKey(), newValue);
		}

		return sideResult.entrySet()
				.stream()
				.filter(higherMapEntry -> !higherMapEntry.getValue().isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}


	/**
	 * Loads the old referenced machine into the state space and retracts its operations,
	 * then loads the current machine back into the animator
	 * @param path the path of the currently not loaded machine
	 * @return a map of operations
	 * @throws IOException file not found
	 */
	public Map<String, CompoundPrologTerm> getOperations(String path) throws IOException {
		StateSpace stateSpace = TraceCheckerUtils.createStateSpace(path, injector);
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		stateSpace.execute(getMachineOperationsFull);
		return getMachineOperationsFull.getOperationsWithNames();
	}


	public Map<String, Map<String, String>> getResultTypeII() {
		return resultTypeII;
	}


	public static Map<String, Map<String, String>> filterTrueDeterministic(Map<String, Map<String, Map<String, String>>> typeIIWithCandidates){
		return typeIIWithCandidates.entrySet().stream()
				.filter(stringMapMap -> stringMapMap.getValue().size() == 1)
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().entrySet().stream().findFirst().get().getValue()));
	}

	public Map<String, Map<String, String>> getResultTypeII(Map<String, Map<String, String>> resultTypeIorII) {
		return resultTypeIorII.entrySet().stream().filter(entry-> !entry.getValue().entrySet().stream()
				.filter(innerEntry -> !innerEntry.getValue().equals(innerEntry.getKey())).collect(Collectors.toSet()).isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public Map<String, Map<String, Map<String, String>>> filterTrueNonDeterministic(Map<String, Map<String, Map<String, String>>> typeIIWithCandidates){
		return typeIIWithCandidates.entrySet().stream()
				.filter(stringMapMap -> stringMapMap.getValue().size() > 1).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public static Map<String, CompoundPrologTerm> extractInitTerm(Map<String, CompoundPrologTerm> allOperations){
		return allOperations.entrySet().stream()
				.filter(entry-> entry.getKey().equals(Transition.INITIALISE_MACHINE_NAME))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public static Map<String, String> createInitMapping(Map<String, CompoundPrologTerm> oldOperations,
														Map<String, CompoundPrologTerm> newOperations, boolean typeIOrIICandidate,
														CheckerInterface checkerInterface,
														PrepareOperationsInterface prepareOperationsInterface) throws PrologTermNotDefinedException {
		Map<String, CompoundPrologTerm> initOld = extractInitTerm(oldOperations);
		Map<String, CompoundPrologTerm> initNew = extractInitTerm(newOperations);

		if(typeIOrIICandidate){
			Map<String, String> initMapping = checkDeterministicPairs(initOld, initNew,
					Collections.singleton(Transition.INITIALISE_MACHINE_NAME), checkerInterface,
					prepareOperationsInterface)
					.entrySet()
					.stream()
					.filter(entry -> entry.getKey().equals(Transition.INITIALISE_MACHINE_NAME)) //Fancy way of checking if INIT was set
					.map(Map.Entry::getValue)
					.reduce(new HashMap<>(), (id, acc) -> {
								Map<String, String> toReturn = new HashMap<>();
								toReturn.putAll(id);
								toReturn.putAll(acc);
								return toReturn;
							});

			boolean initIsTypeI = initMapping
					.entrySet()
					.stream()
					.filter(entry -> !entry.getKey().equals(entry.getValue()))
					.collect(toSet()).isEmpty();

			if(!initIsTypeI){
				return initMapping;
			}else{
				return emptyMap();
			}

		}else{
			return emptyMap();
		}

	}


	public Map<String, String> getResultTypeIIInit() {
		return resultInitTypeII;
	}

	public Map<String, List<RenamingDelta>> getResultTypeIIp() {
		return null;
	}

	public RenamingDelta getResultTypeIIInitAsDelta() {
		return new RenamingDelta(Transition.INITIALISE_MACHINE_NAME, Transition.INITIALISE_MACHINE_NAME, emptyMap(), emptyMap(), resultInitTypeII);
	}



	public List<RenamingDelta> transformResultTypeIIToDeltaList(Map<String, Map<String, String>> resultTypeII){
		 List<RenamingDelta> transitionsWithoutInit = resultTypeII.entrySet().stream()
				.map(entry -> new RenamingDelta(entry.getValue(), oldMachineInfos.get(entry.getKey()))).collect(Collectors.toList());

		if(!getResultTypeIIInit().isEmpty())
		{
			transitionsWithoutInit.add(new RenamingDelta(Transition.INITIALISE_MACHINE_NAME, Transition.INITIALISE_MACHINE_NAME,
					emptyMap(), emptyMap(), getResultTypeIIInit()));
		}

		return transitionsWithoutInit;
	}

	@Override
	public List<RenamingDelta> getResultTypeIIAsDeltaList(){
		return typeIIAsRenamingDeltaList;
	}


	public Map<String, List<RenamingDelta>> transformToDeltaMap(Map<String, Map<String, Map<String, String>>> resultsToCandidates){
		return resultsToCandidates.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry ->
				{
					Map<String, Map<String, String>> candidate = entry.getValue();
					return candidate.values().stream()
							.map(stringStringMap -> new RenamingDelta(stringStringMap, oldMachineInfos.get(entry.getKey())))
							.collect(Collectors.toList());
				}));
	}


	public Map<String, List<RenamingDelta>> getResultTypeIIWithCandidates(){
		return typeIIWithCandidatesAsDeltaMap;
	}

}


