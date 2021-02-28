package de.prob.check;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.PersistenceDelta;
import de.prob.check.tracereplay.check.RenamingDelta;
import de.prob.check.tracereplay.check.ReplayOptions;
import de.prob.check.tracereplay.check.TraceAnalyser;
import de.prob.check.tracereplay.check.TraceChecker;
import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.check.tracereplay.check.TraceModifier;
import de.prob.check.tracereplay.check.exceptions.DeltaCalculationException;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.util.Collections.*;

public class TraceExplorerTypeIVIntegration {
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
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						singleton("on"));

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);


		PersistenceDelta persistenceDelta = new ArrayList<>(resultCleaned.values()).get(0).get(2);
		Assertions.assertEquals("on", persistenceDelta.getOldTransition().getOperationName());
		Assertions.assertEquals(2, persistenceDelta.getNewTransitions().size());
	}

	@Test
	public void integration_test_always_new_intermediate_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						singleton("on"));

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);


		List<PersistenceDelta> compareResult = new ArrayList<>(resultCleaned.values()).get(0)
				.stream()
				.filter(entry -> entry.getOldTransition().getOperationName().equals("on"))
				.filter(entry -> entry.getNewTransitions().size() == 2)
				.collect(Collectors.toList());

		Assertions.assertEquals(4, compareResult.size());
	}


	@Test
	public void integration_test_complete_renamed_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "complete_renamed_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "complete_renamed_operation", "ISLAND.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						singleton("on"));

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultPre = TraceExplorer.removeHelperVariableMappings(result);


		List<PersistenceDelta> resultCleaned = resultPre.values().stream().findFirst().get().stream().filter(entry -> {
			String name = entry.getOldTransition().getOperationName();
			if(name.equals("on")){
				PersistentTransition persistentTransition = entry.getNewTransitions().get(0);
				return persistentTransition.getOperationName().equals("drive_on") &&
						entry.getOldTransition().getDestinationStateVariables().keySet().containsAll(persistentTransition.getDestinationStateVariables().keySet()) &&
						entry.getOldTransition().getDestinationStateVariables().values().containsAll(persistentTransition.getDestinationStateVariables().values());
			}else{
				return true;
			}
		}).collect(Collectors.toList());

		Assertions.assertEquals(10, resultCleaned.size());

	}


	@Test
	public void integration_test_split_operation() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "tropical_island", "version_2", "Island2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "tropical_island", "version_2", "ISLAND.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> replayedTrace =
				new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						singleton("arrive_by_boat"),
						singleton("leave"));



		Map<String, List<String>> expected1 = singletonMap("leave", singletonList("leave_with_car"));
		Map<String, TraceAnalyser.AnalyserResult> expected2 = singletonMap("leave", TraceAnalyser.AnalyserResult.MixedNames);

		Map<String, TraceAnalyser.AnalyserResult> resultCleaned = TraceAnalyser.analyze(singleton("leave"), new ArrayList<>(replayedTrace.values()).get(0), jsonFile.getTransitionList());

		Map<String, List<String>> result = TraceAnalyser.calculateIntermediate(singleton("leave"), new ArrayList<>(replayedTrace.values()).get(0));


		Assertions.assertEquals(expected2, resultCleaned);
		Assertions.assertEquals(expected1, result);

	}


	@Test
	public void integration_test_operation_always_intermediate() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> evaluated = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						emptySet());

		Map<String, List<String>> expected1 = singletonMap("on", Arrays.asList("openBark", "on"));
		Map<String, TraceAnalyser.AnalyserResult> expected2 = singletonMap("on", TraceAnalyser.AnalyserResult.Intermediate);



		Map<String, TraceAnalyser.AnalyserResult> resultCleaned = TraceAnalyser.analyze(singleton("on"), new ArrayList<>(evaluated.values()).get(0), jsonFile.getTransitionList());
		Map<String, List<String>> result = TraceAnalyser.calculateIntermediate(singleton("on"), new ArrayList<>(evaluated.values()).get(0));

		Assertions.assertEquals(expected2, resultCleaned);
		Assertions.assertEquals(expected1, result);

	}


	@Test
	public void integration_test_with_completely_renamed_operation() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "complete_renamed_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "complete_renamed_operation", "ISLAND.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> evaluated = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						emptySet());

		Map<String, List<String>> expected1 = singletonMap("on", singletonList("drive_on"));
		Map<String, TraceAnalyser.AnalyserResult> expected2 = singletonMap("on", TraceAnalyser.AnalyserResult.Straight);



		Map<String, TraceAnalyser.AnalyserResult> resultCleaned = TraceAnalyser.analyze(singleton("on"), new ArrayList<>(evaluated.values()).get(0), jsonFile.getTransitionList());
		Map<String, List<String>> result = TraceAnalyser.calculateIntermediate(singleton("on"), new ArrayList<>(evaluated.values()).get(0));

		Assertions.assertEquals(expected2, resultCleaned);
		Assertions.assertEquals(expected1, result);

	}


	@Test
	public void integration_test_with_one_time_intermediate_operation() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> evaluated = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						emptySet());

		Map<String, List<String>> expected1 = singletonMap("on", Arrays.asList("openBark", "on"));
		Map<String, TraceAnalyser.AnalyserResult> expected2 = singletonMap("on", TraceAnalyser.AnalyserResult.Mixed);



		Map<String, TraceAnalyser.AnalyserResult> resultCleaned = TraceAnalyser.analyze(singleton("on"), new ArrayList<>(evaluated.values()).get(0), jsonFile.getTransitionList());
		Map<String, List<String>> result = TraceAnalyser.calculateIntermediate(singleton("on"), new ArrayList<>(evaluated.values()).get(0));

		Assertions.assertEquals(expected2, resultCleaned);
		Assertions.assertEquals(expected1, result);

	}


	@Test
	public void large_type_IV_test() throws IOException, ModelTranslationError, DeltaCalculationException {

		Path oldPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "complexExample", "PitmanController_v6.mch");
		StateSpace stateSpace1 = proBKernelStub.createStateSpace(oldPath);
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "complexExample", "PitmanController_v6_v6.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "complexExample", "large5000Steps.prob2trace"));

		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());

		TraceChecker traceChecker = new TraceChecker(jsonFile.getTransitionList(), jsonFile.getMachineOperationInfos(), newInfos, new HashSet<>(oldVars), new HashSet<>(stateSpace2.getLoadedMachine().getVariableNames()), oldPath.toString(), newPath.toString(), injector, new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());

		TraceModifier bla = traceChecker.getTraceModifier();

		Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, Map<String, TraceAnalyser.AnalyserResult>>> phase4 = bla.getChangelogPhase4();

		TraceAnalyser.AnalyserResult result = phase4.get(emptySet()).get(emptyMap()).get("ENV_Pitman_DirectionBlinking");

		Assertions.assertEquals(TraceAnalyser.AnalyserResult.Mixed, result);
	}


}
