package de.prob.check.tracereplay.check.refinement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Set;

import com.google.inject.Injector;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.traceConstruction.AdvancedTraceConstructor;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.model.eventb.*;
import de.prob.model.representation.*;
import de.prob.scripting.EventBFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;

public class TraceRefiner {
	private final Injector injector;
	private final List<PersistentTransition> transitionList;
	private final Path alpha; //Machine the trace comes from
	private final Path beta; //Machine the trace has to be adapted to

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
		alternatives.put(Transition.INITIALISE_MACHINE_NAME, singletonList(Transition.INITIALISE_MACHINE_NAME));
		alternatives.put(Transition.SETUP_CONSTANTS_NAME, singletonList(Transition.SETUP_CONSTANTS_NAME));

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


	public List<PersistentTransition> horizontalRefinement() throws IOException, BCompoundException, TraceConstructionError {
		BParser betaParser = new BParser(beta.toString());
		Start betaStart = betaParser.parseFile(beta.toFile(), false);

		OperationsFinder operationsFinder = new OperationsFinder();
		operationsFinder.explore(betaStart);

		StateSpace stateSpace = TraceCheckerUtils.createStateSpace(beta.toString(), injector);

		Set<String> promoted = operationsFinder.getPromoted();
		Map<String, Set<String>> internal = operationsFinder.usedOperationsReversed();

		Set<String> usedOperations = TraceCheckerUtils.usedOperations(transitionList);

		Map<String, List<String>> alternatives = usedOperations.stream().collect(toMap(entry -> entry, entry -> {
			Set<String> result = new HashSet<>();
			if(promoted.contains(entry)){
				result.add(entry);
			}else if(internal.containsKey(entry)){
				result.addAll(internal.get(entry));
			}
			return new ArrayList<>(result);
		}));

		alternatives.remove("INITIALISATION");
		alternatives.put(Transition.INITIALISE_MACHINE_NAME, singletonList(Transition.INITIALISE_MACHINE_NAME));
		alternatives.put(Transition.SETUP_CONSTANTS_NAME, singletonList(Transition.SETUP_CONSTANTS_NAME));

		List<Transition> resultRaw = AdvancedTraceConstructor.constructTraceEventB(transitionList, stateSpace, alternatives, emptyList(), emptyList());

		return PersistentTransition.createFromList(resultRaw);
	}



}
