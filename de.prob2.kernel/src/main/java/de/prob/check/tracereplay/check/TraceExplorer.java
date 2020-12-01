package de.prob.check.tracereplay.check;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.github.krukow.clj_lang.PersistentVector;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.*;
import de.prob.check.tracereplay.*;
import de.prob.formula.PredicateBuilder;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.util.*;
import java.util.stream.Collectors;

public class TraceExplorer {



	public static Set<List<PersistenceDelta>> replayTrace(List<PersistentTransition> transitionList,
																						   StateSpace stateSpace,
																						   Map<String,OperationInfo> operationInfo) {

		Trace trace = new Trace(stateSpace);
		trace.setExploreStateByDefault(true);

		PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> traceStorage = PersistentHashMap.create();

		//Maybe special treatment for init and setup constants?


		for (PersistentTransition transition : transitionList) {

			Set<PersistentTransition> variations = new HashSet<>();
			variations.add(transition);
			if(operationInfo.containsKey(transition.getOperationName())){

				variations = variations.stream().flatMap(variation ->
						possibleConstellations(new HashMap<>(variation.getParameters()),
								new ArrayList<>(operationInfo.get(variation.getOperationName()).getParameterNames())).stream()
								.map(variation::copyWithNewParameters)).collect(Collectors.toSet());

				if(variations.isEmpty()) variations.add(transition);

				variations = variations.stream().flatMap(variation ->
						possibleConstellations(new HashMap<>(variation.getDestinationStateVariables()),
								new ArrayList<>(operationInfo.get(variation.getOperationName()).getWrittenVariables())).stream()
								.map(variation::copyWithNewDestState)).collect(Collectors.toSet());
			}

			if(variations.isEmpty())
			{
				variations.add(transition);
			}

			Map<Trace, Set<Trace>> pathsMapedToPathsToGo;
			if(traceStorage.isEmpty()){
				pathsMapedToPathsToGo = new HashMap<>();
				pathsMapedToPathsToGo.put(trace, replayPersistentTransition(trace, transition).stream().map(trace::add).collect(Collectors.toSet()));
			}else{
				Set<PersistentTransition> finalVariations = variations;

				pathsMapedToPathsToGo = traceStorage.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry ->
						finalVariations.stream().flatMap(variation -> replayPersistentTransition(entry.getKey(), variation).stream()
								.map(innerTransition -> entry.getKey().add(innerTransition))).collect(Collectors.toSet())));

			}


			PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> finalTraceStorage = traceStorage;
			Map<Trace, PersistentVector<PersistenceDelta>> da = pathsMapedToPathsToGo.entrySet().stream().flatMap(entry -> entry.getValue().stream()
					.map(innerEntry -> {

						PersistentTransition previous;
						if(trace.getCurrentTransition() == null){
							previous = null;
						}else{
							previous = new PersistentTransition(trace.getCurrentTransition());
						}


						return new AbstractMap.SimpleEntry<>(innerEntry,
								finalTraceStorage.getOrDefault(entry.getKey(), PersistentVector.emptyVector()).cons(
										new PersistenceDelta(transition,
												Collections.singletonList(
														new PersistentTransition(innerEntry.getCurrentTransition(), previous)))));
					}))
					.collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


			Map<Trace, PersistentVector<PersistenceDelta>> trueResult = new HashMap<>();
			for(Trace t : da.keySet()){
				if (!trueResult.containsKey(t)&&!setContainsEqualTrace(t, trueResult.keySet())) {
					trueResult.put(t, da.get(t));
				}
			}


