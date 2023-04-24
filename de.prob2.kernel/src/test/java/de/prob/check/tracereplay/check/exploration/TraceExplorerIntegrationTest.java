package de.prob.check.tracereplay.check.exploration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.prob.ProBKernelStub;
import de.prob.animator.command.ConstructTraceCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.check.tracereplay.check.TestUtils;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.IdentifierMatcher;
import de.prob.cli.CliTestCommon;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

@Deprecated
public class TraceExplorerIntegrationTest {
	private static ProBKernelStub proBKernelStub;

	@BeforeAll
	static void beforeAll() {
		proBKernelStub = CliTestCommon.getInjector().getInstance(ProBKernelStub.class);
	}

	@AfterAll
	static void afterAll() {
		proBKernelStub.killCurrentAnimator();
	}


	@Test
	public void integration_1() throws IOException {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch"));


		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				IdentifierMatcher.generateAllPossibleMappingVariations(emptyList(), stateSpace.getLoadedMachine().getOperations(), stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec", "getfloors").collect(Collectors.toSet()), new TestUtils.StubFactoryImplementation());


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						emptyList(),
						stateSpace,
						emptySet(),
						selectedMappingsToResultsKeys);



		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void integration_1_traceReplay_three_transitions_with_smaller_signature() throws IOException {


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

		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				IdentifierMatcher.generateAllPossibleMappingVariations(persistentTransitions, toCompare.getLoadedMachine().getOperations(), operationInfoOld, Stream.of("inc").collect(Collectors.toSet()), new TestUtils.StubFactoryImplementation());


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(true, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(persistentTransitions,
						toCompare,
						emptySet(),
						selectedMappingsToResultsKeys);


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Assertions.assertEquals(3,resultCleaned.entrySet().size());

	}

	@Test
	public void integration_2_traceReplay2_three_transitions_with_larger_signature() throws IOException {


		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedSignature", "Lift4_2.mch"));
		Map<String, OperationInfo> oldInformation = stateSpace1.getLoadedMachine().getOperations();

		StateSpace stateSpace2 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedSignature", "Lift4.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition first = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("a","1");
		}}, Collections.emptyMap(), singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition initNew = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition firstNew_var1 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
			put("y","0");
			put("z","0");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition firstNew_var2 = new PersistentTransition("inc", new HashMap<String, String>() {{

			put("x","-1");
			put("y","1");
			put("z","1");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition firstNew_var3 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","-1");
			put("y","1");
			put("z","1");
		}},

				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition secondNew = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");


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

		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				IdentifierMatcher.generateAllPossibleMappingVariations(transitionList, stateSpace2.getLoadedMachine().getOperations(), oldInformation, Stream.of("inc", "dec").collect(Collectors.toSet()), new TestUtils.StubFactoryImplementation());


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(true, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(transitionList,
						stateSpace2,
						emptySet(),
						selectedMappingsToResultsKeys);

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Assertions.assertEquals(expected,resultCleaned);
	}

	@Test
	public void integration_3_traceReplay_no_type_III_candidates() throws IOException {

		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift", "Lift.mch"));
		Map<String, OperationInfo> oldInformation = stateSpace1.getLoadedMachine().getOperations();


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "LiftWithLevels.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), singletonMap("level", "0"), emptySet(), emptyList(), emptyList(), "");

		PersistentTransition first = new PersistentTransition("inc", emptyMap(), emptyMap(),
				singletonMap("level", "1"), emptySet(), emptyList(), emptyList(), "");

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("level", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");



		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> expected = new HashMap<>();
		expected.put(emptyMap(), Stream.of(new PersistenceDelta(init, singletonList(init)),
				new PersistenceDelta(first, singletonList(first)),new PersistenceDelta(second, singletonList(second))).collect(toList()));

		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				IdentifierMatcher.generateAllPossibleMappingVariations(Stream.of(init, first, second).collect(toList()), stateSpace.getLoadedMachine().getOperations(), oldInformation, emptySet(), new TestUtils.StubFactoryImplementation());


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(true, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						Stream.of(init, first, second).collect(toList()),
						stateSpace,
						emptySet(),
						selectedMappingsToResultsKeys);


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Assertions.assertEquals(expected,resultCleaned);
	}

	@Test
	public void integration_4_traceReplay_one_combination_is_not_suitable() throws IOException {


		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift", "oneWrongParameter",  "OneWrongParameterCounter.mch"));
		Map<String, OperationInfo> oldInformation = stateSpace1.getLoadedMachine().getOperations();


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "oneWrongParameter",  "OneWrongParameter.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), singletonMap("levels", "0"), emptySet(), emptyList(), emptyList(), "");

		PersistentTransition first = new PersistentTransition("inc", singletonMap("a", "1"), emptyMap(),
				singletonMap("floors", "1"), emptySet(), emptyList(), emptyList(), "");

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition initNew = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition firstNew_var1 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
			put("y","0");
			put("z","TRUE");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition firstNew_var2 = new PersistentTransition("inc", new HashMap<String, String>() {{

			put("x","0");
			put("y","1");
			put("z","TRUE");
		}}, Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition secondNew = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), "");


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

		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				IdentifierMatcher.generateAllPossibleMappingVariations(Stream.of(init, first, second).collect(toList()), stateSpace.getLoadedMachine().getOperations(), oldInformation, singleton("inc"), new TestUtils.StubFactoryImplementation());


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(true, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						Stream.of(init, first, second).collect(toList()),
						stateSpace,
						emptySet(),
						selectedMappingsToResultsKeys);

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Assertions.assertEquals(expected,resultCleaned);
	}



}
