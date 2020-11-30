package de.prob.check.tracereplay.check;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.github.krukow.clj_lang.PersistentHashSet;
import com.github.krukow.clj_lang.PersistentVector;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.*;
import de.prob.check.tracereplay.*;
import de.prob.formula.PredicateBuilder;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;
import jdk.jfr.TransitionTo;

import java.util.*;
import java.util.concurrent.TransferQueue;
import java.util.stream.Collectors;

public class TraceExplorer {



	public static PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> replayTrace(List<PersistentTransition> transitionList, StateSpace stateSpace,
														 ReplayOptions options, Set<String> typeIVBodyCandidates,
														 Set<String> typeIIICandidates, Map<String,OperationInfo> operationInfo) {

		Trace trace = new Trace(stateSpace);
		trace.setExploreStateByDefault(true);

		PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> traceStorage = PersistentHashMap.create();

		//Maybe special treatment for init and setup constants?


		for (PersistentTransition transition : transitionList) {
			List<PersistentTransition> variations;

			if(!transition.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME)) {
				List<String> da = operationInfo.get(transition.getOperationName()).getWrittenVariables();
				da.addAll(operationInfo.get(transition.getOperationName()).getNonDetWrittenVariables());

				variations = variationFinder(transition, da).stream().map(transition::createFromOld).collect(Collectors.toList());

				if(variations.isEmpty()){
					variations.add(transition);
				}
			}
			else{
				variations = new ArrayList<>();
				variations.add(transition);
			}



			Map<Trace, Set<Trace>> result1 = traceStorage.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
					entry -> variations.stream().flatMap(element -> replayPersistentTransition(entry.getKey(), element).stream())
							.map(innerTransition -> entry.getKey().add(innerTransition)).collect(Collectors.toSet())));


			PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> finalTraceStorage = traceStorage;
			traceStorage = PersistentHashMap.create(result1.entrySet().stream().flatMap(entry -> entry.getValue().stream()
					.map(innerEntry -> new AbstractMap.SimpleEntry<>(innerEntry,
							PersistentVector.create(finalTraceStorage.get(entry.getKey()),
									new PersistenceDelta(transition,
											Collections.singletonList(
													new PersistentTransition(innerEntry.getCurrentTransition(),
															new PersistentTransition(entry.getKey().getCurrentTransition())))))))));
		}


		return traceStorage;
	}



	public static Set<Map<String, String>> variationFinder(PersistentTransition transition, List<String> manipulatedVars){
		List<String> values = new ArrayList<>(transition.getDestinationStateVariables().values());
		return TraceCheckerUtils.generatePerm(manipulatedVars).stream()
				.map(current -> TraceCheckerUtils.zip(current, values)).collect(Collectors.toSet());
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


		return possibleSolutions.entrySet().stream().flatMap(entry -> entry.getValue().getNewTransitions().stream()).collect(Collectors.toSet());

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
