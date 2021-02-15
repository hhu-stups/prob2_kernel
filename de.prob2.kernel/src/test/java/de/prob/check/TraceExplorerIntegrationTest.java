package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.animator.command.ConstructTraceCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.PersistenceDelta;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class TraceExplorerIntegrationTest {


	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null && proBKernelStub==null) {
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}

	}

	@AfterEach
	public void cleanUp(){
		proBKernelStub.killCurrentAnimator();
	}


	@Test
	public void integration_1() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch"));

		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						emptyList(),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						stateSpace.getLoadedMachine().getOperations(),
						Stream.of("inc", "dec", "getfloors").collect(Collectors.toSet()),
						emptySet());



		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void integration_1_traceReplay_three_transitions_with_smaller_signature() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "reducedSigLength", "OneTypeIIICandidateCounterPart.mch"));

		Map<String, OperationInfo> operationInfoOld = stateSpace.getLoadedMachine().getOperations();

		Trace t = new Trace(stateSpace);

		ConstructTraceCommand constructTraceCommand = new ConstructTraceCommand(stateSpace, t.getCurrentState(), Arrays.asList(
				Transition.INITIALISE_MACHINE_NAME, "inc", "dec"),
				Arrays.asList(
						new ClassicalB("levels=0", FormulaExpand.EXPAND),
						new ClassicalB("levels=1&a=1", FormulaExpand.EXPAND),
						new ClassicalB("levels=0", FormulaExpand.EXPAND)));


		stateSpace.execute(constructTraceCommand);

		List<PersistentTransition> persistentTransitions = PersistentTransition.createFromList(constructTraceCommand.getNewTransitions());

		StateSpace toCompare = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "reducedSigLength", "OneTypeIIICandidate.mch"));

		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(true, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(persistentTransitions,
						toCompare,
						toCompare.getLoadedMachine().getOperations(),
						operationInfoOld,
						Stream.of("inc").collect(Collectors.toSet()),
						emptySet());


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Assertions.assertEquals(3,resultCleaned.entrySet().size());

	}

	@Test
	public void integration_2_traceReplay2_three_transitions_with_larger_signature() throws IOException, ModelTranslationError {


		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedSignature", "Lift4_2.mch"));
		Map<String, OperationInfo> oldInformation = stateSpace1.getLoadedMachine().getOperations();

		StateSpace stateSpace2 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedSignature", "Lift4.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("a","1");
		}}, Collections.emptyMap(), singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition initNew = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var1 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
			put("y","0");
			put("z","0");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var2 = new PersistentTransition("inc", new HashMap<String, String>() {{

			put("x","-1");
			put("y","1");
			put("z","1");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var3 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","-1");
			put("y","1");
			put("z","1");
		}},

				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition secondNew = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());


		//The explorer will find multiple possible solutions, we expect at least 3, because we want to test if the mapping from
		//old to new parameters is working properly

		PersistenceDelta delta1 = new PersistenceDelta(init, Collections.singletonList(initNew));
		PersistenceDelta delta2_var1 = new PersistenceDelta(first, Collections.singletonList(firstNew_var1));
		PersistenceDelta delta2_var2 = new PersistenceDelta(first, Collections.singletonList(firstNew_var2));
		PersistenceDelta delta2_var3 = new PersistenceDelta(first, Collections.singletonList(firstNew_var3));

		PersistenceDelta delta3 = new PersistenceDelta(second, Collections.singletonList(secondNew));

		List<PersistentTransition> transitionList = Arrays.asList(init, first, second);

		List<PersistenceDelta> expected1 = Arrays.asList(delta1,delta2_var1,delta3);
		List<PersistenceDelta> expected2 = Arrays.asList(delta1,delta2_var2,delta3);
		List<PersistenceDelta> expected3 = Arrays.asList(delta1,delta2_var3,delta3);


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> expected = new HashMap<>();


		Map<String, String> expectedHelper_dec = new HashMap<>();
		expectedHelper_dec.put("floors", "levels");


		Map<String, String> expectedHelper_1 = new HashMap<>();
		expectedHelper_1.put("floors", "levels");
		expectedHelper_1.put("a", "x");

		Map<String, String> expectedHelper_2 = new HashMap<>();
		expectedHelper_2.put("floors", "levels");
		expectedHelper_2.put("a", "y");

		Map<String, String> expectedHelper_3 = new HashMap<>();
		expectedHelper_3.put("floors", "levels");
		expectedHelper_3.put("a", "z");


		Map<String, Map<String, String>> expected_inner1 = new HashMap<>();
		expected_inner1.put("inc", expectedHelper_1);
		expected_inner1.put("dec", expectedHelper_dec);

		Map<String, Map<String, String>> expected_inner2 = new HashMap<>();
		expected_inner2.put("inc", expectedHelper_2);
		expected_inner2.put("dec", expectedHelper_dec);

		Map<String, Map<String, String>> expected_inner3 = new HashMap<>();
		expected_inner3.put("inc", expectedHelper_3);
		expected_inner3.put("dec", expectedHelper_dec);


		expected.put(expected_inner1,  expected1);
		expected.put(expected_inner2,  expected2);
		expected.put(expected_inner3,  expected3);


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(true, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(transitionList,
						stateSpace2,
						stateSpace2.getLoadedMachine().getOperations(),
						oldInformation,
						Stream.of("inc", "dec").collect(Collectors.toSet()),
						emptySet());

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Assertions.assertEquals(expected,resultCleaned);
	}

	@Test
	public void integration_3_traceReplay_no_type_III_candidates() throws IOException, ModelTranslationError {

		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift", "Lift.mch"));
		Map<String, OperationInfo> oldInformation = stateSpace1.getLoadedMachine().getOperations();


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "LiftWithLevels.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), singletonMap("level", "0"), emptySet(), emptyList());

		PersistentTransition first = new PersistentTransition("inc", emptyMap(), emptyMap(),
				singletonMap("level", "1"), emptySet(), emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("level", "0"), Collections.emptySet(), Collections.emptyList());



		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> expected = new HashMap<>();
		expected.put(emptyMap(), Stream.of(new PersistenceDelta(init, singletonList(init)),
				new PersistenceDelta(first, singletonList(first)),new PersistenceDelta(second, singletonList(second))).collect(toList()));


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(true, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						Stream.of(init, first, second).collect(toList()),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						oldInformation,
						emptySet(),
						emptySet());


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Assertions.assertEquals(expected,resultCleaned);
	}

	@Test
	public void integration_4_traceReplay_one_combination_is_not_suitable() throws IOException, ModelTranslationError {


		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift", "oneWrongParameter",  "OneWrongParameterCounter.mch"));
		Map<String, OperationInfo> oldInformation = stateSpace1.getLoadedMachine().getOperations();


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "oneWrongParameter",  "OneWrongParameter.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), singletonMap("levels", "0"), emptySet(), emptyList());

		PersistentTransition first = new PersistentTransition("inc", singletonMap("a", "1"), emptyMap(),
				singletonMap("floors", "1"), emptySet(), emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition initNew = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var1 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
			put("y","0");
			put("z","TRUE");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var2 = new PersistentTransition("inc", new HashMap<String, String>() {{

			put("x","0");
			put("y","1");
			put("z","TRUE");
		}}, Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());


		PersistentTransition secondNew = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());


		//The explorer will find multiple possible solutions, we expect at least 3, because we want to test if the mapping from
		//old to new parameters is working properly

		PersistenceDelta delta1 = new PersistenceDelta(init, Collections.singletonList(initNew));
		PersistenceDelta delta2_var1 = new PersistenceDelta(first, Collections.singletonList(firstNew_var1));
		PersistenceDelta delta2_var2 = new PersistenceDelta(first, Collections.singletonList(firstNew_var2));

		PersistenceDelta delta3 = new PersistenceDelta(second, Collections.singletonList(secondNew));


		List<PersistenceDelta> expected1 = Arrays.asList(delta1,delta2_var1,delta3);
		List<PersistenceDelta> expected2 = Arrays.asList(delta1,delta2_var2,delta3);


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> expected = new HashMap<>();


		Map<String, String> expectedHelper_1 = new HashMap<>();
		expectedHelper_1.put("floors", "levels");
		expectedHelper_1.put("a", "x");

		Map<String, String> expectedHelper_2 = new HashMap<>();
		expectedHelper_2.put("floors", "levels");
		expectedHelper_2.put("a", "y");


		Map<String, Map<String, String>> expected_inner1 = new HashMap<>();
		expected_inner1.put("inc", expectedHelper_1);

		Map<String, Map<String, String>> expected_inner2 = new HashMap<>();
		expected_inner2.put("inc", expectedHelper_2);


		expected.put(expected_inner1,  expected1);
		expected.put(expected_inner2,  expected2);

		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(true, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						Stream.of(init, first, second).collect(toList()),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						oldInformation,
						singleton("inc"),
						emptySet());

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Assertions.assertEquals(expected,resultCleaned);
	}


	@Test
	public void integration_6_realWorldExample() throws IOException, ModelTranslationError {


		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto.mch"));
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();

		StateSpace stateSpace2 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto2.mch"));
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));

		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTrace().getTransitionList(),
						stateSpace2,
						newInfos,
						oldInfos,
						singleton("inc"),
						emptySet());

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Set<Map<String, String>> resultToCompare = resultCleaned.keySet().stream().flatMap(entry -> entry.values().stream()).collect(toSet());

		Set<Map<String, String>> expectedHelper = TraceCheckerUtils.allDiagonals(Arrays.asList("x", "y"), Arrays.asList("x", "y", "z"));
		Set<Map<String, String>> expected = expectedHelper.stream().peek(entry -> entry.put("floors", "levels")).collect(toSet());
		Assertions.assertEquals(expected, resultToCompare);
	}




}
