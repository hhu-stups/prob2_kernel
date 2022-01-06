package de.prob.check.tracereplay.check.traceConstruction;

import de.prob.animator.command.FindPathCommand;
import de.prob.animator.command.RefineTraceCommand;
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
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


public class AdvancedTraceConstructor {

	@Deprecated
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


	/**
	 * Prepares the adaptation for a trace for Event - B
	 * @param persistentTrace the trace to be adapted
	 * @param stateSpace the machine to adapt the trace for
	 * @return the adapted trace
	 * @throws TraceConstructionError trace could not be adapted
	 */
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


	/**
	 * Prepares the adaptation for a trace for Event - B
	 * @param persistentTrace the trace
	 * @param stateSpace the state space to adapt the trace for
	 * @param alternatives alternatives for each event, as events can be refined by multiple others or can change names
	 * @param refinedAlternatives an collection that contains explicit those events that are refined multiple times
	 * @param skips the events that refine a former skip
	 * @return the adapted trace
	 * @throws TraceConstructionError trace adaptation failed
	 */
	public static List<Transition> constructTraceEventB(List<PersistentTransition> persistentTrace, StateSpace stateSpace, Map<String, List<String>> alternatives, List<String> refinedAlternatives, List<String> skips) throws TraceConstructionError {

		List<PersistentTransition> modifiedTrace = prepareTrace(new Trace(stateSpace), persistentTrace);

		List<String> transitionNames = modifiedTrace.stream()
				.map(PersistentTransition::getOperationName)
				.collect(toList());

		List<String> refiningEvents = alternatives.entrySet().stream()
				.filter(entry -> entry.getValue().stream().anyMatch(refinedAlternatives::contains))
				.map(Map.Entry::getKey).collect(toList());

		List<EventB> predicates = modifiedTrace.stream()
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

		RefineTraceCommand refineTraceCommand = new RefineTraceCommand(stateSpace, stateSpace.getRoot(), transitionNames, predicates, alternatives, refinedAlternatives,  skips);
		stateSpace.execute(refineTraceCommand);

		if (refineTraceCommand.hasErrors() && refineTraceCommand.getNewTransitions().size() < persistentTrace.size()) { //The command failed in finding an complete Trace
			throw new TraceConstructionError(refineTraceCommand.getErrors(), refineTraceCommand.getNewTransitions());
		} else {
			return refineTraceCommand.getNewTransitions();
		}
	}

	/**
	 * When a machine uses setup constants and the trace not, a dummy SC is inserted
	 * @param s the trace representing the current machine
	 * @param persistentTransitionList the trace
	 * @return the fixed or old trace
 	 */
	public static List<PersistentTransition> prepareTrace(Trace s, List<PersistentTransition> persistentTransitionList){

		boolean traceHasSC = persistentTransitionList.stream()
				.anyMatch(entry -> entry.getOperationName()
						.equals(Transition.SETUP_CONSTANTS_NAME));

		boolean machineHasSc = s.getNextTransitions().stream()
				.anyMatch(entry -> entry.getName()
						.equals(Transition.SETUP_CONSTANTS_NAME));

		if(!traceHasSC && machineHasSc){
			ArrayList<PersistentTransition> result = new ArrayList<>(persistentTransitionList);
			result.add(0, new PersistentTransition(Transition.SETUP_CONSTANTS_NAME));
			return result;
		}

		return persistentTransitionList;

	}


}
