package de.prob.check.tracereplay.check;

import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.exploration.PersistenceDelta;
import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class TraceAnalyserTest {
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
	public void integration_test_new_intermediate_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test","resources", "de" , "prob", "testmachines", "traces", "typeIV" , "one_time_intermediate_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test","resources", "de" , "prob", "testmachines", "traces", "typeIV" , "one_time_intermediate_operation", "ISLAND.prob2trace"));

		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				IdentifierMatcher.generateAllPossibleMappingVariations(jsonFile.getTransitionList(), stateSpace.getLoadedMachine().getOperations(), jsonFile.getMachineOperationInfos(), emptySet(), new TestUtils.StubFactoryImplementation());



		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						singleton("on"),
						selectedMappingsToResultsKeys);

		Map<String, TraceAnalyser.AnalyserResult> bla = TraceAnalyser.analyze(singleton("on"), result.values().stream().findFirst().get(), jsonFile.getTransitionList());

		Assertions.assertEquals(1, bla.entrySet().size());
		Assertions.assertEquals(TraceAnalyser.AnalyserResult.Mixed, bla.get("on"));
	}


	@Test
	public void integration_test_always_new_intermediate_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test","resources", "de" , "prob", "testmachines", "traces", "typeIV" , "always_intermediate", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test","resources", "de" , "prob", "testmachines", "traces", "typeIV" , "always_intermediate", "ISLAND.prob2trace"));


		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				IdentifierMatcher.generateAllPossibleMappingVariations(jsonFile.getTransitionList(), stateSpace.getLoadedMachine().getOperations(), jsonFile.getMachineOperationInfos(), emptySet(), new TestUtils.StubFactoryImplementation());



		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						singleton("on"),
						selectedMappingsToResultsKeys);

		Map<String, TraceAnalyser.AnalyserResult> bla = TraceAnalyser.analyze(singleton("on"), result.values().stream().findFirst().get(), jsonFile.getTransitionList());

		Assertions.assertEquals(1, bla.entrySet().size());
		Assertions.assertEquals(TraceAnalyser.AnalyserResult.Intermediate, bla.get("on"));
	}


	@Test
	public void integration_test_complete_renamed_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test","resources", "de" , "prob", "testmachines", "traces", "typeIV" , "complete_renamed_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test","resources", "de" , "prob", "testmachines", "traces", "typeIV" , "complete_renamed_operation", "ISLAND.prob2trace"));

		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				IdentifierMatcher.generateAllPossibleMappingVariations(jsonFile.getTransitionList(), stateSpace.getLoadedMachine().getOperations(), jsonFile.getMachineOperationInfos(), emptySet(), new TestUtils.StubFactoryImplementation());



		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						singleton("on"),
						selectedMappingsToResultsKeys);

		Map<String, TraceAnalyser.AnalyserResult> bla = TraceAnalyser.analyze(singleton("on"), result.values().stream().findFirst().get(), jsonFile.getTransitionList());

		Assertions.assertEquals(1, bla.entrySet().size());
		Assertions.assertEquals(TraceAnalyser.AnalyserResult.Straight, bla.get("on"));
	}


}
