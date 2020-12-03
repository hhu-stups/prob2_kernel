package de.prob.check;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
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

public class TraceExplorerUnitTest {



	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createStub(){
		if(traceManager==null && proBKernelStub==null) {
			System.setProperty("prob.home", "/home/sebastian/prob_prolog");
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}

	}


	@Test
	public void variationsFinder_test_1(){
		PersistentTransition first = new PersistentTransition("inc", Collections.singletonMap("x", "1"),
				emptyMap(), emptyMap(), Collections.emptySet(), Collections.emptyList());

		Set<Map<String, String>> result = TraceExplorer.possibleConstellations(first.getParameters(), Stream.of("x", "y", "z").collect(Collectors.toList()));


		Set<Map<String, String>> expected = new HashSet<>();
		expected.add(Collections.singletonMap("y","1"));
		expected.add(Collections.singletonMap("x","1"));
		expected.add(Collections.singletonMap("z","1"));

		Assert.assertEquals(expected, result);

	}

	@Test
	public void variationsFinder_test_2(){

		Map<String, String> parameter = new HashMap<>();
		parameter.put("x", "1");
		parameter.put("y", "2");

		PersistentTransition first = new PersistentTransition("inc", parameter,
				emptyMap(), emptyMap(), Collections.emptySet(), Collections.emptyList());

		Set<Map<String, String>> result = TraceExplorer.possibleConstellations(first.getParameters(), Stream.of("x", "y", "z").collect(Collectors.toList()));


		Set<Map<String, String>> expected = new HashSet<>();
		Map<String, String> result1 = new HashMap<>();
		result1.put("x", "2");
		result1.put("y", "1");
		expected.add(result1);
		Map<String, String> result2 = new HashMap<>();
		result2.put("x", "1");
		result2.put("y", "2");
		expected.add(result2);
		Map<String, String> result3 = new HashMap<>();
		result3.put("z", "1");
		result3.put("y", "2");
		expected.add(result3);
		Map<String, String> result4 = new HashMap<>();
		result4.put("z", "2");
		result4.put("y", "1");
		expected.add(result4);
		Map<String, String> result5 = new HashMap<>();
		result5.put("x", "2");
		result5.put("z", "1");
		expected.add(result5);
		Map<String, String> result6 = new HashMap<>();
		result6.put("x", "1");
		result6.put("z", "2");
		expected.add(result6);


		Assert.assertEquals(expected, result);

	}


	@Test
	public void variationsFinder_test_3(){

		Map<String, String> parameter = new HashMap<>();
		parameter.put("x", "1");
		parameter.put("y", "2");
		parameter.put("z", "3");

		PersistentTransition first = new PersistentTransition("inc", parameter,
				emptyMap(), emptyMap(), Collections.emptySet(), Collections.emptyList());

		Set<Map<String, String>> result = TraceExplorer.possibleConstellations(first.getParameters(), Stream.of("x", "y").collect(Collectors.toList()));


		Set<Map<String, String>> expected = new HashSet<>();
		Map<String, String> result1 = new HashMap<>();
		result1.put("y", "1");
		result1.put("z", "2");
		expected.add(result1);
		Map<String, String> result2 = new HashMap<>();
		result2.put("x", "1");
		result2.put("z", "2");
		expected.add(result2);
		Map<String, String> result3 = new HashMap<>();
		result3.put("x", "2");
		result3.put("z", "1");
		expected.add(result3);
		Map<String, String> result4 = new HashMap<>();
		result4.put("y", "2");
		result4.put("z", "1");
		expected.add(result4);
		Map<String, String> result5 = new HashMap<>();
		result5.put("x", "1");
		result5.put("y", "2");
		expected.add(result5);
		Map<String, String> result6 = new HashMap<>();
		result6.put("x", "2");
		result6.put("y", "1");
		expected.add(result6);


		Assert.assertEquals(expected, result);

	}

	@Test
	public void variationsFinder_test_4_empty_result(){

		Map<String, String> parameter = new HashMap<>();


		PersistentTransition first = new PersistentTransition("inc", parameter,
				emptyMap(), emptyMap(), Collections.emptySet(), Collections.emptyList());

		Set<Map<String, String>> result = TraceExplorer.possibleConstellations(first.getParameters(), Stream.of("x", "y", "z").collect(Collectors.toList()));



		Assert.assertTrue( result.isEmpty());

	}


