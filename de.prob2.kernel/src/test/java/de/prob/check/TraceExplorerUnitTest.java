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

import static java.util.Arrays.asList;
import static java.util.Collections.*;

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
	public void product_mapping_test_1(){
		List<String> oldV = asList("a", "b", "c");
		List<String> newV = asList("x", "y", "z");

		Map<String, String> result1 = new HashMap<>();
		result1.put("a", "z");
		result1.put("b", "y");
		result1.put("c", "x");

		Map<String, String> result2 = new HashMap<>();
		result2.put("a", "y");
		result2.put("b", "x");
		result2.put("c", "z");

		Map<String, String> result3 = new HashMap<>();
		result3.put("a", "x");
		result3.put("b", "z");
		result3.put("c", "y");

		Map<String, String> result4 = new HashMap<>();
		result4.put("a", "x");
		result4.put("b", "y");
		result4.put("c", "z");

		Map<String, String> result5 = new HashMap<>();
		result5.put("a", "z");
		result5.put("b", "x");
		result5.put("c", "y");

		Map<String, String> result6 = new HashMap<>();
		result6.put("a", "y");
		result6.put("b", "z");
		result6.put("c", "x");

		Set<Map<String, String>> expected = new HashSet<>();
		expected.add(result1);
		expected.add(result2);
		expected.add(result3);
		expected.add(result4);
		expected.add(result5);
		expected.add(result6);

		Assert.assertEquals(expected, TraceExplorer.createAllPossiblePairs(oldV, newV));
	}


	@Test
	public void product_mapping_test_2(){
		List<String> oldV = asList("a", "b", "c");
		List<String> newV = asList("x", "y");

		Map<String, String> result1 = new HashMap<>();
		result1.put("b", "y");
		result1.put("c", "x");

		Map<String, String> result2 = new HashMap<>();
		result2.put("a", "y");
		result2.put("b", "x");

		Map<String, String> result3 = new HashMap<>();
		result3.put("a", "x");
		result3.put("c", "y");

		Map<String, String> result4 = new HashMap<>();
		result4.put("a", "x");
		result4.put("b", "y");

		Map<String, String> result5 = new HashMap<>();
		result5.put("b", "x");
		result5.put("c", "y");

		Map<String, String> result6 = new HashMap<>();
		result6.put("a", "y");
		result6.put("c", "x");

		Set<Map<String, String>> expected = new HashSet<>();
		expected.add(result1);
		expected.add(result2);
		expected.add(result3);
		expected.add(result4);
		expected.add(result5);
		expected.add(result6);

		Assert.assertEquals(expected, TraceExplorer.createAllPossiblePairs(oldV, newV));
	}

	@Test
	public void product_mapping_test_3(){
		List<String> oldV = asList("a", "b");
		List<String> newV = asList("x", "y", "z");

		Map<String, String> result1 = new HashMap<>();
		result1.put("a", "z");
		result1.put("b", "y");

		Map<String, String> result2 = new HashMap<>();
		result2.put("a", "y");
		result2.put("b", "x");

		Map<String, String> result3 = new HashMap<>();
		result3.put("a", "x");
		result3.put("b", "z");

		Map<String, String> result4 = new HashMap<>();
		result4.put("a", "x");
		result4.put("b", "y");

		Map<String, String> result5 = new HashMap<>();
		result5.put("a", "z");
		result5.put("b", "x");

		Map<String, String> result6 = new HashMap<>();
		result6.put("a", "y");
		result6.put("b", "z");

		Set<Map<String, String>> expected = new HashSet<>();
		expected.add(result1);
		expected.add(result2);
		expected.add(result3);
		expected.add(result4);
		expected.add(result5);
		expected.add(result6);

		Assert.assertEquals(expected, TraceExplorer.createAllPossiblePairs(oldV, newV));
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


		OperationInfo operationInfo = new OperationInfo("floors", Collections.singletonList("a"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("levels"),
				Collections.emptyList());

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = TraceExplorer.calculateVarMappings(floors, operationInfo);


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("y", "a");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper2 = new HashMap<>();
		Map<String, String> mapping1_2 = new HashMap<>();
		mapping1_2.put("z", "a");
		Map<String, String> mapping2_2 = new HashMap<>();
		mapping2_2.put("floors", "levels");

		expectedHelper2.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_2);
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES, mapping2_2);
		expectedHelper2.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper3 = new HashMap<>();
		Map<String, String> mapping1_3 = new HashMap<>();
		mapping1_3.put("x", "a");
		Map<String, String> mapping2_3 = new HashMap<>();
		mapping2_3.put("floors", "levels");

		expectedHelper3.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES, mapping2_3);
		expectedHelper3.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());



		expected.add(expectedHelper1);
		expected.add(expectedHelper2);
		expected.add(expectedHelper3);

		System.out.println(expected);
		System.out.println(result);

     	Assert.assertTrue(expected.containsAll(result));
	}


	@Test
	public void calculateMappings_test_3(){

		PersistentTransition floors = new PersistentTransition("floors", Collections.singletonMap("x", "4"),
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());



		OperationInfo operationInfo = new OperationInfo("floors", asList("x", "y", "z"),
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
	public void calculateMappings_test_4_removed_output_and_unchanged_state(){

		PersistentTransition floors = new PersistentTransition("getfloors", emptyMap(),
				Collections.singletonMap("out", "0"), emptyMap(), singleton("floors"), emptyList());


		OperationInfo operationInfo = new OperationInfo("getlevels", emptyList(), emptyList(),true,
				OperationInfo.Type.CLASSICAL_B, singletonList("levels"), emptyList(), emptyList());


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, emptyMap());
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		expected.add(expectedHelper1);

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = TraceExplorer.calculateVarMappings(floors, operationInfo);

		System.out.println(expected);
		System.out.println(result);
		Assert.assertTrue(expected.containsAll(result));
	}

	@Test
	public void calculateMappings_test_5_added_output_and_unchanged_state(){

		PersistentTransition floors = new PersistentTransition("getfloors", emptyMap(),
				emptyMap(), emptyMap(), singleton("floors"), emptyList());


		OperationInfo operationInfo = new OperationInfo("getlevels", emptyList(), singletonList("out"),true,
				OperationInfo.Type.CLASSICAL_B, singletonList("levels"), emptyList(), emptyList());


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, emptyMap());
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		expected.add(expectedHelper1);

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = TraceExplorer.calculateVarMappings(floors, operationInfo);

		System.out.println(expected);
		System.out.println(result);
		Assert.assertTrue(expected.containsAll(result));
	}


	@Test
	public void createPersistentTransitionFromMapping_test_1(){

		PersistentTransition floors = new PersistentTransition("floors", Collections.singletonMap("x", "4"),
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "y");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), Collections.singletonMap("y", "4"),
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
		mapping1_1.put("x", "a");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), Collections.singletonMap("a", "0"),
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
		mapping1_1.put("x", "a");
		mapping1_1.put("y", "b");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "2");
			put("b", "1");
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
		mapping1_1.put("z", "a");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "0");
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
		mapping1_1.put("y", "a");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "0");
		}},
				emptyMap(), Collections.singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assert.assertEquals(expected, result);
	}


	@Test
	public void createPersistentTransitionFromMapping_test_7(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "2");
			put("y", "1");
		}},
				emptyMap(), singletonMap("out", "4"), singleton("floors"), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "a");
		mapping1_1.put("y", "b");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");
		Map<String, String> mapping3_1 = new HashMap<>();
		mapping2_1.put("out", "outter");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, mapping3_1);

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "2");
			put("b", "1");
		}},
				emptyMap(), singletonMap("outter", "4"), singleton("levels"), emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void createPersistentTransitionFromMapping_test_6(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "2");
			put("y", "1");
		}},
				emptyMap(), emptyMap(), singleton("floors"), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "a");
		mapping1_1.put("y", "b");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "2");
			put("b", "1");
		}},
				emptyMap(), emptyMap(), singleton("levels"), emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assert.assertEquals(expected, result);
	}



	@Test
	public void generateAllPossibleMappingVariations_test() throws IOException, ModelTranslationError {
		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("a","1");
			put("b","0");
			put("c","0");
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
		mapping1_1.put("a", "x");
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
		mapping1_2.put("b", "x");
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
		mapping1_3.put("c", "x");
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

		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> result = TraceExplorer.generateAllPossibleMappingVariations(asList(init, first, second), stateSpace.getLoadedMachine().getOperations(), Stream.of("inc", "dec").collect(Collectors.toSet()));

		Assert.assertEquals(expected, result);

	}


	@Test
	public void generateAllPossibleMappingVariations_empty_typeIII_test() throws IOException, ModelTranslationError {
		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, emptyMap(),
				emptyMap(), Collections.singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", new HashMap<String, String>() {{
			put("x","1");
		}},
				emptyMap(), Collections.singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition second = new PersistentTransition("dec", emptyMap(),
				emptyMap(), Collections.singletonMap("leves", "0"), Collections.emptySet(), Collections.emptyList());

		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift2.mch"));


		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> result =
				TraceExplorer.generateAllPossibleMappingVariations(asList(init, first, second), stateSpace.getLoadedMachine().getOperations(), emptySet());

		Assert.assertEquals(singleton(emptyMap()), result);

	}


	@Test
	public void removeEntriesWithEmptyValues_test_return_all(){
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

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result = TraceExplorer.removeEntriesWithEmptyValues(expected);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void removeEntriesWithEmptyValues_test_return_empty_map(){
		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> prepared = new HashMap<>();


		Map<String, String> preparedHelper_1 = new HashMap<>();
		preparedHelper_1.put("floors", "levels");
		preparedHelper_1.put("a", "x");

		Map<String, String> preparedHelper_2 = new HashMap<>();
		preparedHelper_2.put("floors", "levels");
		preparedHelper_2.put("a", "y");


		Map<String, Map<String, String>> prepared_inner1 = new HashMap<>();
		prepared_inner1.put("inc", preparedHelper_1);

		Map<String, Map<String, String>> prepared_inner2 = new HashMap<>();
		prepared_inner2.put("inc", preparedHelper_2);


		prepared.put(prepared_inner1,  emptyList());
		prepared.put(prepared_inner2,  emptyList());



		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result = TraceExplorer.removeEntriesWithEmptyValues(prepared);


		Assert.assertEquals(emptyMap(), result);

	}

	@Test
	public void removeEntriesWithEmptyValues_test_return_clean_one(){
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



		PersistentTransition secondNew = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());


		//The explorer will find multiple possible solutions, we expect at least 3, because we want to test if the mapping from
		//old to new parameters is working properly

		PersistenceDelta delta1 = new PersistenceDelta(init, Collections.singletonList(initNew));
		PersistenceDelta delta2_var1 = new PersistenceDelta(first, Collections.singletonList(firstNew_var1));

		PersistenceDelta delta3 = new PersistenceDelta(second, Collections.singletonList(secondNew));


		List<PersistenceDelta> prepared1 = Arrays.asList(delta1,delta2_var1,delta3);


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> prepared = new HashMap<>();


		Map<String, String> preparedHelper_1 = new HashMap<>();
		preparedHelper_1.put("floors", "levels");
		preparedHelper_1.put("a", "x");

		Map<String, String> preparedHelper_2 = new HashMap<>();
		preparedHelper_2.put("floors", "levels");
		preparedHelper_2.put("a", "y");


		Map<String, Map<String, String>> prepared_inner1 = new HashMap<>();
		prepared_inner1.put("inc", preparedHelper_1);

		Map<String, Map<String, String>> prepared_inner2 = new HashMap<>();
		prepared_inner2.put("inc", preparedHelper_2);


		prepared.put(prepared_inner1,  prepared1);
		prepared.put(prepared_inner2,  emptyList());


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> expected = new HashMap<>();


		Map<String, String> expectedHelper = new HashMap<>();
		expectedHelper.put("floors", "levels");
		expectedHelper.put("a", "x");



		Map<String, Map<String, String>> expected_inner = new HashMap<>();
		expected_inner.put("inc", expectedHelper);


		expected.put(expected_inner,  prepared1);



		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result = TraceExplorer.removeEntriesWithEmptyValues(prepared);


		Assert.assertEquals(expected, result);
	}
}
