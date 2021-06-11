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
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.traceConstruction.AdvancedTraceConstructor;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.model.eventb.Event;
import de.prob.model.representation.*;
import de.prob.scripting.EventBFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import static java.util.stream.Collectors.toMap;

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


	public List<PersistentTransition> checkEventB() throws IOException {
		ReusableAnimator animator = injector.getInstance(ReusableAnimator.class);
		StateSpace stateSpace = animator.createStateSpace();
		EventBFactory eventBFactory = injector.getInstance(EventBFactory.class);
		eventBFactory.extract(alpha.toString()).loadIntoStateSpace(stateSpace);

		Set<String> usedOperations = TraceCheckerUtils.usedOperations(TraceCheckerUtils.stripNonOpClause(transitionList));

		AbstractModel model = stateSpace.getModel();
		List<DependencyGraph.Node> chain = model.getGraph().refinementChain();
		ModelElementList<Machine> involvedMachines = stateSpace.getModel().getChildrenOfType(Machine.class);

		Map<String, Set<String>> operationSets = involvedMachines
				.stream()
				.collect(toMap(Machine::getName, entry -> entry.getChildren().get(BEvent.class).stream().map(Object::toString)
				.collect(Collectors.toSet())));

		Map<String, Machine> machineMap = involvedMachines.stream().collect(toMap(Machine::getName, entry -> entry));

		String bestMatch = operationSets.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> {
			Set<String> current = new HashSet<String>(entry.getValue());
			current.removeAll(usedOperations);
			return current.size();
		})).entrySet().stream().max(new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				if (o1.getValue() >= o2.getValue()) {
					return 1;
				} else {
					return -1;
				}
			}
		}).get().getKey();

		Machine blabla = model.getChildrenOfType(Machine.class).get(model.getGraph().getStart());
		ModelElementList<Event> initialEventList = blabla.getChildrenOfType(Event.class);



		return null;
	}

	public Map<String, String> pairOldAndNew(ModelElementList<Event> eventList, Map<String, String> aux){
		if(aux.isEmpty()){
			eventList.forEach(element -> aux.put(element.getName(), element.getName()));
		}else{

		}
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

		File bla = alpha.getParent().toFile();
		File tempFile = File.createTempFile("machine", ".mch", alpha.getParent().toFile());


		FileWriter writer = new FileWriter(tempFile);
		writer.write(prettyPrinter.getPrettyPrint());
		writer.close();


		StateSpace stateSpace = TraceCheckerUtils.createStateSpace(tempFile.toPath().toString(), injector);


		List<Transition> resultRaw = AdvancedTraceConstructor.constructTraceByName(transitionList, stateSpace);

		return PersistentTransition.createFromList(resultRaw);
	}




}
