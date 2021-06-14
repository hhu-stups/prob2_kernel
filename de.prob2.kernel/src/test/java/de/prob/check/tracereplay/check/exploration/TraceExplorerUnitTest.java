package de.prob.check.tracereplay.check.exploration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.cli.CliTestCommon;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.util.Collections.*;

public class TraceExplorerUnitTest {
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
	public void createPersistentTransitionFromMapping_test_1(){

		PersistentTransition floors = new PersistentTransition("floors", Collections.singletonMap("x", "4"),
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "y");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), Collections.singletonMap("y", "4"),
				emptyMap(), Collections.singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assertions.assertEquals(expected, result);
	}

	@Test
	public void createPersistentTransitionFromMapping_test_2(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("y", "1");
			put("x", "0");
			put("z", "0");
		}},
				emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "a");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), Collections.singletonMap("a", "0"),
				emptyMap(), Collections.singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assertions.assertEquals(expected, result);
	}

	@Test
	public void createPersistentTransitionFromMapping_test_3(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "2");
			put("y", "1");
		}},
				emptyMap(), Collections.singletonMap("floors", "3"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "a");
		mapping1_1.put("y", "b");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "2");
			put("b", "1");
		}},
				emptyMap(), Collections.singletonMap("levels", "3"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assertions.assertEquals(expected, result);
	}

	@Test
	public void createPersistentTransitionFromMapping_test_4(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "2");
			put("y", "1");
			put("z", "0");

		}},
				emptyMap(), Collections.singletonMap("floors", "3"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("z", "a");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "0");
		}},
				emptyMap(), Collections.singletonMap("levels", "3"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assertions.assertEquals(expected, result);
	}

	@Test
	public void createPersistentTransitionFromMapping_test_5(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "1");
			put("y", "0");
			put("z", "0");

		}},
				emptyMap(), Collections.singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("y", "a");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_1);

		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "0");
		}},
				emptyMap(), Collections.singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList(), Collections.emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assertions.assertEquals(expected, result);
	}

	@Test
	public void createPersistentTransitionFromMapping_test_7(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "2");
			put("y", "1");
		}},
				emptyMap(), singletonMap("out", "4"), singleton("floors"), Collections.emptyList(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "a");
		mapping1_1.put("y", "b");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");
		Map<String, String> mapping3_1 = new HashMap<>();
		mapping3_1.put("out", "outter");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping3_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "2");
			put("b", "1");
		}},
				emptyMap(), singletonMap("outter", "4"), singleton("levels"), emptyList(), emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assertions.assertEquals(expected, result);
	}

	@Test
	public void createPersistentTransitionFromMapping_test_6(){

		PersistentTransition floors = new PersistentTransition("floors", new HashMap<String, String>() {{
			put("x", "2");
			put("y", "1");
		}},
				emptyMap(), emptyMap(), singleton("floors"), Collections.emptyList(), Collections.emptyList());



		Map<TraceExplorer.MappingNames, Map<String, String>> mappingHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "a");
		mapping1_1.put("y", "b");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, emptyMap());
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		PersistentTransition expected = new PersistentTransition(floors.getOperationName(), new HashMap<String, String>() {{
			put("a", "2");
			put("b", "1");
		}},
				emptyMap(), emptyMap(), singleton("levels"), emptyList(), emptyList());

		PersistentTransition result = TraceExplorer.createPersistentTransitionFromMapping(mappingHelper1, floors);

		Assertions.assertEquals(expected, result);
	}





}