	@Test
	public void variationsFinder_test_5(){

		Map<String, String> parameter = new HashMap<>();
		parameter.put("x", "1");
		parameter.put("y", "2");
		parameter.put("z", "3");

		PersistentTransition first = new PersistentTransition("inc", parameter,
				emptyMap(), emptyMap(), Collections.emptySet(), Collections.emptyList());

		Set<Map<String, String>> result = TraceExplorer.possibleConstellations(first.getParameters(), Collections.emptyList());



		Assert.assertTrue( result.isEmpty());

	}

	@Test
	public void variationsFinder_test_6(){

		Map<String, String> parameter = new HashMap<>();
		parameter.put("x", "1");

		PersistentTransition first = new PersistentTransition("inc", parameter,
				emptyMap(), emptyMap(), Collections.emptySet(), Collections.emptyList());

		List<String> paras = new ArrayList<>();
		paras.add("x");
		paras.add("y");
		paras.add("z");
		Set<Map<String, String>> result = TraceExplorer.possibleConstellations(first.getParameters(), paras);

		Set<Map<String, String>> expected = new HashSet<>();
		Map<String, String> result1 = new HashMap<>();
		result1.put("x", "1");
		expected.add(result1);
		Map<String, String> result2 = new HashMap<>();
		result2.put("y", "1");
		expected.add(result2);
		Map<String, String> result3 = new HashMap<>();
		result3.put("z", "1");
		expected.add(result3);


		Assert.assertEquals( expected, result);

	}

