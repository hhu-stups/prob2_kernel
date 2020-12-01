package de.prob.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.TraceExplorer;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TraceExplorerUnitTest {


	@Test
	public void variationsFinder_test_1(){
		PersistentTransition first = new PersistentTransition("inc", Collections.singletonMap("x", "1"),
				Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), Collections.emptyList());

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
				Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), Collections.emptyList());

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
				Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), Collections.emptyList());

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
				Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), Collections.emptyList());

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
				Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), Collections.emptyList());

		Set<Map<String, String>> result = TraceExplorer.possibleConstellations(first.getParameters(), Collections.emptyList());



		Assert.assertTrue( result.isEmpty());

	}

	@Test
	public void variationsFinder_test_6(){

		Map<String, String> parameter = new HashMap<>();
		parameter.put("x", "1");

		PersistentTransition first = new PersistentTransition("inc", parameter,
				Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), Collections.emptyList());

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
}
