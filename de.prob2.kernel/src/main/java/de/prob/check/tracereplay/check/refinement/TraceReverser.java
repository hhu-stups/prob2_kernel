package de.prob.check.tracereplay.check.refinement;

import com.google.inject.Injector;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.TraceReplay;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.model.eventb.*;
import de.prob.model.representation.*;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class TraceReverser {

	private final Injector injector;
	private final List<PersistentTransition> transitionList;
	private final Path alpha;
	private final Path beta;

	public TraceReverser(Injector injector, List<PersistentTransition> transitionList, Path alpha, Path beta) {
		this.injector = injector;
		this.transitionList = transitionList;
		this.alpha = alpha;
		this.beta = beta;
	}




	/**
	 * Let A and B be two machines. Let T be a Trace created on B. Let B refine A. This function return the T
	 * recalculated for A.
	 * @return a trace for an abstract machine.
	 * @throws IOException file reading went wrong
	 */
	/*
	public List<PersistentTransition> reverseEventBTrace() throws IOException {

		AbstractModel model = loadEventBFileAsStateSpace().getModel();

		Machine topLevelMachine = model.getChildrenOfType(Machine.class).get(model.getGraph().getStart());

		ModelElementList<Event> eventList = topLevelMachine.getChildrenOfType(Event.class);

		Map<String, String> newOldMapping = eventList.stream()
				.collect(toMap(BEvent::getName, entry -> traceEvent(entry).getName()));

		String original = model.getGraph().getStart();
		String target = model.getGraph().getOutEdges(original).stream().filter(entry -> entry.getRelationship().equals(DependencyGraph.ERefType.REFINES)).collect(toList()).get(0).getTo().getElementName();

		ModelElementList<Machine> modelList = model.getChildrenOfType(Machine.class);
		List<EventBMachine> theBetterList = modelList.stream().map(entry -> (EventBMachine) entry).collect(toList());

		Map<String, EventBMachine> theBetterMap = theBetterList.stream().filter(entry -> entry.getName().equals(original) || entry.getName().equals(target)).collect(toMap(Machine::getName, entry -> entry));

		List<String> originalVars = theBetterMap.get(original).getVariables().stream().map(EventBVariable::getName).collect(toList());
		List<String> targetVars = theBetterMap.get(target).getVariables().stream().map(EventBVariable::getName).collect(toList());


		List<String> originalConst = extractConst(theBetterMap.get(original));
		List<String> targetConst  = extractConst(theBetterMap.get(target));


		List<String> machineExclusiveVars = originalVars.stream().filter(entry -> !targetVars.contains(entry)).collect(toList());
		List<String> machineExclusiveConst = originalConst.stream().filter(entry -> !targetConst.contains(entry)).collect(toList());

		Map<String, List<String>> machineOriginalParams = theBetterMap.get(original).getChildrenOfType(Event.class).stream()
				.collect(toMap(BEvent::getName, entry -> entry.getChildrenOfType(EventParameter.class).stream()
						.map(EventParameter::getName)
						.collect(toList())));

		Map<String, List<String>> machineTargetParams = theBetterMap.get(target).getChildrenOfType(Event.class).stream()
				.collect(toMap(BEvent::getName, entry -> entry.getChildrenOfType(EventParameter.class).stream()
						.map(EventParameter::getName)
						.collect(toList())));


		List<PersistentTransition> removedSkip = transitionList.stream()
				.filter(entry -> {
					if(!entry.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME) && !entry.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME)) {
						String name = entry.getOperationName();
						return !newOldMapping.get(name).equals("skip");
					}
					return true;
				})
				.collect(toList());

		Map<String, List<String>> removedEventParas = machineOriginalParams.entrySet().stream()
				.filter(entry -> !newOldMapping.get(entry.getKey()).equals("skip"))
				.collect(toMap(Map.Entry::getKey, entry -> {
					String nameInAbstract = newOldMapping.get(entry.getKey());
					List<String> parametersOfAbstract = machineTargetParams.get(nameInAbstract);
					return entry.getValue().stream()
							.filter(innerEntry -> !parametersOfAbstract.contains(innerEntry))
							.collect(toList());
				}));

		List<PersistentTransition> removedParameters = predicateRemoverParameters(removedSkip, removedEventParas);
		List<PersistentTransition> removedPredVars = predicateRemover(removedParameters, machineExclusiveVars);
		List<PersistentTransition> removedPredConst = predicateRemover(removedPredVars, machineExclusiveConst);



		List< PersistentTransition> listReady = removedPredConst.stream().map(entry -> {
			if(entry.getOperationName().equals(Transition.SETUP_CONSTANTS_NAME)||entry.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME)){
				return entry;
			}else{
				return entry.copyWithNewName(newOldMapping.get(entry.getOperationName()));
			}
		}).collect(toList());


		StateSpace stateSpace2 = TraceCheckerUtils.createStateSpace(beta.toString(), injector);
		Trace result = TraceReplay.replayTrace(new PersistentTrace("", listReady), stateSpace2);

		return PersistentTransition.createFromList(new ArrayList<>(result.getTransitionList()));
	}
*/
	/**
	 * Extracts all constants of an EventB Machine
	 * @param target the machine to extract from
	 * @return the constants
	 */
	private static List<String> extractConst(EventBMachine target){
		if(!target.getChildrenOfType(de.prob.model.eventb.Context.class).isEmpty())
		{
			return target.getChildrenOfType(Context.class).get(0).getConstants().stream().map(EventBConstant::getName).collect(toList());
		}else{
			return emptyList();
		}

	}

	/**
	 * Helper method for the trace reversal. Remove predicates from parameters that are not present in the target machine.
	 * @param persistentTransitionList the transition list to remove predicates from
	 * @param machineExclusive the predicates that have to be removed
	 * @return the cleansed list
	 */
	private List<PersistentTransition> predicateRemoverParameters(List<PersistentTransition> persistentTransitionList, Map<String, List<String>> machineExclusive) {
		return persistentTransitionList.stream()
				.map(entry -> entry.copyWithNewParameters(entry.getParameters().entrySet().stream()
						.filter(innerEntry -> !machineExclusive.get(entry.getOperationName()).contains(innerEntry.getKey()))
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue))))
				.collect(toList());


	}

	/**
	 * Helper method for the trace reversal. Remove predicates that are not present in the target machine.
	 * @param persistentTransitionList the transition list to remove predicates from
	 * @param machineExclusive the predicates that have to be removed
	 * @return the cleansed list
	 */
	private List<PersistentTransition> predicateRemover(List<PersistentTransition> persistentTransitionList, List<String> machineExclusive){
		return persistentTransitionList.stream()
				.map(entry -> entry.copyWithNewDestState(entry.getDestinationStateVariables()
						.entrySet()
						.stream()
						.filter(innerEntry -> !machineExclusive.contains(innerEntry.getKey()))
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue))))
				.map(entry -> entry.copyWithNewOutputParameters(entry.getOutputParameters()
						.entrySet()
						.stream()
						.filter(innerEntry -> !machineExclusive.contains(innerEntry.getKey()))
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue))))
				.map(entry -> entry.copyWithNewParameters(entry.getParameters()
						.entrySet()
						.stream()
						.filter(innerEntry -> !machineExclusive.contains(innerEntry.getKey()))
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue))))
				.collect(toList());
	}

}
