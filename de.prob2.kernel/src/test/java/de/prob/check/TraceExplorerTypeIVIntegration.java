package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.PersistenceDelta;
import de.prob.check.tracereplay.check.TraceAnalyser;
import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.*;

public class TraceExplorerTypeIVIntegration {

	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null && proBKernelStub==null) {
			System.setProperty("prob.home", "/home/sebastian/prob_prolog");
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}

	}



	@Test
	public void integration_test_new_intermediate_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "island.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
				.replayTrace(
						jsonFile.getTrace().getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						singleton("on"));

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);


		PersistenceDelta persistenceDelta = new ArrayList<>(resultCleaned.values()).get(0).get(3);
		Assert.assertEquals("on", persistenceDelta.getOldTransition().getOperationName());
		Assert.assertEquals(1, persistenceDelta.getNewTransitions().size());
	}

	@Test
	public void integration_test_always_new_intermediate_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
				.replayTrace(
						jsonFile.getTrace().getTransitionList(),
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

		Assert.assertEquals(4, compareResult.size());
	}


	@Test
	public void integration_test_complete_renamed_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "complete_renamed_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "complete_renamed_operation", "island.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
				.replayTrace(
						jsonFile.getTrace().getTransitionList(),
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

		Assert.assertEquals(12, resultCleaned.size());

	}


	@Test
	public void integration_test_splatted_operation() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "tropical_island", "version_2", "Island2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "tropical_island", "version_2", "island_2.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> evaluated = new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
				.replayTrace(
						jsonFile.getTrace().getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						singleton("leave"));



		Map<String, List<String>> expected1 = singletonMap("leave", singletonList("leave_with_car"));
		Map<String, TraceAnalyser.AnalyserResult> expected2 = singletonMap("leave", TraceAnalyser.AnalyserResult.MixedNames);



		Map<String, TraceAnalyser.AnalyserResult> resultCleaned = TraceAnalyser.analyze(singleton("leave"), new ArrayList<>(evaluated.values()).get(0), jsonFile.getTrace().getTransitionList());

		Map<String, List<String>> result = TraceAnalyser.calculateIntermediate(singleton("leave"), new ArrayList<>(evaluated.values()).get(0));


		Assert.assertEquals(expected2, resultCleaned);
		Assert.assertEquals(expected1, result);

	}


	@Test
	public void integration_test_operation_always_intermediate() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> evaluated = new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
				.replayTrace(
						jsonFile.getTrace().getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						emptySet());

		Map<String, List<String>> expected1 = singletonMap("on", Arrays.asList("openBark", "on"));
		Map<String, TraceAnalyser.AnalyserResult> expected2 = singletonMap("on", TraceAnalyser.AnalyserResult.Intermediate);



		Map<String, TraceAnalyser.AnalyserResult> resultCleaned = TraceAnalyser.analyze(singleton("on"), new ArrayList<>(evaluated.values()).get(0), jsonFile.getTrace().getTransitionList());
		Map<String, List<String>> result = TraceAnalyser.calculateIntermediate(singleton("on"), new ArrayList<>(evaluated.values()).get(0));

		Assert.assertEquals(expected2, resultCleaned);
		Assert.assertEquals(expected1, result);

	}


	@Test
	public void integration_test_with_completely_renamed_operation() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV","complete_renamed_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "complete_renamed_operation", "island.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> evaluated = new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
				.replayTrace(
						jsonFile.getTrace().getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						emptySet());

		Map<String, List<String>> expected1 = singletonMap("on", singletonList("drive_on"));
		Map<String, TraceAnalyser.AnalyserResult> expected2 = singletonMap("on", TraceAnalyser.AnalyserResult.Straight);



		Map<String, TraceAnalyser.AnalyserResult> resultCleaned = TraceAnalyser.analyze(singleton("on"), new ArrayList<>(evaluated.values()).get(0), jsonFile.getTrace().getTransitionList());
		Map<String, List<String>> result = TraceAnalyser.calculateIntermediate(singleton("on"), new ArrayList<>(evaluated.values()).get(0));

		Assert.assertEquals(expected2, resultCleaned);
		Assert.assertEquals(expected1, result);

	}


	@Test
	public void integration_test_with_one_time_intermediate_operation() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "island.prob2trace"));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> evaluated = new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
				.replayTrace(
						jsonFile.getTrace().getTransitionList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						jsonFile.getMachineOperationInfos(),
						emptySet(),
						emptySet());

		Map<String, List<String>> expected1 = singletonMap("on", Arrays.asList("openBark", "on"));
		Map<String, TraceAnalyser.AnalyserResult> expected2 = singletonMap("on", TraceAnalyser.AnalyserResult.Mixed);



		Map<String, TraceAnalyser.AnalyserResult> resultCleaned = TraceAnalyser.analyze(singleton("on"), new ArrayList<>(evaluated.values()).get(0), jsonFile.getTrace().getTransitionList());
		Map<String, List<String>> result = TraceAnalyser.calculateIntermediate(singleton("on"), new ArrayList<>(evaluated.values()).get(0));

		Assert.assertEquals(expected2, resultCleaned);
		Assert.assertEquals(expected1, result);

	}



}
