package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.PersistenceDelta;
import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;

public class TraceExplorerIntegrationTest {


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
	public void integration_1() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift.mch"));

		Set<List<PersistenceDelta>> result = TraceExplorer.replayTrace(Collections.emptyList(), stateSpace, stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec", "getfloors").collect(Collectors.toSet()));

		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void integration_2_initialisation_clause() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		Set<List<PersistenceDelta>> result =
				TraceExplorer.replayTrace(Collections.singletonList(init), stateSpace, stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec", "getfloors").collect(Collectors.toSet()));



		Assert.assertEquals(1, result.size());
		assert result.size()>=1;
		PersistenceDelta expected = result.stream().findFirst().get().get(0);
		Assert.assertEquals(expected.getOldTransition(), init);
		Assert.assertEquals(expected.getNewTransitions().get(0), init);
	}


	@Test
	public void integration_3_three_transitions_with_exact_match() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		PersistenceDelta delta1 = new PersistenceDelta(init, Collections.singletonList(init));
		PersistenceDelta delta2 = new PersistenceDelta(first, Collections.singletonList(first));
		PersistenceDelta delta3 = new PersistenceDelta(second, Collections.singletonList(second));

		List<PersistenceDelta> expected = Arrays.asList(delta1,delta2,delta3);


		Set<List<PersistenceDelta>> result =
				TraceExplorer.replayTrace(Arrays.asList(init, first, second), stateSpace, stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec", "getfloors").collect(Collectors.toSet()));

		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(expected, new ArrayList<>(result.stream().findFirst().get()));
	}


	@Test
	public void integration_4_three_transitions_with_1_change_ignore_init() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift2.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		PersistentTransition initNew = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew = new PersistentTransition("inc", singletonMap("x", "0"),
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition secondNew = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());


		PersistenceDelta delta1 = new PersistenceDelta(init, Collections.singletonList(initNew));
		PersistenceDelta delta2 = new PersistenceDelta(first, Collections.singletonList(firstNew));
		PersistenceDelta delta3 = new PersistenceDelta(second, Collections.singletonList(secondNew));

		List<PersistenceDelta> expected = Arrays.asList(delta1,delta2,delta3);

		Set<List<PersistenceDelta>> result =
				TraceExplorer.replayTrace(Arrays.asList(init, first, second), stateSpace, stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec", "getfloors").collect(Collectors.toSet()));


		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(expected, new ArrayList<>(result.stream().findFirst().get()));
	}


	@Test
	public void integration_5_three_transitions_with_larger_signature_change_ignore_init() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift4.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", singletonMap("x", "1"),
				Collections.emptyMap(), singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

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
			put("x","0");
			put("y","1");
			put("z","0");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var3 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","0");
			put("y","0");
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

		List<PersistenceDelta> expected1 = Arrays.asList(delta1,delta2_var1,delta3);
		List<PersistenceDelta> expected2 = Arrays.asList(delta1,delta2_var2,delta3);
		List<PersistenceDelta> expected3 = Arrays.asList(delta1,delta2_var3,delta3);

		Set<List<PersistenceDelta>> result =
				TraceExplorer.replayTrace(Arrays.asList(init, first, second), stateSpace, stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec", "getfloors").collect(Collectors.toSet()));


		Set<List<PersistenceDelta>> expected = new HashSet<>(Arrays.asList(expected1, expected2, expected3));

		Assert.assertTrue(dummyCompare(result, expected));

	}


	@Test
	public void integration_6_three_transitions_with_smaller_signature() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift2.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
			put("y","0");
			put("z","0");
		}},
				Collections.emptyMap(), singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		PersistentTransition initNew = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var1 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var2 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("y","1");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var3 = new PersistentTransition("inc", new HashMap<String, String>() {{
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

		List<PersistenceDelta> expected1 = Arrays.asList(delta1,delta2_var1,delta3);
		List<PersistenceDelta> expected2 = Arrays.asList(delta1,delta2_var2,delta3);
		List<PersistenceDelta> expected3 = Arrays.asList(delta1,delta2_var3,delta3);

		Set<List<PersistenceDelta>> result =
				TraceExplorer.replayTrace(Arrays.asList(init, first, second), stateSpace, stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec", "getfloors").collect(Collectors.toSet()));


		Set<List<PersistenceDelta>> expected = new HashSet<>(Arrays.asList(expected1, expected2, expected3));

		Assert.assertTrue(dummyCompare(result, expected));
	}


	@Test
	public void integration_1_traceReplay2_three_transitions_with_smaller_signature() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift2.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
			put("y","0");
			put("z","0");
		}},
				Collections.emptyMap(), singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		PersistentTransition initNew = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var1 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var2 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","0");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var3 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","0");
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


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> expected =
				TraceExplorer.generateAllPossibleMappingVariations(transitionList, stateSpace.getLoadedMachine().getOperations(),
						Stream.of("inc", "dec").collect(Collectors.toSet())).stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(entry, new ArrayList<PersistenceDelta>()))
				.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_dec = new HashMap<>();
		expectedHelper_dec.put(TraceExplorer.MappingNames.VARIABLES, singletonMap("floors", "levels"));
		expectedHelper_dec.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, emptyMap());
		expectedHelper_dec.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_1 = new HashMap<>();
		expectedHelper_1.put(TraceExplorer.MappingNames.VARIABLES, singletonMap("floors", "levels"));
		expectedHelper_1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, singletonMap("x", "x"));
		expectedHelper_1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_2 = new HashMap<>();
		expectedHelper_2.put(TraceExplorer.MappingNames.VARIABLES, singletonMap("floors", "levels"));
		expectedHelper_2.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, singletonMap("x", "y"));
		expectedHelper_2.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_3 = new HashMap<>();
		expectedHelper_3.put(TraceExplorer.MappingNames.VARIABLES, singletonMap("floors", "levels"));
		expectedHelper_3.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, singletonMap("x", "z"));
		expectedHelper_3.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expected_inner1 = new HashMap<>();
		expected_inner1.put("inc", expectedHelper_1);
		expected_inner1.put("dec", expectedHelper_dec);

		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expected_inner2 = new HashMap<>();
		expected_inner2.put("inc", expectedHelper_2);
		expected_inner2.put("dec", expectedHelper_dec);

		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expected_inner3 = new HashMap<>();
		expected_inner3.put("inc", expectedHelper_3);
		expected_inner3.put("dec", expectedHelper_dec);


		expected.put(expected_inner1,  expected1);
		expected.put(expected_inner2,  expected2);
		expected.put(expected_inner3,  expected3);


		for(Map.Entry<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> entry : expected.entrySet() ){
			System.out.println(entry);
		}
		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result =
				TraceExplorer.replayTrace2(transitionList, stateSpace, stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec").collect(Collectors.toSet()));



		Assert.assertEquals(expected,result);
	}


	public void integration_2_traceReplay2_three_transitions_with_larger_signature() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift4.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
		}},
				Collections.emptyMap(), singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

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
			put("x","0");
			put("y","1");
			put("z","0");
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew_var3 = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","0");
			put("y","0");
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


		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> expected =
				TraceExplorer.generateAllPossibleMappingVariations(transitionList, stateSpace.getLoadedMachine().getOperations(),
						Stream.of("inc", "dec").collect(Collectors.toSet())).stream()
						.map(entry -> new AbstractMap.SimpleEntry<>(entry, new ArrayList<PersistenceDelta>()))
						.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_dec = new HashMap<>();
		expectedHelper_dec.put(TraceExplorer.MappingNames.VARIABLES, singletonMap("floors", "levels"));
		expectedHelper_dec.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, emptyMap());
		expectedHelper_dec.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_1 = new HashMap<>();
		expectedHelper_1.put(TraceExplorer.MappingNames.VARIABLES, singletonMap("floors", "levels"));
		expectedHelper_1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, singletonMap("x", "x"));
		expectedHelper_1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_2 = new HashMap<>();
		expectedHelper_2.put(TraceExplorer.MappingNames.VARIABLES, singletonMap("floors", "levels"));
		expectedHelper_2.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, singletonMap("y", "x"));
		expectedHelper_2.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_3 = new HashMap<>();
		expectedHelper_3.put(TraceExplorer.MappingNames.VARIABLES, singletonMap("floors", "levels"));
		expectedHelper_3.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, singletonMap("z", "x"));
		expectedHelper_3.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expected_inner1 = new HashMap<>();
		expected_inner1.put("inc", expectedHelper_1);
		expected_inner1.put("dec", expectedHelper_dec);

		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expected_inner2 = new HashMap<>();
		expected_inner2.put("inc", expectedHelper_2);
		expected_inner2.put("dec", expectedHelper_dec);

		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expected_inner3 = new HashMap<>();
		expected_inner3.put("inc", expectedHelper_3);
		expected_inner3.put("dec", expectedHelper_dec);


		expected.put(expected_inner1,  expected1);
		expected.put(expected_inner2,  expected2);
		expected.put(expected_inner3,  expected3);


		for(Map.Entry<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> entry : expected.entrySet() ){
			System.out.println(entry);
		}
		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result =
				TraceExplorer.replayTrace2(transitionList, stateSpace, stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec").collect(Collectors.toSet()));



		Assert.assertEquals(expected,result);
	}

	public boolean dummyCompare(Set<List<PersistenceDelta>> d1, Set<List<PersistenceDelta>> d2){
		for(List<PersistenceDelta> d : d1){
			if(dummyCompare_aux(d, d2)){
				return true;
			}
		}
		return false;
	}

	public boolean dummyCompare_aux(List<PersistenceDelta> d1, Set<List<PersistenceDelta>> d2){
		for(List<PersistenceDelta> inner : d2){
			if(d1.equals(inner)){
				return true;
			}
		}
		return false;
	}
}
