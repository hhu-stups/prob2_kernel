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
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
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

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

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

		Map<Map<String,  Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
						.replayTrace(
								Collections.emptyList(),
								stateSpace,
								stateSpace.getLoadedMachine().getOperations(),
								Stream.of("inc", "dec", "getfloors").collect(Collectors.toSet()),
								emptySet(),
								Stream.of("floors").collect(Collectors.toSet()),
								emptySet(),
								emptySet());

		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void integration_1_traceReplay2_three_transitions_with_smaller_signature() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift2.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("a","1");
			put("b","0");
			put("c","0");
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


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> expected = new HashMap<>();


		Map<String, String> expectedHelper_dec = new HashMap<>();
		expectedHelper_dec.put("floors", "levels");


		Map<String, String> expectedHelper_1 = new HashMap<>();
		expectedHelper_1.put("floors", "levels");
		expectedHelper_1.put("a", "x");

		Map<String, String> expectedHelper_2 = new HashMap<>();
		expectedHelper_2.put("floors", "levels");
		expectedHelper_2.put("b", "x");

		Map<String, String> expectedHelper_3 = new HashMap<>();
		expectedHelper_3.put("floors", "levels");
		expectedHelper_3.put("c", "x");


		Map<String,  Map<String, String>> expected_inner1 = new HashMap<>();
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


		Map<Map<String,  Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(true, new TestUtils.StubFactoryImplementation())
						.replayTrace(transitionList,
								stateSpace,
								stateSpace.getLoadedMachine().getOperations(),
								Stream.of("inc", "dec").collect(Collectors.toSet()),
								emptySet(),
								Stream.of("levels").collect(Collectors.toSet()),
								emptySet(),
								emptySet());



		Assert.assertEquals(expected,result);
	}

	@Test
	public void integration_2_traceReplay2_three_transitions_with_larger_signature() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift4.mch"));



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



		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(true, new TestUtils.StubFactoryImplementation())
						.replayTrace(transitionList,
								stateSpace,
								stateSpace.getLoadedMachine().getOperations(),
						Stream.of("inc", "dec").collect(Collectors.toSet()),
								emptySet(),
								Stream.of("levels").collect(Collectors.toSet()),
								emptySet(),
								emptySet());



		Assert.assertEquals(expected,result);
	}


	@Test
	public void integration_3_traceReplay_no_type_III_candidates() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift3.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), singletonMap("level", "0"), emptySet(), emptyList());

		PersistentTransition first = new PersistentTransition("inc", emptyMap(), emptyMap(),
				singletonMap("level", "1"), emptySet(), emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("level", "0"), Collections.emptySet(), Collections.emptyList());



		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> expected = new HashMap<>();
		expected.put(emptyMap(), Stream.of(new PersistenceDelta(init, singletonList(init)),
				new PersistenceDelta(first, singletonList(first)),new PersistenceDelta(second, singletonList(second))).collect(toList()));


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(true, new TestUtils.StubFactoryImplementation())
						.replayTrace(
								Stream.of(init,first,second).collect(toList()),
								stateSpace,
								stateSpace.getLoadedMachine().getOperations(),
								emptySet(),
								emptySet(),
								emptySet(),
								emptySet(),
								emptySet());



		Assert.assertEquals(expected,result);
	}


	@Test
	public void integration_4_traceReplay_one_combination_is_not_suitable() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift5.mch"));


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
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());




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



		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(true, new TestUtils.StubFactoryImplementation())
						.replayTrace(
								Stream.of(init,first,second).collect(toList()),
								stateSpace,
								stateSpace.getLoadedMachine().getOperations(),
								singleton("inc"),
								emptySet(),
								singleton("levels"),
								emptySet(),
								emptySet());



		Assert.assertEquals(expected,result);
	}


	@Test
	public void integration_5_traceReplay_empty_changes() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift5.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), singletonMap("levels", "0"), emptySet(), emptyList());

		PersistentTransition first = new PersistentTransition("inc", emptyMap(), emptyMap(),
				singletonMap("levels", "1"), emptySet(), emptyList());

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
		}},
				Collections.emptyMap(), singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());




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
		expectedHelper_1.put("levels", "levels");

		Map<String, String> expectedHelper_2 = new HashMap<>();
		expectedHelper_2.put("levels", "levels");


		Map<String, Map<String, String>> expected_inner1 = new HashMap<>();
		expected_inner1.put("inc", expectedHelper_1);

		Map<String, Map<String, String>> expected_inner2 = new HashMap<>();
		expected_inner2.put("inc", expectedHelper_2);




		expected.put(expected_inner1,  expected1);
		expected.put(expected_inner2,  expected2);



		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(true, new TestUtils.StubFactoryImplementation())
						.replayTrace(
								Stream.of(init,first,second).collect(toList()),
								stateSpace,
								stateSpace.getLoadedMachine().getOperations(),
								singleton("inc"),
								emptySet(),
								singleton("levels"),
								emptySet(),
								emptySet());



		Assert.assertEquals(expected,result);
	}


	@Test
	public void integration_5_traceReplay_none_combination_is_not_suitable() throws IOException, ModelTranslationError {

		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift2.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), singletonMap("level", "0"), emptySet(), emptyList());

		PersistentTransition first = new PersistentTransition("inc", singletonMap("x", "true"), emptyMap(),
				singletonMap("level", "1"), emptySet(), emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("level", "0"), Collections.emptySet(), Collections.emptyList());



		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(true, new TestUtils.StubFactoryImplementation()).replayTrace(
						Stream.of(init,first,second).collect(toList()),
						stateSpace,
						stateSpace.getLoadedMachine().getOperations(),
						emptySet(),
						emptySet(),
						emptySet(),
						emptySet(),
						emptySet());


		Assert.assertEquals(emptyMap(),result);
	}


	@Test
	public void integration_5_traceReplay_ini_was_not_set() throws IOException, ModelTranslationError {

		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift2.mch"));


		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), singletonMap("floors", "0"), emptySet(), emptyList());

		PersistentTransition first = new PersistentTransition("inc", singletonMap("x", "1"), emptyMap(),
				singletonMap("levels", "1"), emptySet(), emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());


		PersistenceDelta deltaInit = new PersistenceDelta(init, singletonList(
				new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
						emptyMap(), singletonMap("levels", "0"), emptySet(), emptyList())));

		PersistenceDelta deltaFirst = new PersistenceDelta(first, singletonList(first));

		PersistenceDelta deltaSecond = new PersistenceDelta(second, singletonList(second));

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> expected = new HashMap<>();
		expected.put(emptyMap(), Arrays.asList(deltaInit, deltaFirst, deltaSecond));

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
						.replayTrace(Stream.of(init,first,second).collect(toList()),
								stateSpace,
								stateSpace.getLoadedMachine().getOperations(),
								emptySet(),
								emptySet(),
								emptySet(),
								emptySet(),
								emptySet());


		Assert.assertEquals(expected,result);
	}
	
	
	@Test
	public void integration_6_realWorldExample() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "LiftProto2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "LiftProto.prob2trace"));

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
						.replayTrace(
								jsonFile.getTrace().getTransitionList(),
								stateSpace,
								jsonFile.getMachineOperationInfos(),
								singleton("inc"),
								emptySet(),
								emptySet(),
								emptySet(),
								emptySet());

		System.out.println(result);
	}


}
