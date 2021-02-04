package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.check.tracereplay.check.exceptions.MappingFactoryInterface;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
	public void sortByValue_test_two_data_types(){
		Map<String, String> input = new HashMap<>();
		input.put("a", "integer");
		input.put("b", "integer");
		input.put("c", "integer");
		input.put("d", "boolean");
		input.put("e", "boolean");
		input.put("f", "boolean");

		Map<String, List<String>> expected = new HashMap<>();
		expected.put("integer", Arrays.asList("a","b","c"));
		expected.put("boolean", Arrays.asList("d","e","f"));

		Map<String, List<String>> result = TraceExplorer.sortByValue(input);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void permutedMap_test_empty(){
		Map<String, List<String>> input = new HashMap<>();

		List<String> listA = new ArrayList<>(asList("a", "b", "c"));
		List<String> listB = new ArrayList<>(asList("d", "e", "f"));

		input.put("integer", new ArrayList<>(listA));
		input.put("boolean", new ArrayList<>(listB));

		Map<String, Integer> inputLength = new HashMap<>();
		inputLength.put("integer", 3);
		inputLength.put("boolean", 3);


		Set<List<String>> permA = new HashSet<>(TraceCheckerUtils.generatePerm(listA));
		Set<List<String>> permB = new HashSet<>(TraceCheckerUtils.generatePerm(listB));

		Map<String, Set<List<String>>> expected = new HashMap<>();
		expected.put("integer", permA);
		expected.put("boolean", permB);

		Map<String, Set<List<String>>> result = TraceExplorer.permutedMap(input, inputLength)
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> new HashSet<>(entry.getValue())));

		Assert.assertEquals(expected, result);
	}

	@Test
	public void productCombination_test(){

		List<String> listA = new ArrayList<>(asList("a", "b", "c"));
		List<String> listB = new ArrayList<>(asList("x", "y", "z"));

		List<List<String>> permA = TraceCheckerUtils.generatePerm(listA);
		List<List<String>> permB = TraceCheckerUtils.generatePerm(listB);


		Set<Map<String, String>> result = TraceExplorer.productCombination(permA, permB);


		Map<String, String> map1 = new HashMap<>();
		map1.put("a", "z");
		map1.put("b", "y");
		map1.put("c", "x");
		Map<String, String> map2 = new HashMap<>();
		map2.put("a", "y");
		map2.put("b", "x");
		map2.put("c", "z");
		Map<String, String> map3 = new HashMap<>();
		map3.put("a", "y");
		map3.put("b", "z");
		map3.put("c", "x");
		Map<String, String> map4 = new HashMap<>();
		map4.put("a", "x");
		map4.put("b", "z");
		map4.put("c", "y");
		Map<String, String> map5 = new HashMap<>();
		map5.put("a", "x");
		map5.put("b", "y");
		map5.put("c", "z");
		Map<String, String> map6 = new HashMap<>();
		map6.put("a", "z");
		map6.put("b", "x");
		map6.put("c", "y");

		Set<Map<String, String>> expected = new HashSet<>(Arrays.asList(map1, map2, map3, map4, map5, map6));

		Assert.assertEquals(expected, result);
	}

	@Test
	public void melt_test(){
		List<List<String>> bool1 = Arrays.asList(Arrays.asList("b","a"), Arrays.asList("a", "b"));
		List<List<String>> bool2 = Arrays.asList(Arrays.asList("x","y"), Arrays.asList("y", "x"));
		List<List<String>> integer1 = Arrays.asList(Arrays.asList("n","m"), Arrays.asList("m", "n"));
		List<List<String>> integer2 = Arrays.asList(Arrays.asList("i","j"), Arrays.asList("j", "i"));

		Map<String, List<List<String>>> first = new HashMap<>();
		first.put("boolean", bool1);
		first.put("integer", integer1);


		Map<String, List<List<String>>> second = new HashMap<>();
		second.put("boolean", bool2);
		second.put("integer", integer2);

		Map<String, String> map1 = new HashMap<>();
		map1.put("a", "y");
		map1.put("b", "x");
		Map<String, String> map2 = new HashMap<>();
		map2.put("a", "x");
		map2.put("b", "y");
		Map<String, String> map3 = new HashMap<>();
		map3.put("m", "i");
		map3.put("n", "j");
		Map<String, String> map4 = new HashMap<>();
		map4.put("m", "j");
		map4.put("n", "i");

		Map<String, Set<Map<String, String>>> expected = new HashMap<>();
		expected.put("boolean", new HashSet<>(Arrays.asList(map1, map2)));
		expected.put("integer", new HashSet<>(Arrays.asList(map3, map4)));



		Map<String, Set<Map<String, String>>> result = TraceExplorer.melt(first, second);


		Assertions.assertEquals(expected,result);
	}
	
	@Test
	public void applyProduct_test(){
		Map<String, String> map1 = new HashMap<>();
		map1.put("a", "y");
		map1.put("b", "x");
		Map<String, String> map2 = new HashMap<>();
		map2.put("a", "x");
		map2.put("b", "y");
		Map<String, String> map3 = new HashMap<>();
		map3.put("m", "i");
		map3.put("n", "j");
		Map<String, String> map4 = new HashMap<>();
		map4.put("m", "j");
		map4.put("n", "i");

		HashSet<Map<String, String>> first = new HashSet<>(asList(map1, map2));
		HashSet<Map<String, String>> second = new HashSet<>(Arrays.asList(map3, map4));


		Map<String, String> map1Expected = new HashMap<>();
		map1Expected.put("a", "x");
		map1Expected.put("b", "y");
		map1Expected.put("m", "i");
		map1Expected.put("n", "j");
		Map<String, String> map2Expected = new HashMap<>();
		map2Expected.put("a", "x");
		map2Expected.put("b", "y");
		map2Expected.put("m", "j");
		map2Expected.put("n", "i");
		Map<String, String> map3Expected = new HashMap<>();
		map3Expected.put("a", "y");
		map3Expected.put("b", "x");
		map3Expected.put("m", "i");
		map3Expected.put("n", "j");
		Map<String, String> map4Expected = new HashMap<>();
		map4Expected.put("a", "y");
		map4Expected.put("b", "x");
		map4Expected.put("m", "j");
		map4Expected.put("n", "i");

		Set<Map<String, String>> expected = new HashSet<>(asList(map1Expected, map2Expected, map3Expected, map4Expected));

		Set<Map<String, String>> result = TraceExplorer.applyProduct(first, second);

		Assert.assertEquals(expected, result);

	}

	@Test
	public void reduceSet_test(){
		Map<String, String> map1 = new HashMap<>();
		map1.put("a", "y");
		map1.put("b", "x");
		Map<String, String> map2 = new HashMap<>();
		map2.put("a", "x");
		map2.put("b", "y");
		Map<String, String> map3 = new HashMap<>();
		map3.put("m", "i");
		map3.put("n", "j");
		Map<String, String> map4 = new HashMap<>();
		map4.put("m", "j");
		map4.put("n", "i");

		HashSet<Map<String, String>> first = new HashSet<>(asList(map1, map2));
		HashMap<String, Set<Map<String, String>>> ready = new HashMap<>();
		ready.put("integer", first);
		HashSet<Map<String, String>> second = new HashSet<>(Arrays.asList(map3, map4));
		ready.put("boolean", second);

		Map<String, String> map1Expected = new HashMap<>();
		map1Expected.put("a", "x");
		map1Expected.put("b", "y");
		map1Expected.put("m", "i");
		map1Expected.put("n", "j");
		Map<String, String> map2Expected = new HashMap<>();
		map2Expected.put("a", "x");
		map2Expected.put("b", "y");
		map2Expected.put("m", "j");
		map2Expected.put("n", "i");
		Map<String, String> map3Expected = new HashMap<>();
		map3Expected.put("a", "y");
		map3Expected.put("b", "x");
		map3Expected.put("m", "i");
		map3Expected.put("n", "j");
		Map<String, String> map4Expected = new HashMap<>();
		map4Expected.put("a", "y");
		map4Expected.put("b", "x");
		map4Expected.put("m", "j");
		map4Expected.put("n", "i");

		Set<Map<String, String>> expected = new HashSet<>(asList(map1Expected, map2Expected, map3Expected, map4Expected));

		Set<Map<String, String>> result = TraceExplorer.reduceSet(ready);

		Assert.assertEquals(expected, result);

	}

	@Test
	public void cleanse_test(){
		Map<String, List<String>> input = new HashMap<>();

		List<String> listA = new ArrayList<>(asList("a", "b", "c"));
		List<String> listB = new ArrayList<>(asList("d", "e", "f"));

		input.put("integer", new ArrayList<>(listA));
		input.put("boolean", new ArrayList<>(listB));

		Map<String, List<String>> expected = new HashMap<>();

		expected.put("boolean", new ArrayList<>(listB));

		Map<String, List<String>> result = TraceExplorer.cleanse(input, singleton("boolean"));

		Assert.assertEquals(expected, result);
	}


	@Test
	public void trigger_dialog_test(){
		

		Map<String, String> oldMapping = new HashMap<>();
		oldMapping.put("a", "integer");
		oldMapping.put("b", "integer");
		oldMapping.put("c", "integer");
		oldMapping.put("d", "integer");
		oldMapping.put("e", "integer");
		oldMapping.put("f", "integer");
		oldMapping.put("g", "integer");
		oldMapping.put("h", "integer");
		oldMapping.put("i", "integer");
		oldMapping.put("j", "integer");
		

		Map<String, String> newMapping = new HashMap<>();
		newMapping.put("a", "integer");
		newMapping.put("b", "integer");
		newMapping.put("c", "integer");
		newMapping.put("d", "integer");
		newMapping.put("e", "integer");
		newMapping.put("f", "integer");
		newMapping.put("g", "integer");
		newMapping.put("h", "integer");
		newMapping.put("i", "integer");
		newMapping.put("j", "integer");

		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();
		Set<Map<String, String>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).createAllPossiblePairs(newMapping, oldMapping, TraceExplorer.MappingNames.VARIABLES_MODIFIED, "inc");

		Map<String, String> inner = TraceCheckerUtils.zip(new ArrayList<>(oldMapping.keySet()), new ArrayList<>(newMapping.keySet()));
		Set<Map<String, String>> expected = new HashSet<>();
		expected.add(inner);
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void createAllPossiblePairs_test_1(){

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

		Map<String, String> oldMapping = new HashMap<>();
		oldMapping.put("a", "integer");
		oldMapping.put("b", "integer");
		oldMapping.put("c", "integer");

		Map<String, String> newMapping = new HashMap<>();
		newMapping.put("x", "integer");
		newMapping.put("y", "integer");
		newMapping.put("z", "integer");

		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();
		Set<Map<String, String>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).createAllPossiblePairs(newMapping, oldMapping, TraceExplorer.MappingNames.VARIABLES_MODIFIED, "inc");

		Assert.assertEquals(expected, result);
	}

	@Test
	public void createAllPossiblePairs_test_2(){

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

		Map<String, String> oldMapping = new HashMap<>();
		oldMapping.put("a", "integer");
		oldMapping.put("b", "integer");
		oldMapping.put("c", "integer");

		Map<String, String> newMapping = new HashMap<>();
		newMapping.put("x", "integer");
		newMapping.put("y", "integer");

		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();
		Set<Map<String, String>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).createAllPossiblePairs(newMapping, oldMapping, TraceExplorer.MappingNames.VARIABLES_MODIFIED, "");

		Assert.assertEquals(expected, result);
	}

	@Test
	public void createAllPossiblePairs_test_3(){

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

		Map<String, String> oldMapping = new HashMap<>();
		oldMapping.put("a", "integer");
		oldMapping.put("b", "integer");

		Map<String, String> newMapping = new HashMap<>();
		newMapping.put("x", "integer");
		newMapping.put("y", "integer");
		newMapping.put("z", "integer");

		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();
		Set<Map<String, String>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).createAllPossiblePairs(newMapping, oldMapping, TraceExplorer.MappingNames.VARIABLES_MODIFIED, "");

		Assert.assertEquals(expected, result);
	}

	@Test
	public void createAllPossiblePairs_test_4(){

		Map<String, String> result3 = new HashMap<>();
		result3.put("a", "x");
		result3.put("b", "z");

		Map<String, String> result6 = new HashMap<>();
		result6.put("a", "y");
		result6.put("b", "z");

		Set<Map<String, String>> expected = new HashSet<>();
		expected.add(result3);
		expected.add(result6);

		Map<String, String> oldMapping = new HashMap<>();
		oldMapping.put("a", "integer");
		oldMapping.put("b", "boolean");

		Map<String, String> newMapping = new HashMap<>();
		newMapping.put("x", "integer");
		newMapping.put("y", "integer");
		newMapping.put("z", "boolean");

		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();
		Set<Map<String, String>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).createAllPossiblePairs(newMapping, oldMapping, TraceExplorer.MappingNames.VARIABLES_MODIFIED, "");


		Assert.assertEquals(expected, result);
	}

	@Test
	public void createAllPossiblePairs_test_5(){

		Map<String, String> result4 = new HashMap<>();
		result4.put("a", "x");
		result4.put("b", "y");


		Set<Map<String, String>> expected = new HashSet<>();

		expected.add(result4);

		Map<String, String> oldMapping = new HashMap<>();
		oldMapping.put("a", "integer");
		oldMapping.put("b", "boolean");
		oldMapping.put("c", "POW(integer)");

		Map<String, String> newMapping = new HashMap<>();
		newMapping.put("x", "integer");
		newMapping.put("y", "boolean");

		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();
		Set<Map<String, String>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).createAllPossiblePairs(newMapping, oldMapping, TraceExplorer.MappingNames.VARIABLES_MODIFIED, "");


		Assert.assertEquals(expected, result);
	}

	@Test
	public void calculateMappings_test_1(){

		Map<String, String> varMapping1 = new HashMap<>();
		varMapping1.put("floors", "integer");
		varMapping1.put("x", "integer");

		OperationInfo operationInfoOld = new OperationInfo("floors", Collections.singletonList("x"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("floors"),
				Collections.emptyList(), varMapping1);

		Map<String, String> varMapping2 = new HashMap<>();
		varMapping2.put("levels", "integer");
		varMapping2.put("y", "integer");


		OperationInfo operationInfoNew = new OperationInfo("floors", Collections.singletonList("y"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("levels"),
				Collections.emptyList(), varMapping2);



		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();



		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory())
				.calculateVarMappings("floors", operationInfoNew, operationInfoOld);


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper = new HashMap<>();
		Map<String, String> mapping1 = new HashMap<>();
		mapping1.put("x", "y");
		Map<String, String> mapping2 = new HashMap<>();
		mapping2.put("floors", "levels");

		expectedHelper.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1);
		expectedHelper.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2);
		expectedHelper.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());
		expectedHelper.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		expected.add(expectedHelper);
		Assert.assertTrue(expected.containsAll(result));
	}

	@Test
	public void calculateMappings_test_2(){

		Map<String, String> varMapping1 = new HashMap<>();
		varMapping1.put("floors", "integer");
		varMapping1.put("x", "integer");
		varMapping1.put("y", "integer");
		varMapping1.put("z", "integer");

		OperationInfo operationInfoOld = new OperationInfo("floors", Arrays.asList("x","y","z"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("floors"),
				Collections.emptyList(), varMapping1);

		Map<String, String> varMapping2 = new HashMap<>();
		varMapping2.put("levels", "integer");
		varMapping2.put("a", "integer");


		OperationInfo operationInfo = new OperationInfo("floors", Collections.singletonList("a"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("levels"),
				Collections.emptyList(), varMapping2);


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("y", "a");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());
		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper2 = new HashMap<>();
		Map<String, String> mapping1_2 = new HashMap<>();
		mapping1_2.put("z", "a");
		Map<String, String> mapping2_2 = new HashMap<>();
		mapping2_2.put("floors", "levels");

		expectedHelper2.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_2);
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_2);
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());
		expectedHelper2.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper3 = new HashMap<>();
		Map<String, String> mapping1_3 = new HashMap<>();
		mapping1_3.put("x", "a");
		Map<String, String> mapping2_3 = new HashMap<>();
		mapping2_3.put("floors", "levels");

		expectedHelper3.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());
		expectedHelper3.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

		expected.add(expectedHelper1);
		expected.add(expectedHelper2);
		expected.add(expectedHelper3);


		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).calculateVarMappings("floors",  operationInfo, operationInfoOld);


		Assert.assertTrue(expected.containsAll(result));
	}

	@Test
	public void calculateMappings_test_3(){

		Map<String, String> varMapping1 = new HashMap<>();
		varMapping1.put("floors", "integer");
		varMapping1.put("x", "integer");

		OperationInfo operationInfoOld = new OperationInfo("floors", Collections.singletonList("x"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("floors"),
				Collections.emptyList(), varMapping1);

		Map<String, String> varMapping2 = new HashMap<>();
		varMapping2.put("levels", "integer");
		varMapping2.put("x", "integer");
		varMapping2.put("y", "integer");
		varMapping2.put("z", "integer");


		OperationInfo operationInfo = new OperationInfo("floors", asList("x", "y", "z"),
				Collections.emptyList(),true, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(), Collections.singletonList("levels"),
				Collections.emptyList(), varMapping2);


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("x", "y");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper2 = new HashMap<>();
		Map<String, String> mapping1_2 = new HashMap<>();
		mapping1_2.put("x", "z");
		Map<String, String> mapping2_2 = new HashMap<>();
		mapping2_2.put("floors", "levels");

		expectedHelper2.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_2);
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_2);
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

		expectedHelper2.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper3 = new HashMap<>();
		Map<String, String> mapping1_3 = new HashMap<>();
		mapping1_3.put("x", "x");
		Map<String, String> mapping2_3 = new HashMap<>();
		mapping2_3.put("floors", "levels");

		expectedHelper3.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

		expectedHelper3.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		expected.add(expectedHelper1);
		expected.add(expectedHelper2);
		expected.add(expectedHelper3);


		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).calculateVarMappings("floors", operationInfo, operationInfoOld);


		Assert.assertTrue(expected.containsAll(result));
	}

	@Test
	public void calculateMappings_test_4_removed_output_and_unchanged_state(){

		Map<String, String> varMapping1 = new HashMap<>();
		varMapping1.put("floors", "integer");
		varMapping1.put("out", "integer");

		OperationInfo operationInfoOld = new OperationInfo("floors", emptyList(),
				singletonList("out"),true, OperationInfo.Type.CLASSICAL_B, Collections.singletonList("floors"), Collections.emptyList(),
				Collections.emptyList(), varMapping1);

		Map<String, String> varMapping2 = new HashMap<>();
		varMapping2.put("levels", "integer");


		OperationInfo operationInfo = new OperationInfo("getlevels", emptyList(), emptyList(),true,
				OperationInfo.Type.CLASSICAL_B, singletonList("levels"), emptyList(), emptyList(), varMapping2);


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, emptyMap());
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, emptyMap());
		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		expected.add(expectedHelper1);


		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).calculateVarMappings("getfloors", operationInfo, operationInfoOld);


		Assert.assertTrue(expected.containsAll(result));
	}

	@Test
	public void calculateMappings_test_5_added_output_and_unchanged_state(){

		Map<String, String> varMapping1 = new HashMap<>();
		varMapping1.put("floors", "integer");

		OperationInfo operationInfoOld = new OperationInfo("floors", emptyList(), emptyList(), true,
				OperationInfo.Type.CLASSICAL_B, Collections.singletonList("floors"), Collections.emptyList(),
				Collections.emptyList(), varMapping1);

		Map<String, String> varMapping2 = new HashMap<>();
		varMapping2.put("levels", "integer");
		varMapping2.put("out", "integer");


		OperationInfo operationInfo = new OperationInfo("getlevels", emptyList(), singletonList("out"),true,
				OperationInfo.Type.CLASSICAL_B, singletonList("levels"), emptyList(), emptyList(), varMapping2);


		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> expected = new HashSet<>();


		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, emptyMap());
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED
				, emptyMap());

		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		expected.add(expectedHelper1);

		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();

		Set<Map<TraceExplorer.MappingNames, Map<String, String>>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).calculateVarMappings("floors", operationInfo, operationInfoOld);

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
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

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
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

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
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

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
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, emptyMap());

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
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_1);

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
		mapping3_1.put("out", "outter");

		mappingHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping3_1);
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_1);
		mappingHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());

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
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, emptyMap());
		mappingHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_1);
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

		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "reducedSigLengthAndRenamedVariable",  "OneTypeIIICandidateCounterPart.mch"));
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();

		StateSpace stateSpace2 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "reducedSigLengthAndRenamedVariable", "OneTypeIIICandidate.mch"));
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();


		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> expected = new HashSet<>();

		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expectedHelper1_1 = new HashMap<>();

		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper_dec = new HashMap<>();

		Map<String, String> mapping_dec = new HashMap<>();
		mapping_dec.put("floors", "levels");

		expectedHelper_dec.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, emptyMap());
		expectedHelper_dec.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());
		expectedHelper_dec.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping_dec);
		expectedHelper_dec.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping_dec);




		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper1 = new HashMap<>();
		Map<String, String> mapping1_1 = new HashMap<>();
		mapping1_1.put("a", "x");
		Map<String, String> mapping2_1 = new HashMap<>();
		mapping2_1.put("floors", "levels");

		expectedHelper1.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_1);
		expectedHelper1.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_1);
		expectedHelper1.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_1);

		expectedHelper1_1.put("inc", expectedHelper1);
		expectedHelper1_1.put("dec", expectedHelper_dec);



		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expectedHelper1_2 = new HashMap<>();
		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper2 = new HashMap<>();
		Map<String, String> mapping1_2 = new HashMap<>();
		mapping1_2.put("b", "x");
		Map<String, String> mapping2_2 = new HashMap<>();
		mapping2_2.put("floors", "levels");

		expectedHelper2.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_2);
		expectedHelper2.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_2);
		expectedHelper2.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_2);

		expectedHelper1_2.put("inc", expectedHelper2);
		expectedHelper1_2.put("dec", expectedHelper_dec);



		Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>> expectedHelper1_3 = new HashMap<>();
		Map<TraceExplorer.MappingNames, Map<String, String>> expectedHelper3 = new HashMap<>();
		Map<String, String> mapping1_3 = new HashMap<>();
		mapping1_3.put("c", "x");
		Map<String, String> mapping2_3 = new HashMap<>();
		mapping2_3.put("floors", "levels");

		expectedHelper3.put(TraceExplorer.MappingNames.INPUT_PARAMETERS, mapping1_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES_MODIFIED, mapping2_3);
		expectedHelper3.put(TraceExplorer.MappingNames.VARIABLES_READ, mapping2_3);
		expectedHelper3.put(TraceExplorer.MappingNames.OUTPUT_PARAMETERS, emptyMap());


		expectedHelper1_3.put("inc", expectedHelper3);
		expectedHelper1_3.put("dec", expectedHelper_dec);



		expected.add(expectedHelper1_3);
		expected.add(expectedHelper1_2);
		expected.add(expectedHelper1_1);



		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();

		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> result = new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory())
				.generateAllPossibleMappingVariations(asList(init, first, second), newInfos, oldInfos, new HashSet<>(Arrays.asList("inc", "dec")));

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


		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "reducedSigLengthAndRenamedVariable",  "OneTypeIIICandidateCounterPart.mch"));
		Map<String, OperationInfo> newInfos = stateSpace1.getLoadedMachine().getOperations();


		StateSpace stateSpace2 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "reducedSigLengthAndRenamedVariable", "OneTypeIIICandidate.mch"));
		Map<String, OperationInfo> oldInfos = stateSpace2.getLoadedMachine().getOperations();



		MappingFactoryInterface mappingFactoryInterface = new TestUtils.StubFactoryImplementation();


		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> result =
				new TraceExplorer(true, mappingFactoryInterface, new TestUtils.ProgressStubFactory()).generateAllPossibleMappingVariations(asList(init, first, second), newInfos, oldInfos, emptySet());

		Assert.assertEquals(singleton(emptyMap()), result);

	}



}
