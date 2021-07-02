package de.prob.check.tracereplay.check.refinement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Injector;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.FindPathCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.check.tracereplay.check.traceConstruction.AdvancedTraceConstructor;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.model.eventb.Event;
import de.prob.model.representation.*;
import de.prob.scripting.EventBFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import static java.util.stream.Collectors.*;

public class RefinementChecker {
	private final Injector injector;
	private final List<PersistentTransition> transitionList;
	private final Path alpha;
	private final Path beta;
	private List<PersistentTransition> createTrace;

	public RefinementChecker(Injector injector, List<PersistentTransition> transitionList, Path alpha, Path beta) {
		this.injector = injector;
		this.transitionList = transitionList;
		this.alpha = alpha;
		this.beta = beta;
	}




	/**
	 * Composition method with the task of extracting the nodes from a adding them to b, making b into a abstract machine if
	 * not, writing the new machine in a file, reparse the file to account for includes/sees/imports, as the recursive machine loader
	 * is inflexible. Then constructing a trace on the machine.
	 * @return the trace if it works on the machine
	 * @throws IOException something went wrong when parsing the file
	 * @throws BCompoundException something in the file was wrong with the machine
	 * @throws TraceConstructionError something went wrong when constructing the trace
	 */
	public List<PersistentTransition> check() throws IOException, BCompoundException, TraceConstructionError {
		switch (alpha.toString().substring(alpha.toString().lastIndexOf("."))){
			case ".mch":
			case ".ref":
			case ".imp":
				return checkClassicalB();
			case ".bum":
				return checkEventB();
			default:
				throw new IOException("file not suitable for refinement replay");
		}
	}


	public List<PersistentTransition> checkEventB() throws IOException, TraceConstructionError {
		ReusableAnimator animator = injector.getInstance(ReusableAnimator.class);
		StateSpace stateSpace = animator.createStateSpace();
		EventBFactory eventBFactory = injector.getInstance(EventBFactory.class);
		eventBFactory.extract(alpha.toString()).loadIntoStateSpace(stateSpace);

		AbstractModel model = stateSpace.getModel();

		Machine topLevelMachine = model.getChildrenOfType(Machine.class).get(model.getGraph().getStart());

		ModelElementList<Event> eventList = topLevelMachine.getChildrenOfType(Event.class);

		Map<String, String> newOldMapping = eventList.stream()
				.collect(toMap(BEvent::getName, entry -> traceEvent(entry).getName()));


		Map<String, List<String>> alternatives = newOldMapping.entrySet().stream()
				.collect(groupingBy(Map.Entry::getValue))
				.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
						.map(Map.Entry::getKey)
						.collect(toList())));

		alternatives.remove("INITIALISATION");
		alternatives.put(Transition.INITIALISE_MACHINE_NAME, Collections.singletonList(Transition.INITIALISE_MACHINE_NAME));
		alternatives.put(Transition.SETUP_CONSTANTS_NAME, Collections.singletonList(Transition.SETUP_CONSTANTS_NAME));

		List<String> blackList =newOldMapping.entrySet().stream()
				.filter(entry -> !entry.getValue().equals(entry.getKey()))
				.map(Map.Entry::getValue)
				.collect(toList());


		List<Transition> resultRaw = AdvancedTraceConstructor.constructTrace(transitionList, stateSpace, alternatives, blackList);

		return PersistentTransition.createFromList(resultRaw);
	}





	/**
	 * Traces an event back to its origins and returns the original event, return event if it was never refined
	 * @param event the event to trace
	 * @return the origin event
	 */
	public static Event traceEvent(Event event){
		if(event.getRefines().isEmpty()){
			return event;
		}else{
			return traceEvent(event.getRefines().get(0)); //In EventB there should only be one Event refined from
		}
	}

	public void reverseTrace() throws IOException {

		ReusableAnimator animator = injector.getInstance(ReusableAnimator.class);
		StateSpace stateSpace = animator.createStateSpace();
		EventBFactory eventBFactory = injector.getInstance(EventBFactory.class);
		eventBFactory.extract(alpha.toString()).loadIntoStateSpace(stateSpace);

		AbstractModel model = stateSpace.getModel();

		Machine topLevelMachine = model.getChildrenOfType(Machine.class).get(model.getGraph().getStart());

		ModelElementList<Event> eventList = topLevelMachine.getChildrenOfType(Event.class);

		Map<String, String> newOldMapping = eventList.stream()
				.collect(toMap(BEvent::getName, entry -> traceEvent(entry).getName()));

		//List<String> lastIntroducedVariables = topLevelMachine.getChildrenOfType()

		//FindPathCommand findPathCommand = new FindPathCommand()
		System.out.println();
	}


	public List<PersistentTransition> checkClassicalB() throws IOException, BCompoundException, TraceConstructionError {
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
