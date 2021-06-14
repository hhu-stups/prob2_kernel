package de.prob.check.tracereplay.check.exploration;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.formula.PredicateBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

public class ReplayOptionsTest {

	@Test
	public void replayOptions_test_1(){
		Set<ReplayOptions.OptionFlags> options = Arrays.stream(ReplayOptions.OptionFlags.values()).collect(Collectors.toSet());
		ReplayOptions replayOptions = new ReplayOptions(options, emptyList(), emptyMap(), emptyMap());

		PersistentTransition persistentTransition = new PersistentTransition("inc", emptyMap(), emptyMap(), emptyMap(), emptySet(), emptyList(), emptyList());

		PredicateBuilder result = replayOptions.createMapping(persistentTransition);

		Assertions.assertEquals(new PredicateBuilder(), result);
	}

	@Test
	public void replayOptions_test_2(){
		Set<ReplayOptions.OptionFlags> options = emptySet();
		ReplayOptions replayOptions = new ReplayOptions(options, emptyList(), emptyMap(), emptyMap());

		Map<String, String> input = singletonMap("b", "1=1");
		Map<String, String> output = singletonMap("c", "1=1");
		Map<String, String> variables = singletonMap("a", "1=1");

		PersistentTransition persistentTransition = new PersistentTransition("inc", input,
				output, variables, emptySet(), emptyList(), emptyList());

		PredicateBuilder expected = new PredicateBuilder();
		expected.addMap(input);
		expected.addMap(output);
		expected.addMap(variables);

		PredicateBuilder result = replayOptions.createMapping(persistentTransition);

		Assertions.assertEquals(expected, result);
	}

	@Test
	public void replayOptions_test_3(){
		Set<ReplayOptions.OptionFlags> options = Arrays.stream(ReplayOptions.OptionFlags.values()).collect(Collectors.toSet());
		ReplayOptions replayOptions = new ReplayOptions(options, emptyList(), emptyMap(), emptyMap());

		Map<String, String> input = singletonMap("b", "1=1");
		Map<String, String> output = singletonMap("c", "1=1");
		Map<String, String> variables = singletonMap("a", "1=1");

		PersistentTransition persistentTransition = new PersistentTransition("inc", input,
				output, variables, emptySet(), emptyList(), emptyList());

		PredicateBuilder expected = new PredicateBuilder();


		PredicateBuilder result = replayOptions.createMapping(persistentTransition);

		Assertions.assertEquals(expected, result);
	}


	@Test
	public void replayOptions_test_4(){
		Set<ReplayOptions.OptionFlags> options = emptySet();
		ReplayOptions replayOptions = new ReplayOptions(options, singletonList("a"), emptyMap(), emptyMap());

		Map<String, String> input = singletonMap("b", "1=1");
		Map<String, String> output = singletonMap("c", "1=1");
		Map<String, String> variables = singletonMap("a", "1=1");

		PersistentTransition persistentTransition = new PersistentTransition("inc", input,
				output, variables, emptySet(), emptyList(), emptyList());

		PredicateBuilder expected = new PredicateBuilder();
		expected.addMap(input);
		expected.addMap(output);


		PredicateBuilder result = replayOptions.createMapping(persistentTransition);

		Assertions.assertEquals(expected, result);
	}

	@Test
	public void replayOptions_test_5(){
		Set<ReplayOptions.OptionFlags> options = emptySet();
		ReplayOptions replayOptions = new ReplayOptions(options, singletonList("a"), singletonMap("inc", singleton(ReplayOptions.OptionFlags.Output)), singletonMap("inc", emptyList()));

		Map<String, String> input = singletonMap("b", "1=1");
		Map<String, String> output = singletonMap("c", "1=1");
		Map<String, String> variables = singletonMap("a", "1=1");

		PersistentTransition persistentTransition = new PersistentTransition("inc", input,
				output, variables, emptySet(), emptyList(), emptyList());

		PredicateBuilder expected = new PredicateBuilder();
		expected.addMap(input);


		PredicateBuilder result = replayOptions.createMapping(persistentTransition);

		Assertions.assertEquals(expected, result);
	}


	@Test
	public void replayOptions_test_6(){
		Set<ReplayOptions.OptionFlags> options = emptySet();
		ReplayOptions replayOptions = new ReplayOptions(options, singletonList("a"), singletonMap("inc", singleton(ReplayOptions.OptionFlags.Output)), singletonMap("inc", singletonList("b")));

		Map<String, String> input = singletonMap("b", "1=1");
		Map<String, String> output = singletonMap("c", "1=1");
		Map<String, String> variables = singletonMap("a", "1=1");

		PersistentTransition persistentTransition = new PersistentTransition("inc", input,
				output, variables, emptySet(), emptyList(), emptyList());

		PredicateBuilder expected = new PredicateBuilder();


		PredicateBuilder result = replayOptions.createMapping(persistentTransition);

		Assertions.assertEquals(expected, result);
	}

	@Test
	public void replayOptions_integration_test(){
		Set<ReplayOptions.OptionFlags> options = emptySet();
		ReplayOptions replayOptions = new ReplayOptions(options, singletonList("a"), singletonMap("inc", singleton(ReplayOptions.OptionFlags.Output)), singletonMap("inc", singletonList("b")));

		Map<String, String> input = singletonMap("b", "1=1");
		Map<String, String> output = singletonMap("c", "1=1");
		Map<String, String> variables = singletonMap("a", "1=1");

		PersistentTransition persistentTransition = new PersistentTransition("inc", input,
				output, variables, emptySet(), emptyList(), emptyList());

		PredicateBuilder expected = new PredicateBuilder();


		PredicateBuilder result = replayOptions.createMapping(persistentTransition);

		Assertions.assertEquals(expected, result);
	}
}
