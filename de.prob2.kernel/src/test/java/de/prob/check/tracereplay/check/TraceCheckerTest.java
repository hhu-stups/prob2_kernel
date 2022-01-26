package de.prob.check.tracereplay.check;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.google.inject.Injector;
import de.prob.ProBKernelStub;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.check.exploration.PersistenceDelta;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.check.tracereplay.check.renamig.DeltaCalculationException;
import de.prob.check.tracereplay.check.renamig.DynamicRenamingAnalyzer;
import de.prob.check.tracereplay.check.renamig.PrologTermNotDefinedException;
import de.prob.check.tracereplay.check.renamig.RenamingDelta;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class TraceCheckerTest {
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
	public void integration_short_constructor() throws IOException, DeltaCalculationException {


		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto.mch"));
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto2.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));


		
		TraceChecker traceChecker = new TraceChecker(jsonFile.getTransitionList(),
				oldInfos,
				newInfos,
				new HashSet<>(oldVars),
				new HashSet<>(newVars), 
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());


		TraceModifier modifier = traceChecker.getTraceModifier();

		Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> result = modifier.getChangelogPhase3II();

		Assertions.assertEquals(1, modifier.getChangelogPhase2().size());
		Assertions.assertEquals(6, modifier.tracesStoredInTypeIII());

	}


	@Test
	public void integration_short_constructor_2() throws IOException, DeltaCalculationException {
		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "tropical_island", "version_2", "Island2.mch");

		StateSpace stateSpace = proBKernelStub.createStateSpace(newPath);

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "tropical_island", "version_2", "ISLAND.prob2trace"));

		TraceChecker traceChecker = new TraceChecker(
				jsonFile.getTransitionList(),
				jsonFile.getMachineOperationInfos(),
				stateSpace.getLoadedMachine().getOperations(),
				new HashSet<>(jsonFile.getVariableNames()),
				new HashSet<>(stateSpace.getLoadedMachine().getVariableNames()),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory()
		);


		TraceModifier modifier = traceChecker.getTraceModifier();

		Assertions.assertFalse(modifier.typeIIDetDirty());
		Assertions.assertFalse(modifier.typeIINonDetDirty());
		Assertions.assertTrue(modifier.typeIIIDirty());
		Assertions.assertTrue(modifier.typeIVDirty());
		Assertions.assertTrue(modifier.isDirty());
		Assertions.assertTrue(modifier.tracingFoundResult());



	}


	@Test
	public void integration_long_constructor() throws IOException, PrologTermNotDefinedException, DeltaCalculationException {


		Path oldPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto.mch");
		StateSpace stateSpace1 = proBKernelStub.createStateSpace(oldPath);
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto2.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));


		TraceChecker traceChecker = new TraceChecker(jsonFile.getTransitionList(),
				oldInfos,
				newInfos,
				new HashSet<>(oldVars),
				new HashSet<>(newVars),
				oldPath.toString(),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());


		TraceModifier modifier = traceChecker.getTraceModifier();

		Assertions.assertEquals(1, modifier.getChangelogPhase2().size());
		Assertions.assertEquals(6, modifier.tracesStoredInTypeIII());
		Assertions.assertTrue(traceChecker.getTraceModifier().tracingFoundResult());

	}

	@Test
	public void integration_long_constructor_2() throws IOException, DeltaCalculationException {



		Path oldPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "always_intermediate", "ISLAND.mch");
		StateSpace stateSpace1 = proBKernelStub.createStateSpace(oldPath);
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "always_intermediate", "ISLAND.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "always_intermediate", "ISLAND.prob2trace"));


		TraceChecker traceChecker = new TraceChecker(jsonFile.getTransitionList(),
				oldInfos,
				newInfos,
				new HashSet<>(oldVars),
				new HashSet<>(newVars),
				oldPath.toString(),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());

		Assertions.assertTrue(traceChecker.getTraceModifier().traceMatchesExactly());
	}



	@Test
	public void integration_long_constructor_3() throws IOException, DeltaCalculationException {



		Path oldPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "complexExample", "PitmanController_v6.mch");
		StateSpace stateSpace1 = proBKernelStub.createStateSpace(oldPath);
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",   "complexExample", "PitmanController_TIME_v4.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "complexExample", "pitman_v6_to_time.prob2trace"));


		TraceChecker traceChecker = new TraceChecker(jsonFile.getTransitionList(),
				oldInfos,
				newInfos,
				new HashSet<>(oldVars),
				new HashSet<>(newVars),
				oldPath.toString(),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());


		TraceModifier modifier = traceChecker.getTraceModifier();


		Assertions.assertFalse(modifier.typeIIDetDirty());
		Assertions.assertFalse(modifier.typeIINonDetDirty());
		Assertions.assertFalse(modifier.typeIIIDirty());
		Assertions.assertFalse(modifier.typeIVDirty());
		Assertions.assertFalse(modifier.isDirty());
		Assertions.assertFalse(modifier.tracingFoundResult());
		Assertions.assertTrue(modifier.thereAreIncompleteTraces());


	}


	@Test
	public void integration_show_potential_non_determinism() throws IOException, DeltaCalculationException {



		Path oldPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "complexExample", "PitmanController_TIME_v4.mch");
		StateSpace stateSpace1 = proBKernelStub.createStateSpace(oldPath);
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",   "complexExample", "PitmanController_v6.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "complexExample", "PitmanController_TIME_v4_v2.prob2trace"));


		TraceChecker traceChecker = new TraceChecker(jsonFile.getTransitionList(),
				oldInfos,
				newInfos,
				new HashSet<>(oldVars),
				new HashSet<>(newVars),
				oldPath.toString(),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());


		TraceModifier modifier = traceChecker.getTraceModifier();


		Assertions.assertFalse(modifier.typeIIDetDirty());
		Assertions.assertFalse(modifier.typeIINonDetDirty());
		Assertions.assertFalse(modifier.typeIIIDirty());
		//The result can be non deterministic this is a future problem, if the trace should be explored in such cases

		//	Assertions.assertTrue(modifier.typeIVDirty());
	//	Assertions.assertTrue(modifier.isDirty());
	//	Assertions.assertFalse(modifier.tracingFoundResult());
	//	Assertions.assertTrue(modifier.thereAreIncompleteTraces());

	}



	@Test
	public void machine_has_no_operations() throws IOException, DeltaCalculationException {



		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "machineWithNoOperation.mch");

		StateSpace stateSpace = proBKernelStub.createStateSpace(newPath);

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "tropical_island", "version_2", "ISLAND.prob2trace"));

		TraceChecker traceChecker = new TraceChecker(
				jsonFile.getTransitionList(),
				jsonFile.getMachineOperationInfos(),
				stateSpace.getLoadedMachine().getOperations(),
				new HashSet<>(jsonFile.getVariableNames()),
				new HashSet<>(stateSpace.getLoadedMachine().getVariableNames()),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory()
		);


		TraceModifier modifier = traceChecker.getTraceModifier();

		Assertions.assertFalse(modifier.tracingFoundResult());
		Assertions.assertFalse(modifier.traceMatchesExactly());

	}



	@Test
	public void test_traceModifier_holds_correct_results_1() throws IOException, DeltaCalculationException {


		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND2.mch");

		StateSpace stateSpace = proBKernelStub.createStateSpace(newPath);

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "always_intermediate", "ISLAND.prob2trace"));

		TraceChecker traceChecker = new TraceChecker(
				jsonFile.getTransitionList(),
				jsonFile.getMachineOperationInfos(),
				stateSpace.getLoadedMachine().getOperations(),
				new HashSet<>(jsonFile.getVariableNames()),
				new HashSet<>(stateSpace.getLoadedMachine().getVariableNames()),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory()
		);


		TraceModifier modifier = traceChecker.getTraceModifier();

		Assertions.assertFalse(modifier.typeIIDetDirty());
		Assertions.assertFalse(modifier.typeIINonDetDirty());
		Assertions.assertFalse(modifier.typeIIIDirty());
		Assertions.assertTrue(modifier.typeIVDirty());
		Assertions.assertTrue(modifier.isDirty());
		Assertions.assertTrue(modifier.tracingFoundResult());
	}



	@Test
	public void test_traceModifier_holds_correct_results_2() throws IOException, DeltaCalculationException {


		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND2.mch");

		StateSpace stateSpace = proBKernelStub.createStateSpace(newPath);

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "always_intermediate", "ISLAND_edited_for_ISLAND2.prob2trace"));

		TraceChecker traceChecker = new TraceChecker(
				jsonFile.getTransitionList(),
				jsonFile.getMachineOperationInfos(),
				stateSpace.getLoadedMachine().getOperations(),
				new HashSet<>(jsonFile.getVariableNames()),
				new HashSet<>(stateSpace.getLoadedMachine().getVariableNames()),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory()
		);


		TraceModifier modifier = traceChecker.getTraceModifier();

		Assertions.assertFalse(modifier.typeIIDetDirty());
		Assertions.assertFalse(modifier.typeIINonDetDirty());
		Assertions.assertFalse(modifier.typeIIIDirty());
		Assertions.assertFalse(modifier.typeIVDirty());
		Assertions.assertFalse(modifier.isDirty());
		Assertions.assertTrue(modifier.tracingFoundResult());
	}


	@Test
	public void replay_trace_on_alien_machine_test() throws IOException, DeltaCalculationException {


		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND2.mch");

		StateSpace stateSpace = proBKernelStub.createStateSpace(newPath);

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "testTraceMachine10Steps.prob2trace"));

		TraceChecker traceChecker = new TraceChecker(
				jsonFile.getTransitionList(),
				jsonFile.getMachineOperationInfos(),
				stateSpace.getLoadedMachine().getOperations(),
				new HashSet<>(jsonFile.getVariableNames()),
				new HashSet<>(stateSpace.getLoadedMachine().getVariableNames()),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory()
		);


		TraceModifier modifier = traceChecker.getTraceModifier();

		Assertions.assertFalse(modifier.typeIIDetDirty());
		Assertions.assertFalse(modifier.typeIINonDetDirty());
		Assertions.assertFalse(modifier.typeIIIDirty());
		Assertions.assertFalse(modifier.typeIVDirty());
		Assertions.assertFalse(modifier.isDirty());
		Assertions.assertFalse(modifier.tracingFoundResult());
	}

	@Test
	public void bevarege_vending_renamed_soda() throws IOException, DeltaCalculationException {
		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "vending_machine", "beverage_vending_machine.mch");

		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "vending_machine", "beverage_vending_machine_renamed.mch");

		StateSpace stateSpace1 = proBKernelStub.createStateSpace(pathOld);
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		StateSpace stateSpace2 = proBKernelStub.createStateSpace(path);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines",  "traces",  "vending_machine", "beverage_vending_machine.prob2trace"));


		TraceChecker traceChecker = new TraceChecker(
				jsonFile.getTransitionList(),
				jsonFile.getMachineOperationInfos(),
				stateSpace2.getLoadedMachine().getOperations(),
				new HashSet<>(jsonFile.getVariableNames()),
				new HashSet<>(stateSpace2.getLoadedMachine().getVariableNames()),
				path.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory()
		);



		TraceModifier modifier = traceChecker.getTraceModifier();
		Assertions.assertTrue(modifier.getChangelogPhase2().keySet().size() > 1);

		List<List<PersistenceDelta>> result = modifier.getChangelogPhase3II().entrySet().stream()
				.flatMap(entry -> entry.getValue().values().stream()
						.map(ArrayList::new))
				.filter(entry -> entry.size() == jsonFile.getTransitionList().size())
				.collect(Collectors.toList());
		Assertions.assertEquals(1, result.size());
	}

}