			traceStorage = PersistentHashMap.create(trueResult);
		}


		return traceStorage.values().stream().map(ArrayList::new).collect(Collectors.toSet());
	}




	/**
	 * Gets a mapping from variables to values (input/output/variables) and the variables of the current operation and
	 * calculates all possible mappings, e.g.
	 * inc(1,2) -> inc(x,y,z) produces
	 * 			-> inc(1, 2, ?)
	 * 			-> inc(1, ?, 2)
	 * 			-> inc(?, 1, 2)
	 * 		[..]
	 * void means that the mapping is not existent in the resulting map
	 * @param elements the mapping from the current transition
	 * @param target the variables manipulated by the transition according to the new machine
	 * @return all possible mappings
	 */
	public static Set<Map<String, String>> possibleConstellations(Map<String, String> elements, List<String> target){

		if(elements.isEmpty()||target.isEmpty()) return Collections.emptySet();

		List<String> values = new ArrayList<>(elements.keySet());
		Set<Map<String, String>> result;
		if(values.size()>target.size()){
			result =  permutate(TraceCheckerUtils.generatePerm(values), new ArrayList<>(target));
		}
		else {
			result =  permutate(TraceCheckerUtils.generatePerm(new ArrayList<>(target)), values);
		}

		return result.stream().map(entry ->
				entry.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, inner -> elements.get(inner.getKey())))).collect(Collectors.toSet());
	}

	/**
	 * helper for @possibleConstellations
	 * @param permutations a list with all permutations
	 * @param values the values to map onto
	 * @return a set of mappings with the new constellations
	 */
	public static Set<Map<String, String>> permutate(List<List<String>> permutations, List<String> values){
		Set<Map<String, String>> resultSet = new HashSet<>();
		for(List<String> option : permutations){
			Map<String, String> resultMap = new HashMap<>();
			for(int i = 0; i < option.size(); i++){
				if(i < values.size()){
					resultMap.put(values.get(i), option.get(i));
				}
				//Else the values are not for direct interest when building the predicate
			}
			resultSet.add(resultMap);
		}

		return resultSet;
	}

	/**
	 * Compares a trace and a set of traces on the property of their equivalence. Equivalence is archived if the
	 * contained transitions list can be transformed to persistent transitions and those are equivalent
	 * @param t the trace to compare
	 * @param set the set of traces to compare against
	 * @return wherever the set contains the given trace t
	 */
	public static boolean setContainsEqualTrace(Trace t, Set<Trace> set){
		return set.stream().anyMatch(trace -> {
			List<PersistentTransition> transitionList1 = trace.getTransitionList().stream()
					.map(transition -> new PersistentTransition(transition, null)).collect(Collectors.toList());
			List<PersistentTransition> transitionList2 = t.getTransitionList().stream()
					.map(transition -> new PersistentTransition(transition, null)).collect(Collectors.toList());

			return transitionList1.equals(transitionList2);
		});
	}


	//Evtl. Return map<PersistentTransition, Transition>
	private static Set<Transition> replayPersistentTransition(Trace t, PersistentTransition persistentTransition) {


		Map<Constraint, GetOperationByPredicateCommand>  possibleSolutions = Arrays.stream(Constraint.values()).collect(Collectors.toMap(entry -> entry,
				entry ->{
					StateSpace stateSpace = t.getStateSpace();

					final GetOperationByPredicateCommand command =  commandBuild(stateSpace, persistentTransition, t, entry);

					stateSpace.execute(command);

					return command;
				})).entrySet().stream().filter(entry -> !entry.getValue().getNewTransitions().isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


		Set<Transition> gna = possibleSolutions.entrySet().stream().flatMap(entry -> entry.getValue().getNewTransitions().stream()).collect(Collectors.toSet());

		return gna;
	}

	enum Constraint{
		FULL, ONLY_PARAMETER, PARAMETER_AND_DESTINATION_STATE, PARAMETER_AND_OUTPUT, DESTINATION_STATE_AND_OUTPUT
	}

	public static GetOperationByPredicateCommand commandBuild(StateSpace stateSpace, PersistentTransition persistentTransition,
															  Trace t, Constraint constraint){

		PredicateBuilder predicateBuilder = new PredicateBuilder();

		switch (constraint){
			case FULL:
				predicateBuilder.addMap(persistentTransition.getParameters());
				predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());
				predicateBuilder.addMap(persistentTransition.getOutputParameters());
				break;
			case ONLY_PARAMETER:
				predicateBuilder.addMap(persistentTransition.getParameters());
				break;
			case PARAMETER_AND_OUTPUT:
				predicateBuilder.addMap(persistentTransition.getParameters());
				predicateBuilder.addMap(persistentTransition.getOutputParameters());
				break;
			case DESTINATION_STATE_AND_OUTPUT:
				predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());
				predicateBuilder.addMap(persistentTransition.getOutputParameters());
				break;
			case PARAMETER_AND_DESTINATION_STATE:
				predicateBuilder.addMap(persistentTransition.getParameters());
				predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());
		}

		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		return new GetOperationByPredicateCommand(stateSpace, t.getCurrentState().getId(),
				persistentTransition.getOperationName(), pred, 1);
	}


	public static List<Transition> resultChecker(ReplayOptions options, List<Transition> possibleTransitions,
												 PersistentTransition persistentTransition, StateSpace stateSpace){

		return possibleTransitions.stream().filter(transition -> {

			if(!options.checkDestState()) return true;

			Map<String, String> results = convertResult(transition);

			Map<String, String> comparator = filterFromTransition(persistentTransition.getDestinationStateVariables(), results);

			return comparator.isEmpty();
		}).filter(transition -> {

			if(!options.checkDestStateNotChanged()) return true;


			Map<String, String> results = convertResult(transition);

			Set<String> comparator = persistentTransition.getDestStateNotChanged().stream()
					.filter(results::containsKey).collect(Collectors.toSet());

			return comparator.isEmpty();
		}).filter(transition -> {

			if(!options.checkOutput()) return true;

			List<String> outputParameterNames = stateSpace.getLoadedMachine().getOperations()
					.get(persistentTransition.getOperationName()).getOutputParameterNames();

			Map<String, String> toCompare = TraceCheckerUtils.zip(outputParameterNames, transition.getReturnValues());

			Map<String , String> comparator = filterFromTransition(persistentTransition.getOutputParameters(), toCompare);

			return comparator.isEmpty();
		}).collect(Collectors.toList());
	}


	public static Map<String, String> convertResult(Transition transition){
		Map<ClassicalB, EvalResult> converted = transition.getDestination().getVariableValues(FormulaExpand.EXPAND)
				.entrySet().stream().collect(Collectors.toMap(key -> (ClassicalB) key.getKey(), key -> (EvalResult) key.getValue()));
		return converted.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().getValue()));

	}

	public static Map<String, String> filterFromTransition(Map<String, String> persistentTransition, Map<String, String> toCompare){
		return persistentTransition.entrySet().stream()
				.filter(entry -> toCompare.containsKey(entry.getKey()) &&
						toCompare.get(entry.getKey()).equals(entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
