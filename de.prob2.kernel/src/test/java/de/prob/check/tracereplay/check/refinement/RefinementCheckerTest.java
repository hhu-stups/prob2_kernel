package de.prob.check.tracereplay.check.refinement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.inject.Injector;
import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.TestUtils;
import de.prob.check.tracereplay.check.TraceChecker;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.check.tracereplay.check.renamig.DeltaCalculationException;
import de.prob.check.tracereplay.check.traceConstruction.AdvancedTraceConstructor;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.model.eventb.Event;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.Machine;
import de.prob.model.representation.ModelElementList;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RefinementCheckerTest {


	private static TraceManager traceManager;
	private static ProBKernelStub proBKernelStub;

	@BeforeAll
	static void beforeAll() {

		traceManager = CliTestCommon.getInjector().getInstance(TraceManager.class);
		proBKernelStub = CliTestCommon.getInjector().getInstance(ProBKernelStub.class);
	}

	@AfterAll
	static void afterAll() {
		proBKernelStub.killCurrentAnimator();
	}



	@Test
	public void test_integration_1_simple_refinement() throws IOException, BCompoundException, TraceConstructionError {


		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLightRef.ref");
		BParser bParser = new BParser(file.toString());

		Start start = bParser.parseFile(file.toFile(), false);

		NodeCollector nodeCollector = new NodeCollector(start);

		Path file2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLight.mch");
		BParser bParser2 = new BParser(file2.toString());

		Start start2 = bParser2.parseFile(file2.toFile(), false);

		ASTManipulator astManipulator = new ASTManipulator(start2, nodeCollector);

		AAbstractMachineParseUnit aAbstractMachineParseUnit = (AAbstractMachineParseUnit) astManipulator.getStart().getPParseUnit();

		PrettyPrinter prettyPrinter = new PrettyPrinter();
		prettyPrinter.caseAAbstractMachineParseUnit(aAbstractMachineParseUnit);

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "TrafficLightRef.prob2trace"));

		File tempFile = File.createTempFile("machine", ".mch");

		FileWriter writer = new FileWriter(tempFile);
		writer.write(prettyPrinter.getPrettyPrint());
		writer.close();

		StateSpace stateSpace = proBKernelStub.createStateSpace(tempFile.toPath());

		List<Transition> resultRaw;

		resultRaw = AdvancedTraceConstructor.constructTraceByName(jsonFile.getTransitionList(), stateSpace);

		List<String> result = PersistentTransition.createFromList(resultRaw).stream().map(PersistentTransition::getOperationName).collect(Collectors.toList());

		List<String> expected = jsonFile.getTransitionList().stream().map(PersistentTransition::getOperationName).collect(Collectors.toList());
		Assertions.assertEquals(expected, result);
	}



	@Test
	public void simple_event_b_no_changes() throws IOException, DeltaCalculationException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v2_files", "PitmanController.bum");
		StateSpace stateSpace1 = proBKernelStub.createEventB(pathStateSpace1);

		LoadedMachine loadedMachine = stateSpace1.getLoadedMachine();
		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "PitmanController.prob2trace"));


		AbstractModel foo = stateSpace1.getModel();

		ModelElementList<Machine> gna = stateSpace1.getModel().getChildrenOfType(Machine.class);

		ModelElementList<? extends AbstractElement> bla = gna.get(0).getChildren().get(Event.class);
		ModelElementList<? extends AbstractElement> bla2 = gna.get(1).getChildren().get(Event.class);


		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		StateSpace stateSpace2 = proBKernelStub.createEventB(pathStateSpace1);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();


		new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();
	}





}
