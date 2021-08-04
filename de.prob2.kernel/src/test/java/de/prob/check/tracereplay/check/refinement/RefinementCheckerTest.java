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
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.check.tracereplay.PersistentTrace;
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
		System.setProperty("prob.home", "/home/sebastian/prob_prolog");

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
	public void simple_event_b_no_changes() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "trafficLight", "mac.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB",  "trafficLight", "trafficLight.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		Assertions.assertEquals(jsonFile.getTransitionList().size(), result.size());
	}


	@Test
	public void simple_event_b_one_refinement_step_should_fail() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "PitmanController.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "BlinkLamps30RandomSteps.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		Assertions.assertEquals(jsonFile.getTransitionList().size(), result.size());
	}


	@Test
	public void simple_event_b_one_refinement_step_careful_choice_should_fail() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "PitmanController.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "BlinkLampsTest4.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		Assertions.assertEquals(jsonFile.getTransitionList().size(), result.size());
	}


	@Test
	public void blinkLamps_to_PitmanController() throws IOException, TraceConstructionError, BCompoundException {
		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "PitmanController.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "BlinkLamps4.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		//jsonFile.getTransitionList().forEach(entry -> System.out.println(entry.getOperationName()));
		//result.forEach(entry -> System.out.println(entry.getOperationName()));

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "TipBlinkingTestForPitmanController.prob2trace"), jsonFile.updateMetaData().changeTransitionList(result));
		Assertions.assertEquals(31, result.size());
	}


	@Test
	public void PitmanController_to_PitmanControllerTime() throws IOException, TraceConstructionError, BCompoundException {

		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "PitmanController2_TIME.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "TipBlinkingTestForPitmanController.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		//jsonFile.getTransitionList().forEach(entry -> System.out.println(entry.getOperationName()));
		//result.forEach(entry -> System.out.println(entry.getOperationName()));

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "TipBlinkingTestForPitmanControllerTime.prob2trace"), jsonFile.changeTransitionList(result));
		Assertions.assertEquals(42, result.size());
	}



	@Test
	public void reverse_trace_PitmanControllerTime_PitmanController() throws IOException, TraceConstructionError {

		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "PitmanController2_TIME.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "PitmanController.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "PitmanController2_TIME_100Steps.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace2).reverseTrace();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "PitmanController_created_from_PitmanController2_TIME.prob2trace"), jsonFile.changeTransitionList(result));

	}

	@Test
	public void reverse_trace_PitmanController_BlinkLamps() throws IOException, TraceConstructionError {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "PitmanController.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "BlinkLamps.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "PitmanController_created_from_PitmanController2_TIME.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace2).reverseTrace();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "BlinkLamps_created_from_PitmanController.prob2trace"), jsonFile.changeTransitionList(result));

	}

	@Test
	public void refine_trace_BlinkLamps_to_PitmanController() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "PitmanController.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "BlinkLamps_created_from_PitmanController.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "PitmanController_created_from_BlinkLamps.prob2trace"), jsonFile.changeTransitionList(result));


		//jsonFile.getTransitionList().forEach(entry -> System.out.println(entry.getOperationName()));
		//result.forEach(entry -> System.out.println(entry.getOperationName()));

		System.out.println("Input Size " + jsonFile.getTransitionList().size());
		System.out.println("Output Size "  + result.size());

	}

	@Test
	public void refine_trace_PitmanController_to_PitmanController_Time() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "pitman_v4_files", "PitmanController2_TIME.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "PitmanController_created_from_BlinkLamps.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "PitmanController_Time_created_from_PitmanController.prob2trace"), jsonFile.changeTransitionList(result));


		jsonFile.getTransitionList().forEach(entry -> System.out.println(entry.getOperationName()));

		result.forEach(entry -> System.out.println(entry.getOperationName()));

		System.out.println("Input Size " + jsonFile.getTransitionList().size());
		System.out.println("Output Size "  + result.size());

		//	Assertions.assertEquals(jsonFile.getTransitionList().size(), result.size());
	}



	@Test
	public void trafficLightAbstract_trafficLightTimed() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "trafficLight", "TrafficLight2.mch");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "trafficLight", "TrafficLightTime_Ref.mch");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "trafficLight","trafficLightAbstract.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace2).check();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b","trafficLight", "trafficLightTime.prob2trace"), jsonFile.changeTransitionList(result));


		jsonFile.getTransitionList().forEach(entry -> System.out.println(entry.getOperationName()));

		result.forEach(entry -> System.out.println(entry.getOperationName()));

		System.out.println("Input Size " + jsonFile.getTransitionList().size());
		System.out.println("Output Size "  + result.size());

	}


	@Test
	public void refine_trace_els_1112_M0_to_M1() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M1.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M0RefinementTest.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M1RefinementTest.prob2trace"), jsonFile.changeTransitionList(result));
		Assertions.assertEquals(jsonFile.getTransitionList().size(), result.size());
	}

	@Test
	public void refine_trace_els_1112_M1_to_M2() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M2.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M1RandomMovements.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M2RandomMovements		.prob2trace"), jsonFile.changeTransitionList(result));
		Assertions.assertEquals(jsonFile.getTransitionList().size(), result.size());
	}

	@Test
	public void reverse_trace_M5_M4() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M5.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M4.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M5100Steps.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace2).reverseTrace();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M4_created_from_M5.prob2trace"), jsonFile.changeTransitionList(result));

		List<PersistentTransition> result2 = new RefinementChecker(CliTestCommon.getInjector(), result, pathStateSpace1, pathStateSpace1).check();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M5_created_from_M4_created_from_M5.prob2trace"), jsonFile.changeTransitionList(result));


	}

	@Test
	public void reverse_trace_M4_M3() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M4.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M3.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M4_created_from_M5.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace2).reverseTrace();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M3_created_from_M4.prob2trace"), jsonFile.changeTransitionList(result));

		List<PersistentTransition> result2 = new RefinementChecker(CliTestCommon.getInjector(), result, pathStateSpace1, pathStateSpace1).check();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M4_created_from_M3_created_from_M4.prob2trace"), jsonFile.changeTransitionList(result));


	}

	@Test
	public void reverse_trace_M3_M2() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M3.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M2.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M3_created_from_M4.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace2).reverseTrace();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M2_created_from_M3.prob2trace"), jsonFile.changeTransitionList(result));

		List<PersistentTransition> result2 = new RefinementChecker(CliTestCommon.getInjector(), result, pathStateSpace1, pathStateSpace1).check();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M3_created_from_M2_created_from_M3.prob2trace"), jsonFile.changeTransitionList(result));


	}

	@Test
	public void reverse_trace_M2_M1() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M2.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M1.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M2_created_from_M3.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace2).reverseTrace();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M1_created_from_M2.prob2trace"), jsonFile.changeTransitionList(result));

		List<PersistentTransition> result2 = new RefinementChecker(CliTestCommon.getInjector(), result, pathStateSpace1, pathStateSpace1).check();

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M2_created_from_M1_created_from_M2.prob2trace"), jsonFile.changeTransitionList(result));


	}


	@Test
	public void paper_test_M1_M2() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M2.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M1.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "M2_created_from_M1_created_from_M2.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		System.out.println("result length " + result.size());

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "paper_test_M1_M2.prob2trace"), jsonFile.changeTransitionList(result));

		System.out.println("Input Size " + jsonFile.getTransitionList().size());
		System.out.println("Output Size "  + result.size());



	}

	@Test
	public void paper_test_M2_M3() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M3.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M2.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "paper_test_M1_M2.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		System.out.println("Input Size " + jsonFile.getTransitionList().size());
		System.out.println("Output Size "  + result.size());

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "paper_test_M2_M3.prob2trace"), jsonFile.changeTransitionList(result));

	}


	@Test
	public void paper_test_M3_M4() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M4.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M3.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "paper_test_M2_M3.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();


		System.out.println("Input Size " + jsonFile.getTransitionList().size());
		System.out.println("Output Size "  + result.size());

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "paper_test_M3_M4.prob2trace"), jsonFile.changeTransitionList(result));

	}


	@Test
	public void paper_test_M4_M5() throws IOException, TraceConstructionError, BCompoundException {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M5.bum");

		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "els_1112", "M4.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "paper_test_M3_M4.prob2trace"));

		List<PersistentTransition> result = new RefinementChecker(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace1).check();

		System.out.println("Input Size " + jsonFile.getTransitionList().size());
		System.out.println("Output Size "  + result.size());

		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "paper_test_M4_M5.prob2trace"), jsonFile.changeTransitionList(result));

	}

	@Test
	public void parse_classical_B() throws IOException, TraceConstructionError, BCompoundException {


		String bla = "val=Upward5 & hl={(lowBeamLeft|->0),(lowBeamRight|->0),(tailLampLeft|->0),(tailLampRight|->0),(corneringLightLeft|->0),(corneringLightRight|->0)}" +
				//" & left=FALSE " +
				"& pitmanArmUD=Upward5 & nextflashDesacLeft=0 & nextflashDesacRight=0 & right=FALSE & nbc=0";

		EventB eventB = new EventB(bla, FormulaExpand.EXPAND);



	}

}