	@Test
	public void calculateMappings_test_1(){
		PersistentTransition floors = new PersistentTransition("floors", Collections.singletonMap("x", "4"),
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		OperationInfo operationInfo = new OperationInfo("floors", Collections.singletonList("y"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("levels"),
				Collections.emptyList());

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = TraceExplorer.calculateVarMappings(floors, operationInfo);


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper = new HashMap<>();
		Map<String, String> mapping1 = new HashMap<>();
		mapping1.put("x", "y");
		Map<String, String> mapping2 = new HashMap<>();
		mapping2.put("floors", "levels");

		expectedHelper.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1);
		expectedHelper.put(TraceExplorer.MappingNames.VARIABLES, mapping2);
		expectedHelper.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		expected.add(expectedHelper);

		Assert.assertTrue(expected.containsAll(result));
	}



	@Test
	public void calculateMappings_test_2(){
		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x","1");
			put("y","0");
			put("z","0");
		}},
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		OperationInfo operationInfo = new OperationInfo("floors", Collections.singletonList("y"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("levels"),
				Collections.emptyList());

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = TraceExplorer.calculateVarMappings(floors, operationInfo);


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("y", "y");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper2 = new HashMap<>();
		Map<String, String> mapping1_2 = new HashMap<>();
		mapping1_2.put("y", "z");
		Map<String, String> mapping2_2 = new HashMap<>();
		mapping2_2.put("floors", "levels");

		expectedHelper2.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_2);
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES, mapping2_2);
		expectedHelper2.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper3 = new HashMap<>();
		Map<String, String> mapping1_3 = new HashMap<>();
		mapping1_3.put("y", "x");
		Map<String, String> mapping2_3 = new HashMap<>();
		mapping2_3.put("floors", "levels");

		expectedHelper3.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES, mapping2_3);
		expectedHelper3.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());



		expected.add(expectedHelper1);
		expected.add(expectedHelper2);
		expected.add(expectedHelper3);
	Assert.assertTrue(expected.containsAll(result));
	}


	@Test
	public void calculateMappings_test_3(){

		PersistentTransition floors = new PersistentTransition("floors", Collections.singletonMap("x", "4"),
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());



		OperationInfo operationInfo = new OperationInfo("floors", Arrays.asList("x", "y", "z"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("levels"),
				Collections.emptyList());

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = TraceExplorer.calculateVarMappings(floors, operationInfo);


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "y");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper2 = new HashMap<>();
		Map<String, String> mapping1_2 = new HashMap<>();
		mapping1_2.put("x", "z");
		Map<String, String> mapping2_2 = new HashMap<>();
		mapping2_2.put("floors", "levels");

		expectedHelper2.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_2);
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES, mapping2_2);
		expectedHelper2.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper3 = new HashMap<>();
		Map<String, String> mapping1_3 = new HashMap<>();
		mapping1_3.put("x", "x");
		Map<String, String> mapping2_3 = new HashMap<>();
		mapping2_3.put("floors", "levels");

		expectedHelper3.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES, mapping2_3);
		expectedHelper3.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());



		expected.add(expectedHelper1);
		expected.add(expectedHelper2);
		expected.add(expectedHelper3);


		Assert.assertTrue(expected.containsAll(result));
	}

	@Test
	public void createPersistentTransitionFromMapping_test_1(){

		PersistentTransition floors = new PersistentTransition("floors", Collections.singletonMap("y", "4"),
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "y");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), Collections.singletonMap("x", "4"),
				emptyMap(), Collections.singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void createPersistentTransitionFromMapping_test_2(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("y", "1");
			put("x", "0");
			put("z", "0");
		}},
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("y", "x");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), Collections.singletonMap("y", "0"),
				emptyMap(), Collections.singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assert.assertEquals(expected, result);
	}


	@Test
	public void createPersistentTransitionFromMapping_test_3(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "2");
			put("y", "1");
		}},
				emptyMap(), Collections.singletonMap("floors", "3"), Collections.emptySet(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("y", "x");
		mapping1_1.put("z", "y");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("y", "2");
			put("z", "1");
		}},
				emptyMap(), Collections.singletonMap("levels", "3"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assert.assertEquals(expected, result);
	}


	@Test
	public void createPersistentTransitionFromMapping_test_4(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "2");
			put("y", "1");
			put("z", "0");

		}},
				emptyMap(), Collections.singletonMap("floors", "3"), Collections.emptySet(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "z");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("x", "0");
		}},
				emptyMap(), Collections.singletonMap("levels", "3"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assert.assertEquals(expected, result);
	}


	@Test
	public void createPersistentTransitionFromMapping_test_5(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "1");
			put("y", "0");
			put("z", "0");

		}},
				emptyMap(), Collections.singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "y");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("x", "0");
		}},
				emptyMap(), Collections.singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assert.assertEquals(expected, result);
	}


	@Test
	public void generateAllPossibleMappingVariations_test() throws IOException, ModelTranslationError {
		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
			put("y","0");
			put("z","0");
		}},
				emptyMap(), Collections.singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition second = new PersistentTransition("dec", emptyMap(),
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift2.mch"));


		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> expected = new HashSet<>();

		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expectedHelper1_1 = new HashMap<>();

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_dec = new HashMap<>();

		Map<String, String> mapping_dec = new HashMap<>();
		mapping_dec.put("floors", "levels");

		expectedHelper_dec.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, emptyMap());
		expectedHelper_dec.put(TraceExplorer.MappingNames.VARIABLES, mapping_dec);
		expectedHelper_dec.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());




		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "y");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		expectedHelper1_1.put("inc", expectedHelper1);
		expectedHelper1_1.put("dec", expectedHelper_dec);



		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expectedHelper1_2 = new HashMap<>();
		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper2 = new HashMap<>();
		Map<String, String> mapping1_2 = new HashMap<>();
		mapping1_2.put("x", "z");
		Map<String, String> mapping2_2 = new HashMap<>();
		mapping2_2.put("floors", "levels");

		expectedHelper2.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_2);
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES, mapping2_2);
		expectedHelper2.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		expectedHelper1_2.put("inc", expectedHelper2);
		expectedHelper1_2.put("dec", expectedHelper_dec);



		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expectedHelper1_3 = new HashMap<>();
		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper3 = new HashMap<>();
		Map<String, String> mapping1_3 = new HashMap<>();
		mapping1_3.put("x", "x");
		Map<String, String> mapping2_3 = new HashMap<>();
		mapping2_3.put("floors", "levels");

		expectedHelper3.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES, mapping2_3);
		expectedHelper3.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		expectedHelper1_3.put("inc", expectedHelper3);
		expectedHelper1_3.put("dec", expectedHelper_dec);



		expected.add(expectedHelper1_3);
		expected.add(expectedHelper1_2);
		expected.add(expectedHelper1_1);

		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> result = TraceExplorer.generateAllPossibleMappingVariations(Arrays.asList(init, first, second), stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec").collect(Collectors.toSet()));

		Assert.assertEquals(expected, result);

	}
}
