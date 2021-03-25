package de.prob.animator.command;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.ProBKernelStub;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.check.tracereplay.check.refinement.ASTManipulator;
import de.prob.check.tracereplay.check.refinement.NodeCollector;
import de.prob.check.tracereplay.check.traceConstruction.AdvancedTraceConstructor;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FindPathCommandTest {

	private static TraceManager traceManager;
	private static ProBKernelStub proBKernelStub;

	@BeforeAll
	static void beforeAll() {

		traceManager = CliTestCommon.getInjector().getInstance(TraceManager.class);
		proBKernelStub = CliTestCommon.getInjector().getInstance(ProBKernelStub.class);
	}

	/**
	 * Test
	 * Best match even if fails
	 * Can construct simple traces without choice points
	 * Does find right path in list (simple)
	 */

	@AfterAll
	static void afterAll() {
		proBKernelStub.killCurrentAnimator();
	}



	@Test
	public void test_find_path_1_path_exists() throws IOException, ModelTranslationError {


		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLight.mch");

		StateSpace stateSpace = proBKernelStub.createStateSpace(file);

		List<PersistentTransition> transitionList = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLight.prob2trace")).getTransitionList();

		List<String> names = transitionList.stream().map(PersistentTransition::getOperationName).collect(Collectors.toList());
		List<ClassicalB> predicates =transitionList.stream().map(entry -> new ClassicalB(ReplayOptions.allowAll().createMapping(entry).toString(), FormulaExpand.EXPAND)).collect(Collectors.toList());


		FindPathCommand findPathCommand = new FindPathCommand(stateSpace,new Trace(stateSpace).getCurrentState(), names, predicates );

		stateSpace.execute(findPathCommand);

		List<String> result = findPathCommand.getNewTransitions().stream().map(Transition::getName).collect(Collectors.toList());

		Assertions.assertEquals(names, result);
	}


	@Test
	public void test_find_path_2_fails_but_will_return_longest_match() throws IOException, ModelTranslationError {


		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLight.mch");

		StateSpace stateSpace = proBKernelStub.createStateSpace(file);


		List<String> names = Arrays.asList(Transition.INITIALISE_MACHINE_NAME, "set_cars", "set_peds_go");
		List<ClassicalB> predicates = Arrays.asList(new ClassicalB("1=1", FormulaExpand.EXPAND), new ClassicalB("cars_go=TRUE", FormulaExpand.EXPAND), new ClassicalB("1=1", FormulaExpand.EXPAND));

		List<String>expected = new ArrayList<>(names);

		expected.remove("set_peds_go");

		FindPathCommand findPathCommand = new FindPathCommand(stateSpace,new Trace(stateSpace).getCurrentState(), names, predicates );

		stateSpace.execute(findPathCommand);

		List<String> result = findPathCommand.getNewTransitions().stream().map(Transition::getName).collect(Collectors.toList());

		Assertions.assertEquals(expected, result);
	}



	 //Simple Trace Replay is not enough here, you need to select the right operations in order to archive the goal of replaying the full trace
	@Test
	public void test_integration_1_simple_refinement_can_make_right_choices() throws IOException, BCompoundException, ModelTranslationError {


		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLightRef.ref");
		BParser bParser = new BParser(file.toString());

		Start start = bParser.parseFile(file.toFile(), false);

		NodeCollector nodeCollector = new NodeCollector(start);

		Path file2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLight.mch");
		BParser bParser2 = new BParser(file2.toString());

		Start start2 = bParser2.parseFile(file2.toFile(), false);

		ASTManipulator astManipulator = new ASTManipulator(start2, nodeCollector);

		PrettyPrinter prettyPrinter = new PrettyPrinter();


		AAbstractMachineParseUnit aAbstractMachineParseUnit = (AAbstractMachineParseUnit) astManipulator.getStart().getPParseUnit();
		prettyPrinter.caseAAbstractMachineParseUnit(aAbstractMachineParseUnit);


		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "TrafficLightRef.prob2trace"));

		File tempFile = File.createTempFile("machine", ".mch");

		FileWriter writer = new FileWriter(tempFile);
		writer.write(prettyPrinter.getPrettyPrint());
		writer.close();

		StateSpace stateSpace = proBKernelStub.createStateSpace(tempFile.toPath());

		List<String> names = jsonFile.getTransitionList().stream().map(PersistentTransition::getOperationName).collect(Collectors.toList());
		List<ClassicalB> predicates =jsonFile.getTransitionList().stream().map(entry -> new ClassicalB(ReplayOptions.allowAll().createMapping(entry).toString(), FormulaExpand.EXPAND)).collect(Collectors.toList());

		FindPathCommand findPathCommand = new FindPathCommand(stateSpace,new Trace(stateSpace).getCurrentState(), names, predicates );

		stateSpace.execute(findPathCommand);

		List<String> result = findPathCommand.getNewTransitions().stream().map(Transition::getName).collect(Collectors.toList());

		Assertions.assertEquals(names, result);

	}

}
