package de.prob.check.tracereplay.check.exploration;

import de.prob.animator.command.ConstructTraceCommand;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.ui.ProgressMemoryInterface;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.ui.MappingFactoryInterface;
import de.prob.exception.ProBError;
import de.prob.formula.PredicateBuilder;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.util.*;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class TraceExplorer {


	private final boolean initWasSet;
	private final MappingFactoryInterface mappingFactory;
	private final Map<Map<String, Map<MappingNames, Map<String, String>>>, Set<String>> updatedTypeIV = new HashMap<>();
	private final ReplayOptions replayOptions;
	private final ProgressMemoryInterface progressMemoryInterface;
	private final List<List<PersistenceDelta>> ungracefulTraces = new ArrayList<>();

	public TraceExplorer(boolean initWasSet, MappingFactoryInterface mappingFactory, ReplayOptions replayOptions, ProgressMemoryInterface progressMemoryInterface) {
		this.initWasSet = initWasSet;
		this.mappingFactory = mappingFactory;
		this.replayOptions = replayOptions;
		this.progressMemoryInterface = progressMemoryInterface;
	}

	public TraceExplorer(boolean initWasSet, MappingFactoryInterface mappingFactory, ProgressMemoryInterface progressMemoryInterface) {
		this.initWasSet = initWasSet;
		this.mappingFactory = mappingFactory;
		this.replayOptions = ReplayOptions.allowAll();
		this.progressMemoryInterface = progressMemoryInterface;
	}

	/**
	 * Gets a list of transition and a variable mapping and applies the mapping to all transitions in the list
	 *
	 * @param mapping        the mapping to be applied
	 * @param transitionList the list to apply to
	 * @return the changed transition list
	 */
	public static List<PersistentTransition> transformTransitionList(Map<String, Map<MappingNames, Map<String, String>>> mapping,
																	 List<PersistentTransition> transitionList) {
		return transitionList.stream().map(transition ->
		{
			if (mapping.containsKey(transition.getOperationName())) {
				return createPersistentTransitionFromMapping(mapping.get(transition.getOperationName()), transition);
			}
			return transition;
		}).collect(toList());
	}

	/**
	 * Removes the Enum Constans that were used to reduce the overhead for calculations by splitting the work in smaller pieces
	 *
	 * @param mappings the mapping to clean up
	 * @return the cleansed mapping
	 */
	public static Map<Map<String, Map<String, String>>, List<PersistenceDelta>> removeHelperVariableMappings(
			Map<Map<String, Map<MappingNames, Map<String, String>>>, List<PersistenceDelta>> mappings) {
		return mappings.entrySet().stream().collect(toMap(variationToDelta ->
				variationToDelta.getKey().entrySet().stream().collect(toMap(Map.Entry::getKey, operationToVariation ->
						operationToVariation.getValue().values().stream().reduce(new HashMap<>(), (acc, current) -> {
							Map<String, String> mapping = new HashMap<>();
							mapping.putAll(acc);
							mapping.putAll(current);
							return mapping;
						}))), Map.Entry::getValue));
	}

	/**
	 * Creates a new persistent transition with the help of two transition
	 *
	 * @param oldTransition the last seen transition or null when none was last
	 * @param newTransition the current transition from which the persistent transition should be created from
	 * @return the persistent transition
	 */
	public static PersistentTransition generateNewTransition(Transition oldTransition, Transition newTransition) {
		if (oldTransition == null) {
			return new PersistentTransition(newTransition, null);
		} else {
			return new PersistentTransition(newTransition, new PersistentTransition(oldTransition, null));
		}
	}



	/**
	 * Creates a PersistentTransition with an existing mapping
	 *
	 * @param mapping the mapping to create the PT from. Old -&gt; New
	 * @param current the current (old) transition
	 * @return the new transition
	 */
	public static PersistentTransition createPersistentTransitionFromMapping(Map<MappingNames, Map<String, String>> mapping,
																			 PersistentTransition current) {

		Map<String, String> destChangedVariables =
				mapping.get(MappingNames.VARIABLES_MODIFIED).entrySet().stream()
						.filter(entry -> current.getDestinationStateVariables().containsKey(entry.getKey()))
						.collect(toMap(Map.Entry::getValue, entry -> current.getDestinationStateVariables().get(entry.getKey())));


		Set<String> destNotChangedVariables =
				mapping.get(MappingNames.VARIABLES_READ).entrySet().stream()
						.filter(entry -> current.getDestStateNotChanged().contains(entry.getKey()))
						.map(Map.Entry::getValue).collect(toSet());


		Map<String, String> resultOutputParameters =
				mapping.get(MappingNames.OUTPUT_PARAMETERS).entrySet().stream()
						.collect(toMap(Map.Entry::getValue, entry -> current.getOutputParameters().get(entry.getKey())));


		Map<String, String> resultInputParameters =
				mapping.get(MappingNames.INPUT_PARAMETERS).entrySet().stream()
						.collect(toMap(Map.Entry::getValue, entry -> current.getParameters().get(entry.getKey())));


		return current.copyWithNewDestState(destChangedVariables).copyWithNewParameters(resultInputParameters)
				.copyWithNewOutputParameters(resultOutputParameters).copyWithDestStateNotChanged(destNotChangedVariables);
	}


	/**
	 * Returns a list with all transitions enabled
	 *
	 * @param t the trace
	 * @return return a list with all enabled transitions by name
	 */
	public static List<String> enabledOperations(Trace t) {
		
		return t.getCurrentState().getOutTransitions()
				.stream()
				.collect(toCollection(() -> new TreeSet<>(Comparator.comparing(Transition::getName))))
				.stream()
				.map(Transition::getName)
				.collect(toList());
	}

	/**
	 * The current operation cannot be found, it seems like it was renamed or removed, lets try to execute operations
	 * and hope we can satisfy the original predicates dealing with variables
	 *
	 * @param t       the trace
	 * @param current the current transition
	 * @return a list of transitions that satisfy the privilige
	 */
	public  List<Transition> renamedTransition(Trace t, PersistentTransition current, PersistentTransition last) {
		List<String> enabledOperations = enabledOperations(t);

		List<Transition> result = enabledOperations.stream()
				.flatMap(entry -> executeOperation(t, entry).stream())
				.collect(toList());

		Map<Transition, Integer> scoredPaths = scorePaths(current, last , result);


		return extractMaxScore(scoredPaths);
	}

	/**
	 * Attempts to replay the transition with the given name
	 *
	 * @param t                the trace
	 * @param p the transition to be replayed
	 * @return a successful transition
	 */
	public List<Transition> replayTransition(Trace t, PersistentTransition p) {

		StateSpace stateSpace = t.getStateSpace();

		final IEvalElement pred = stateSpace.getModel().parseFormula(replayOptions.createMapping(p).toString(), FormulaExpand.EXPAND);

		GetOperationByPredicateCommand command =
				new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(), p.getOperationName(), pred, 1);

		stateSpace.execute(command);

		return command.getNewTransitions();
	}

	/**
	 * Execute a operation with this name without further constraints
	 * @param t the current trace
	 * @param name the name of the operation to be executed
	 * @return the results
	 */
	public List<Transition> executeOperation(Trace t, String name){
		StateSpace stateSpace = t.getStateSpace();

		final IEvalElement pred = stateSpace.getModel().parseFormula(new PredicateBuilder().toString(), FormulaExpand.EXPAND);

		GetOperationByPredicateCommand command =
				new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(), name, pred, 5);

		stateSpace.execute(command);

		return command.getNewTransitions();
	}



	/**
	 * @param persistentTransition the transition to execute
	 * @param t                    the trace symbolizes the current state
	 * @return the executed operation
	 */
	public GetOperationByPredicateCommand buildTransition(PersistentTransition persistentTransition, Trace t) {

		StateSpace stateSpace = t.getStateSpace();

		PredicateBuilder predicateBuilder = replayOptions.createMapping(persistentTransition);

		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		return new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(),
				persistentTransition.getOperationName(), pred, 1);
	}

	/**
	 * @param name    the name of the operation to be executed before
	 * @param t       the trace symbolizing the current state
	 * @param current the transition to be replayed
	 * @return the command contains the newly created trace fragment
	 */
	public ConstructTraceCommand buildTransitions(String name, Trace t, PersistentTransition current) {
		return new ConstructTraceCommand(
				t.getStateSpace(),
				t.getCurrentState(),
				Arrays.asList(name, current.getOperationName()),
				Arrays.asList(
						new ClassicalB("1=1", FormulaExpand.EXPAND),
						new ClassicalB(replayOptions.createMapping(current).toString(), FormulaExpand.EXPAND)));
	}

	/**
	 * It can have multiple causes that a transition is not active as expected:
	 * 1) The operation with this name does not longer exists
	 * 2) The operation with this name is guarded behind an other operation
	 * Either way we need to find our "way" to this transition - this is the way
	 * In order to archive this goal we first "skipping" a step and see if the transition is enabled, else we assume
	 * the transition is renamed and execute the transition which will satisfy our predicates
	 *
	 * @param t          the trace
	 * @param transition the transition we are looking for
	 * @return a list of transition showing the way
	 */
	private List<Transition> findPath(Trace t, PersistentTransition transition, PersistentTransition last) {

		List<String> possibleTransitions = enabledOperations(t);
		if (possibleTransitions.contains(transition.getOperationName())) {
			return replayTransition(t, transition);
		}

		List<Transition> lookAheadResult = lookAhead(t, transition, last);

		if (!lookAheadResult.isEmpty()) {
			return lookAheadResult;
		}

		return renamedTransition(t, transition, last);

	}

	/**
	 * Skips a step and look if the current transition is enabled afterwards
	 *
	 * @param t       the trace
	 * @param current the current transition
	 * @return a list of transitions that can be taken to skip the transition
	 */
	public List<Transition> lookAhead(Trace t, PersistentTransition current, PersistentTransition last) {
		List<String> possibleTransitions = enabledOperations(t);

		List<ConstructTraceCommand> commands = possibleTransitions
				.stream()
				.map(name -> buildTransitions(name, t, current))
				.collect(toList());

		StateSpace stateSpace = t.getStateSpace();

		commands.forEach(stateSpace::execute);

		return selectBestMatch(commands, current, last);
	}


	/**
	 * Gets a list of commands and extracts the command that contains the best result
	 * @param commands the list of EXECUTED commands
	 * @param current the persistent transition to compare against
	 * @return the best match or an empty list
	 */
	public static List<Transition> selectBestMatch(List<ConstructTraceCommand> commands, PersistentTransition current, PersistentTransition last){

		List<List<Transition>> validTraces = commands.stream()
				.filter(entry -> !entry.hasErrors() && entry.getNewTransitions().size() > 0)
				.map(entry -> new ArrayList<>(entry.getNewTransitions()))
				.collect(toList());


		Map<Transition, List<Transition>> bla = validTraces.stream().collect(toMap(entry -> entry.get(entry.size() - 1), entry -> entry));

		Map<Transition, Integer> scored = scorePaths(current, last, new ArrayList<>(bla.keySet()));

		List<Transition> max = extractMaxScore(scored);

		if(max.isEmpty()){
			return emptyList();
		}
		else{
			return bla.get(max.get(0));
		}

	}


	/**
	 * Gets a map of transition lists to which a score is assigned and extracts the one with the maximum score
	 * @param scoredPaths the map with paths and scores assigned to
	 * @return the path with the highest score
	 */
	public static List<Transition> extractMaxScore(Map<Transition, Integer> scoredPaths){


		List<Transition> currentlyCollected = new ArrayList<>();
		int highestScore = 0;
		for( Map.Entry<Transition, Integer> entry : scoredPaths.entrySet()){
			if(entry.getValue() > highestScore){
				highestScore = entry.getValue();
				currentlyCollected.clear();
				currentlyCollected.add(entry.getKey());

			}else if(entry.getValue() == highestScore){
				currentlyCollected.add(entry.getKey());
			}
		}


		if(currentlyCollected.size() == 1 ){
			return singletonList(currentlyCollected.get(0));
		}



		if(currentlyCollected.size() > 1 && highestScore != 0){
			return singletonList( currentlyCollected.get(0));
		}else{
			return emptyList();
		}

	}

	/**
	 * gets a list of transition lists and scores each one of them
	 * @param original the goal the scores is calculated with
	 * @param toScore the lists to score
	 * @return the scored lists
	 */
	public static Map<Transition, Integer> scorePaths(PersistentTransition original, PersistentTransition last, List<Transition> toScore){
		return toScore.stream().collect(toMap(entry -> entry, entry -> {
			PersistentTransition persistentTransition = new PersistentTransition(entry, last);
			return (int) mapContainsMatchingElements(original.getAllPredicates(), persistentTransition.getAllPredicates());
		}));
	}

	/**
	 * Takes to maps with predicates assigned to variables. Will calculate a score representing how many variables and their
	 * predicates are matching in bot maps
	 * @param a first map
	 * @param b second map
	 * @return the score
	 */
	public static long mapContainsMatchingElements(Map<String, String> a, Map<String, String> b){
		return  b.entrySet().stream()
				.filter(entry -> a.containsKey(entry.getKey()))
				.map(entry -> a.get(entry.getKey()).equals(entry.getValue()))
				.filter(entry -> entry).count();
	}




	/**
	 * The main logic component of this class. Gets a trace and information extracted previously and tries to run the
	 * trace under the current conditions. Problems will be caught and "fixed" on the fly if sensebill and logical.
	 *
	 * @param transitionList    the trace to be replayed
	 * @param stateSpace        the statesman where the current machine lives in
	 * @param typeIVCandidates  candidates that may not accessible, removed, or renamed...
	 * @return the replayed trace in dependence to the identifier selection regarding typeIIICandidates
	 */
	public Map<Map<String, Map<MappingNames, Map<String, String>>>, List<PersistenceDelta>> replayTrace(List<PersistentTransition> transitionList,
																										StateSpace stateSpace,
																										Set<String> typeIVCandidates,
																										Set<Map<String, Map<MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys) {

		Trace trace = new Trace(stateSpace);
		trace.setExploreStateByDefault(true);

		progressMemoryInterface.nextStep();
		progressMemoryInterface.addTasks(selectedMappingsToResultsKeys.size()*transitionList.size());


		return selectedMappingsToResultsKeys.stream()
				.map(mapping -> {
					List<PersistentTransition> newTransitions = transformTransitionList(mapping, transitionList);
					List<PersistenceDelta> preparedDelta = TraceCheckerUtils.zipPreserveOrder(transitionList.stream().map(PersistentTransition::copy).collect(toList()), newTransitions)//A copy is necessary; Else objects could share the same reference leading to a incorrect zipping
							.entrySet().stream()
							.map(entry -> new PersistenceDelta(entry.getKey(), singletonList(entry.getValue())))
							.collect(toList());
					return new AbstractMap.SimpleEntry<>(mapping, createNewTransitionList(preparedDelta, trace, typeIVCandidates, mapping));
				})
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
	}

	List<PersistentTransition> newT = new ArrayList<>();

	/**
	 * Gets a prepared transition list and tries to replay the trace. Eventual problems are caught and it will be tried
	 * to find a new path to replay the trace with search depth 1. Errors when replaying are caught and a empty list of
	 * as new transition lists is returned
	 *
	 * @param persistentTransitions the prepared transition list
	 * @param t                     the initial state of the loaded machine
	 * @param typeIV                all operations that are identified as type IV
	 * @param currentTypeIIIMapping the currently used variable mapping
	 * @return the new transition list either containing a correct pathing or is empty when no pathing was found
	 */
	public List<PersistenceDelta> createNewTransitionList(List<PersistenceDelta> persistentTransitions, Trace t, Set<String> typeIV, Map<String, Map<MappingNames, Map<String, String>>> currentTypeIIIMapping) {
		Trace currentState = t;
		List<PersistenceDelta> newTransitions = new ArrayList<>();


		Set<String> usedTypeIV = new HashSet<>(typeIV);
		for (PersistenceDelta oldTPersistentTransition : persistentTransitions) {
			boolean isDirty = false;
			PersistentTransition oldPTransition = oldTPersistentTransition.getOldTransition();
			if (!usedTypeIV.contains(oldPTransition.getOperationName())) {
				try {
					Transition oldTransition = currentState.getCurrentTransition();
					Transition newTransition = replayPersistentTransition(currentState, oldTPersistentTransition.getNewTransitions().get(0));
					currentState = currentState.add(newTransition);
					PersistentTransition newPersistentTransition = generateNewTransition(oldTransition, newTransition);
					newTransitions.add(new PersistenceDelta(oldTPersistentTransition.getOldTransition(), singletonList(newPersistentTransition)));

				} catch (TransitionHasNoSuccessorException ignored) {
					isDirty = true;
				} catch (TransitionFailedToExecuteException ignore){}
			}

			if (usedTypeIV.contains(oldPTransition.getOperationName()) || isDirty) {
				List<Transition> result;

				if(newTransitions.size() ==0){
					result = findPathForInitAndSC(currentState, oldPTransition);
				}else{
					result = findPath(currentState, oldPTransition, newTransitions.get(newTransitions.size() - 1).getLast());
				}
				if (result.isEmpty()) {
					ungracefulTraces.add(newTransitions);
					return emptyList();
				} else {
					if (newTransitions.isEmpty()) {
						newTransitions.add(new PersistenceDelta(oldPTransition, PersistentTransition.createFromList(new ArrayList<>(result))));
					} else {
						PersistentTransition lastTransition = newTransitions.get(newTransitions.size() - 1).getLast();
						newTransitions.add(new PersistenceDelta(oldPTransition, PersistentTransition.createFromList(new ArrayList<>(result), lastTransition)));
					}
					currentState = currentState.addTransitions(new ArrayList<>(result));
					usedTypeIV.add(oldPTransition.getOperationName()); //Careful! pass by reference, the global state is a reference to the passed parameter in the top function, better rework this
				}

			}

			progressMemoryInterface.fulfillTask();
		}
		updatedTypeIV.put(currentTypeIIIMapping, usedTypeIV);

		return newTransitions;
	}


	/**
	 * Special case whenever the first operation of the trace exploration is a Type IV - executes init or sc blindly
	 * without enforcing predicates
	 * @param t the current trace
	 * @param transition the current transition
	 * @return the new transition
	 */
	public List<Transition> findPathForInitAndSC(Trace t, PersistentTransition transition){
		List<String> possibleTransitions = enabledOperations(t);
		if(transition.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME) && possibleTransitions.contains(Transition.INITIALISE_MACHINE_NAME)){

			return replayTransition(t, PersistentTransition.createEmptyPTransition(Transition.INITIALISE_MACHINE_NAME));
		}
		if(transition.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME) && possibleTransitions.contains(Transition.SETUP_CONSTANTS_NAME)){

			return replayTransition(t, PersistentTransition.createEmptyPTransition(Transition.SETUP_CONSTANTS_NAME));
		}

		return emptyList();
	}


	/**
	 * Replays a persistent Transition
	 *
	 * @param t                    the current state
	 * @param persistentTransition the transition to replay
	 * @return the replayed transition
	 * @throws TransitionHasNoSuccessorException replay has failed
	 */
	private Transition replayPersistentTransition(Trace t, PersistentTransition persistentTransition) throws TransitionHasNoSuccessorException, TransitionFailedToExecuteException {

		StateSpace stateSpace = t.getStateSpace();

		final GetOperationByPredicateCommand command;

		if (persistentTransition.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME)) {
			command = buildInit(persistentTransition, t);
		} else {
			command = buildTransition(persistentTransition, t);
		}

		try {
			stateSpace.execute(command);
		}catch (ProBError e){
			throw new TransitionFailedToExecuteException(persistentTransition);
		}

		if (command.getNewTransitions().size() > 1) {
			return command.getNewTransitions().get(0);
		}

		if(command.getNewTransitions().size() == 1){
			return command.getNewTransitions().get(0);
		}
		throw new TransitionHasNoSuccessorException(persistentTransition);
	}

	/**
	 * Special case for the Initialisation - if the initialisation was already treated it will be just passed
	 *
	 * @param persistentTransition the persistent transition
	 * @param t                    the trace representing the current state
	 * @return the build command
	 */
	public GetOperationByPredicateCommand buildInit(PersistentTransition persistentTransition, Trace t) {

		StateSpace stateSpace = t.getStateSpace();

		PredicateBuilder predicateBuilder;

		if (initWasSet) {
			predicateBuilder = replayOptions.createMapping(persistentTransition);
		}else{
			predicateBuilder = new PredicateBuilder();
		}

		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		return new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(),
				persistentTransition.getOperationName(), pred, 1);
	}

	public Map<Map<String, Map<MappingNames, Map<String, String>>>, Set<String>> getUpdatedTypeIV() {
		return updatedTypeIV;
	}


	public List<List<PersistenceDelta>> getUngracefulTraces(){
		return ungracefulTraces;
	}

	/**
	 * An helper datatype to better split operation infos
	 */
	public enum MappingNames {
		INPUT_PARAMETERS, OUTPUT_PARAMETERS, VARIABLES_MODIFIED, VARIABLES_READ
	}

}
