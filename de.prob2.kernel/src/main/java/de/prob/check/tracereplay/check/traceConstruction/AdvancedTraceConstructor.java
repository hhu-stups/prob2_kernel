package de.prob.check.tracereplay.check.traceConstruction;

import de.prob.animator.command.FindPathCommand;
import de.prob.animator.command.RefineTraceCommand;
import de.prob.animator.command.RefineTraceEventBCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.formula.PredicateBuilder;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class AdvancedTraceConstructor {


	public static List<Transition> constructTraceWithOptions(List<PersistentTransition> persistentTrace, StateSpace stateSpace, ReplayOptions replayOptions) throws TraceConstructionError {

		List<PersistentTransition> modifiedTrace = prepareTrace(new Trace(stateSpace), persistentTrace);

		List<String> transitionNames = modifiedTrace.stream()
				.map(PersistentTransition::getOperationName)
				.collect(toList());
		List<ClassicalB> predicates = modifiedTrace.stream()
				.map(entry -> new ClassicalB(replayOptions.createMapping(entry).toString(), FormulaExpand.EXPAND))
				.collect(toList());

		FindPathCommand constructTraceCommand = new FindPathCommand(stateSpace, stateSpace.getRoot(), transitionNames, predicates);
		stateSpace.execute(constructTraceCommand);

		if (constructTraceCommand.hasErrors() && constructTraceCommand.getNewTransitions().size() < persistentTrace.size()) {
			throw new TraceConstructionError(constructTraceCommand.getErrors(), constructTraceCommand.getNewTransitions());
		} else {
			return constructTraceCommand.getNewTransitions();
		}
	}



	public static List<Transition> constructTraceByName(List<PersistentTransition> persistentTrace, StateSpace stateSpace) throws TraceConstructionError {
		return constructTraceWithOptions(persistentTrace, stateSpace, ReplayOptions.replayJustNames());
	}

	public static List<Transition> constructTrace(List<PersistentTransition> persistentTrace, StateSpace stateSpace) throws TraceConstructionError {

		List<PersistentTransition> modifiedTrace = prepareTrace(new Trace(stateSpace), persistentTrace);

		List<String> transitionNames = modifiedTrace.stream()
				.map(PersistentTransition::getOperationName)
				.collect(toList());
		List<ClassicalB> predicates = modifiedTrace.stream()
				.map(entry -> new ClassicalB(new PredicateBuilder().addMap(entry.getAllPredicates()).toString(), FormulaExpand.EXPAND))
				.collect(toList());

		RefineTraceCommand refineTraceCommand = new RefineTraceCommand(stateSpace, stateSpace.getRoot(), transitionNames, predicates);
		stateSpace.execute(refineTraceCommand);

		if (refineTraceCommand.hasErrors() && refineTraceCommand.getNewTransitions().size() < persistentTrace.size()) {
			throw new TraceConstructionError(refineTraceCommand.getErrors(), refineTraceCommand.getNewTransitions());
		} else {
			return refineTraceCommand.getNewTransitions();
		}
	}

	public static List<Transition> constructTraceEventB(List<PersistentTransition> persistentTrace, StateSpace stateSpace, Map<String, List<String>> alternatives, List<String> refinedAlternatives, List<String> skips) throws TraceConstructionError {

		Trace modifiedTrace = prepareTrace2(new Trace(stateSpace), persistentTrace);
		modifiedTrace.setExploreStateByDefault(true);
		List<PersistentTransition> modifiedList = prepareTraceList(modifiedTrace, persistentTrace);

		List<String> transitionNames = modifiedList.stream()
				.map(PersistentTransition::getOperationName)
				.collect(toList());


		List<String> refiningEvents = alternatives.entrySet().stream()
				.filter(entry -> entry.getValue().stream().anyMatch(refinedAlternatives::contains))
				.map(Map.Entry::getKey).collect(toList());


		List<EventB> predicates = modifiedList.stream()
				.map(entry -> {
					if(refiningEvents.contains(entry.getOperationName())){ //A refining event does not now its former parameters, keeping them would lead to failing predicates
						Map<String, String > mapWithoutParameters = entry.getAllPredicates().entrySet().stream()
								.filter(innerEntry -> !entry.getParameters().containsKey(innerEntry.getKey()))
								.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
						return new EventB(new PredicateBuilder().addMap(mapWithoutParameters).toString(), FormulaExpand.EXPAND);
					}else {
						return new EventB(new PredicateBuilder().addMap(entry.getAllPredicates()).toString(), FormulaExpand.EXPAND);
					}
				})
				.collect(toList());

		RefineTraceEventBCommand refineTraceCommand = new RefineTraceEventBCommand(modifiedTrace.getStateSpace(), modifiedTrace.getCurrentState(), transitionNames, predicates, alternatives, refinedAlternatives,  skips);
		stateSpace.execute(refineTraceCommand);

		if (refineTraceCommand.hasErrors() && refineTraceCommand.getNewTransitions().size() < persistentTrace.size()) { //The command failed in finding an complete Trace
			throw new TraceConstructionError(refineTraceCommand.getErrors(), refineTraceCommand.getNewTransitions());
		} else {
			return refineTraceCommand.getNewTransitions();
		}
	}

	public static List<PersistentTransition> prepareTrace(Trace t, List<PersistentTransition> persistentTransitionList){
		boolean traceHasSC = possesOperationNamePT(persistentTransitionList, Transition.SETUP_CONSTANTS_NAME);

		boolean machineHasSc = possesOperationNameT(t.getNextTransitions(), Transition.SETUP_CONSTANTS_NAME);



		if(!traceHasSC && machineHasSc){
			ArrayList<PersistentTransition> result = new ArrayList<>(persistentTransitionList);
			result.add(0, new PersistentTransition(Transition.SETUP_CONSTANTS_NAME));
			return result;
		}

		return persistentTransitionList;

	}




	/**
	 * Similar to prepareTrace for the other direction. The Trace has more elements than the machine has, therefore the
	 * trace is reworked for the needs of the machine
	 * @param t the machine represented as trace
	 * @param persistentTransitionList the trace to replay
	 * @return the modified trace
	 *
     */
	public static List<PersistentTransition> prepareTraceList(Trace t, List<PersistentTransition> persistentTransitionList){
		boolean scWasSet = possesOperationNameT(t.getTransitionList(), Transition.SETUP_CONSTANTS_NAME);

		return persistentTransitionList.stream()
				.filter(entry -> !(entry.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME) && scWasSet))
				.collect(Collectors.toList());
	}


	/**
	 * Prepares a trace depending on the machine and the trace to replay. The problem is the following:
	 * A loaded machine represented by t can have constants. The trace might have not constants. What to do? Replay will
	 * fail when starting from root, as a SC execution is expected but in the trace there is none. The method will therefore
	 * take care of executing SC before and return a so prepared trace.
	 *
	 * @param t the currently loaded machine at point root
	 * @param persistentTransitionList the trace to investigate
	 * @return a prepared trace that has the correct starting point set
	 */
	public static Trace prepareTrace2(Trace t, List<PersistentTransition> persistentTransitionList){
		boolean traceHasSC = possesOperationNamePT(persistentTransitionList, Transition.SETUP_CONSTANTS_NAME);

		boolean machineHasSc = possesOperationNameT(t.getNextTransitions(), Transition.SETUP_CONSTANTS_NAME);

		Trace toReturn = t;

		if(!traceHasSC && machineHasSc){
			toReturn = t.add(t.getNextTransitions().stream()
					.filter(entry -> entry.getName().equals(Transition.SETUP_CONSTANTS_NAME))
					.collect(toList()).get(0));
		}

		return toReturn;

	}

	//To small helpers as the contains function does not work properly on complex data structures
	public static boolean possesOperationNameT(Collection<Transition> list, String name){
		return list.stream().anyMatch(entry -> entry.getName().equals(name));
	}

	public static boolean possesOperationNamePT(Collection<PersistentTransition> list, String name){
		return list.stream().anyMatch(entry -> entry.getOperationName().equals(name));
	}


}
