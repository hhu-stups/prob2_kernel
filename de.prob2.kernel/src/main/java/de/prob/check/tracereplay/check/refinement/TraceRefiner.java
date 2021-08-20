package de.prob.check.tracereplay.check.refinement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import com.google.inject.Injector;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.TraceReplay;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.traceConstruction.AdvancedTraceConstructor;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.model.eventb.*;
import de.prob.model.representation.*;
import de.prob.scripting.EventBFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

public class TraceRefiner {
	private final Injector injector;
	private final List<PersistentTransition> transitionList;
	private final Path alpha;
	private final Path beta;

	public TraceRefiner(Injector injector, List<PersistentTransition> transitionList, Path alpha, Path beta) {
		this.injector = injector;
		this.transitionList = transitionList;
		this.alpha = alpha;
		this.beta = beta;
	}




	/**
	 * Refines the trace for the given machine. Let A and B be machines. B refines A. A = alpha, B = Beta
	 * @return the trace if it works on the machine
	 * @throws IOException something went wrong when parsing the file
	 * @throws BCompoundException something in the file was wrong with the machine
	 * @throws TraceConstructionError something went wrong when constructing the trace
	 */
	public List<PersistentTransition> refineTrace() throws IOException, BCompoundException, TraceConstructionError {
		switch (alpha.toString().substring(alpha.toString().lastIndexOf("."))){
			case ".mch":
			case ".ref":
			case ".imp":
				return refineTraceClassicalB();
			case ".bum":
				return refineTraceEventB();
			default:
				throw new IOException("file not suitable for refinement replay");
		}
	}

	/**
	 * Reverses a given trace crated on B back to A if possible.
	 * @return
	 */
	public List<PersistentTransition> reverseTrace(){
		return null;
	}

	/**
	 * Checks an EventB machine if it is able to perform the given Trace. For the algorithm see prolog code.
	 * The idea of this method is to already do some static precalculation before letting ProB solve the problem.
	 * The precalculations are the theoretical possible alternatives for each situation. For example:
	 * Let T be a Trace. Let p_e and p_g be two operation such that T = p_e, p_g, p_e, p_e....
	 * Let p_ee and p_eee refine p_e and p_g refine p_g then this method will prepare
	 * T to be [[p_ee, p_eee][p_g]....
	 * Further the method will prepare the difference between explicit refined and extended events and skip events.
	 * @return The adapted Trace
	 * @throws IOException File reading went wrong
	 * @throws TraceConstructionError no suitable adaptation was found
	 */
	private List<PersistentTransition> refineTraceEventB() throws IOException, TraceConstructionError {
		StateSpace stateSpace = loadEventBFileAsStateSpace();

		AbstractModel model = stateSpace.getModel();

		Machine topLevelMachine = model.getChildrenOfType(Machine.class).get(model.getGraph().getStart());

		ModelElementList<Event> eventList = topLevelMachine.getChildrenOfType(Event.class);

		List<Event> refinedEvents = topLevelMachine.getChildrenOfType(Event.class)
				.stream()
				.filter(entry -> !entry.isExtended())
				.collect(toList());

		Map<String, String> newOldMapping = eventList.stream()
				.collect(toMap(BEvent::getName, entry -> traceEvent(entry).getName()));

		List<String> introducedBySkip = newOldMapping.entrySet().stream()
				.filter(s -> s.getValue().equals("skip"))
				.map(Map.Entry::getKey)
				.collect(toList());

		List<String> formallyRefined = refinedEvents.stream()
				.filter(event -> !event.getWitnesses().isEmpty())
				.map(BEvent::getName)
				.collect(toList());

		Map<String, List<String>> alternatives = newOldMapping.entrySet().stream()
				.collect(groupingBy(Map.Entry::getValue))
				.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
						.map(Map.Entry::getKey)
						.collect(toList())));


		alternatives.remove("INITIALISATION");
		alternatives.put(Transition.INITIALISE_MACHINE_NAME, Collections.singletonList(Transition.INITIALISE_MACHINE_NAME));
		alternatives.put(Transition.SETUP_CONSTANTS_NAME, Collections.singletonList(Transition.SETUP_CONSTANTS_NAME));

		List<Transition> resultRaw = AdvancedTraceConstructor.constructTraceEventB(transitionList, stateSpace, alternatives, formallyRefined, introducedBySkip);


		return PersistentTransition.createFromList(resultRaw);
	}

	/**
	 * Traces an event back to its origins and returns the original event, return event if it was never refined
	 * @param event the event to trace
	 * @return the origin event
	 */
	private static Event traceEvent(Event event){
		if(event.getRefines().isEmpty()){
			return new Event("skip", Event.EventType.ORDINARY, true);
		}else{
			return event.getRefines().get(0); //In EventB there should only be one Event refined from
		}
	}

	/**
	 * Loads an EventB File into a new StateSpace
	 * @return the file loaded into the state space
	 * @throws IOException file reading went wrong
	 */
	private StateSpace loadEventBFileAsStateSpace() throws IOException {
		ReusableAnimator animator = injector.getInstance(ReusableAnimator.class);
		StateSpace stateSpace = animator.createStateSpace();
		EventBFactory eventBFactory = injector.getInstance(EventBFactory.class);
		eventBFactory.extract(alpha.toString()).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}

	/**
	 * Let A and B be two machines. Let T be a Trace created on B. Let B refine A. This function return the T
	 * recalculated for A.
	 * @return a trace for an abstract machine.
	 * @throws IOException file reading went wrong
	 */
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

	/**
	 * Checks an EventB machine if it is able to perform the given Trace. For the algorithm see prolog code.
	 * For that both machines are merged and run and the original trace is executed.
	 * @return The transformed trace
	 * @throws IOException file reading went wrong
	 * @throws BCompoundException predicate translation went wrong
	 * @throws TraceConstructionError trace could not be found
	 */
	private List<PersistentTransition> refineTraceClassicalB() throws IOException, BCompoundException, TraceConstructionError {
		BParser alphaParser = new BParser(alpha.toString());
		Start alphaStart = alphaParser.parseFile(alpha.toFile(), false);

		BParser betaParser = new BParser(beta.toString());
		Start betaStart = betaParser.parseFile(beta.toFile(), false);

		NodeCollector nodeCollector = new NodeCollector(alphaStart);
		ASTManipulator astManipulator = new ASTManipulator(betaStart, nodeCollector);

		AAbstractMachineParseUnit aAbstractMachineParseUnit = (AAbstractMachineParseUnit) astManipulator.getStart().getPParseUnit();

		PrettyPrinter prettyPrinter = new PrettyPrinter();
		prettyPrinter.caseAAbstractMachineParseUnit(aAbstractMachineParseUnit);

		File tempFile = File.createTempFile("machine", ".mch", alpha.getParent().toFile());


		FileWriter writer = new FileWriter(tempFile);
		writer.write(prettyPrinter.getPrettyPrint());
		writer.close();


		StateSpace stateSpace = TraceCheckerUtils.createStateSpace(tempFile.toPath().toString(), injector);

		List<Transition> resultRaw = AdvancedTraceConstructor.constructTraceByName(transitionList, stateSpace);

		return PersistentTransition.createFromList(resultRaw);
	}




}
