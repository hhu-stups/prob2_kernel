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
		expected.add(Collections.singletonMap("x","x"));
		expected.add(Collections.singletonMap("x","y"));
		expected.add(Collections.singletonMap("x","z"));

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
		result1.put("x", "x");
		result1.put("y", "y");
		expected.add(result1);
		Map<String, String> result2 = new HashMap<>();
		result2.put("x", "y");
		result2.put("y", "x");
		expected.add(result2);
		Map<String, String> result3 = new HashMap<>();
		result3.put("x", "z");
		result3.put("y", "y");
		expected.add(result3);
		Map<String, String> result4 = new HashMap<>();
		result4.put("x", "z");
		result4.put("y", "x");
		expected.add(result4);
		Map<String, String> result5 = new HashMap<>();
		result5.put("x", "x");
		result5.put("y", "z");
		expected.add(result5);
		Map<String, String> result6 = new HashMap<>();
		result6.put("x", "y");
		result6.put("y", "z");
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
		result1.put("x", "x");
		result1.put("y", "y");
		expected.add(result1);
		Map<String, String> result2 = new HashMap<>();
		result2.put("x", "y");
		result2.put("y", "x");
		expected.add(result2);
		Map<String, String> result3 = new HashMap<>();
		result3.put("x", "z");
		result3.put("y", "y");
		expected.add(result3);
		Map<String, String> result4 = new HashMap<>();
		result4.put("x", "z");
		result4.put("y", "x");
		expected.add(result4);
		Map<String, String> result5 = new HashMap<>();
		result5.put("x", "x");
		result5.put("y", "z");
		expected.add(result5);
		Map<String, String> result6 = new HashMap<>();
		result6.put("x", "y");
		result6.put("y", "z");
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
}
