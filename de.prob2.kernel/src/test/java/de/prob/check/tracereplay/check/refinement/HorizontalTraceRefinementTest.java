package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HorizontalTraceRefinementTest {

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
	public void test_horizontal_refinement_simple_promotes() throws IOException, TraceConstructionError, BCompoundException {
		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "promotes", "M1.mch");
		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "promotes", "M2.mch");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b",  "promotes", "test.prob2trace"));

		HorizontalTraceRefiner traceRefiner = new HorizontalTraceRefiner(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace2);

		List<PersistentTransition> result = traceRefiner.refineTrace();

		Assertions.assertEquals(jsonFile.getTransitionList().size(), result.size());
	}



	@Test
	public void handlePromotedOperations_test() throws IOException, BCompoundException {

		Path alpha = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "promotes", "M3.mch");
		Path beta = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "promotes", "M4.mch");
		String alphaName = "M3";

		BParser parser = new BParser(beta.toString());
		Start betaFile = parser.parseFile(beta.toFile(), false);



		OperationsFinder operationsFinder = new OperationsFinder(alphaName, betaFile);
		operationsFinder.explore();


		StateSpace stateSpace2 = proBKernelStub.createStateSpace(alpha);
		Map<String, OperationsFinder.RenamingContainer> promotedOperations =
				HorizontalTraceRefiner.handlePromotedOperations(operationsFinder.getPromoted(), alphaName, new ArrayList<>(stateSpace2.getLoadedMachine().getOperations().keySet()), operationsFinder.getExtendedMachines(), operationsFinder.getIncludedImportedMachines());


		OperationsFinder.RenamingContainer expectedEntry = new OperationsFinder.RenamingContainer("", "Pow");
		Map<String, OperationsFinder.RenamingContainer> expected = new HashMap<>();

		expected.put("Pow", expectedEntry);

		Assertions.assertEquals(expected, promotedOperations);

	}

	@Test
	public void handleExtendedOperations_test() throws IOException, BCompoundException {

		Path alpha = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "extends", "M1.mch");
		Path beta = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "extends", "M2.mch");
		String alphaName = "M1";

		BParser parser = new BParser(beta.toString());
		Start betaFile = parser.parseFile(beta.toFile(), false);



		OperationsFinder operationsFinder = new OperationsFinder(alphaName, betaFile);
		operationsFinder.explore();


		StateSpace stateSpace2 = proBKernelStub.createStateSpace(alpha);
		Map<String, OperationsFinder.RenamingContainer> promotedOperations =
				HorizontalTraceRefiner.handlePromotedOperations(operationsFinder.getPromoted(), alphaName, new ArrayList<>(stateSpace2.getLoadedMachine().getOperations().keySet()), operationsFinder.getExtendedMachines(), operationsFinder.getIncludedImportedMachines());


		OperationsFinder.RenamingContainer expectedEntry = new OperationsFinder.RenamingContainer("", "Inc");
		OperationsFinder.RenamingContainer expectedEntry2 = new OperationsFinder.RenamingContainer("", "Dec");

		Map<String, OperationsFinder.RenamingContainer> expected = new HashMap<>();

		expected.put("Inc", expectedEntry);
		expected.put("Dec", expectedEntry2);

		Assertions.assertEquals(expected, promotedOperations);

	}

	@Test
	public void handleExtendsAndRenames_test() throws IOException, BCompoundException {

		Path alpha = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "extends", "M1.mch");
		Path beta = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "extends", "M3.mch");
		String alphaName = "M1";

		BParser parser = new BParser(beta.toString());
		Start betaFile = parser.parseFile(beta.toFile(), false);



		OperationsFinder operationsFinder = new OperationsFinder(alphaName, betaFile);
		operationsFinder.explore();


		StateSpace stateSpace2 = proBKernelStub.createStateSpace(alpha);
		Map<String, OperationsFinder.RenamingContainer> promotedOperations =
				HorizontalTraceRefiner.handlePromotedOperations(operationsFinder.getPromoted(), alphaName, new ArrayList<>(stateSpace2.getLoadedMachine().getOperations().keySet()), operationsFinder.getExtendedMachines(), operationsFinder.getIncludedImportedMachines());


		OperationsFinder.RenamingContainer expectedEntry = new OperationsFinder.RenamingContainer("r1", "Inc");
		OperationsFinder.RenamingContainer expectedEntry2 = new OperationsFinder.RenamingContainer("r1", "Dec");

		Map<String, OperationsFinder.RenamingContainer> expected = new HashMap<>();

		expected.put("Inc", expectedEntry);
		expected.put("Dec", expectedEntry2);

		Assertions.assertEquals(expected, promotedOperations);

	}


	@Test
	public void test_horizontal_refinement_traffic_light_example() throws IOException, TraceConstructionError, BCompoundException {
		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "trafficLight", "TrafficLight2.mch");
		Path pathStateSpace2 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "trafficLight", "TrafficLightTime_Ref.mch");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b",  "trafficLight", "TrafficLight2Minimal.prob2trace"));

		HorizontalTraceRefiner traceRefiner = new HorizontalTraceRefiner(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1, pathStateSpace2);

		List<PersistentTransition> result = traceRefiner.refineTrace();

		Assertions.assertEquals(jsonFile.getTransitionList().size()+1, result.size()); //Will add setup constants
	}

}

