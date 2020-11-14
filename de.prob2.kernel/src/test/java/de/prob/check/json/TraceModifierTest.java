package de.prob.check.json;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.Delta;
import de.prob.check.tracereplay.check.TraceModifier;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TraceModifierTest {


	@Test
	public void changeAll_test_changed_parameter_names(){
		List<PersistentTransition> currentState;
		List<PersistentTransition> expected;

		PersistentTransition persistentTransition = new PersistentTransition("hallo", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());

		PersistentTransition persistentTransition2 = new PersistentTransition("welt", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());

		currentState = Arrays.asList(persistentTransition, persistentTransition2, persistentTransition);


		PersistentTransition expectedTransition = new PersistentTransition("hallihallo", Collections.singletonMap("b", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		Delta delta = new Delta("hallihallo", Collections.singletonMap("x", "b"), Collections.emptyMap(), Collections.emptyMap());

		expected = Arrays.asList(expectedTransition, persistentTransition2, expectedTransition);

		List<PersistentTransition> result = TraceModifier.changeAll("hallo", delta, currentState);


		Assert.assertEquals(expected, result);
	}


	@Test
	public void changeAll_everything_changed(){
		List<PersistentTransition> currentState;
		List<PersistentTransition> expected;

		PersistentTransition persistentTransition = new PersistentTransition("hallo", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());

		PersistentTransition persistentTransition2 = new PersistentTransition("welt", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());

		currentState = Arrays.asList(persistentTransition, persistentTransition2, persistentTransition);


		PersistentTransition expectedTransition = new PersistentTransition("hallihallo", Collections.singletonMap("b", "1"),
				Collections.singletonMap("g", "1"), Collections.singletonMap("f", "1"), Collections.singleton("a"),
				Collections.emptyList());


		Delta delta = new Delta("hallihallo", Collections.singletonMap("x", "b"),
				Collections.singletonMap("y", "g"),
				Collections.singletonMap("z", "f"));

		expected = Arrays.asList(expectedTransition, persistentTransition2, expectedTransition);

		List<PersistentTransition> result = TraceModifier.changeAll("hallo", delta, currentState);


		Assert.assertEquals(expected, result);
	}

	@Test
	public void changeAll_nothing_changed(){
		List<PersistentTransition> currentState;
		List<PersistentTransition> expected;

		PersistentTransition persistentTransition = new PersistentTransition("hallo", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());

		PersistentTransition persistentTransition2 = new PersistentTransition("welt", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());

		currentState = Arrays.asList(persistentTransition, persistentTransition2, persistentTransition);




		Delta delta = new Delta("hallo", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

		expected = Arrays.asList(persistentTransition, persistentTransition2, persistentTransition);

		List<PersistentTransition> result = TraceModifier.changeAll("hallo", delta, currentState);


		Assert.assertEquals(expected, result);
	}

	@Test
	void unifyTransitionList_test(){
		List<PersistentTransition> currentState1;
		List<PersistentTransition> currentState2;
		List<PersistentTransition> original;


		PersistentTransition persistentTransition = new PersistentTransition("eins", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition3 = new PersistentTransition("drei", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition5 = new PersistentTransition("fuenf", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition6 = new PersistentTransition("sechs", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition7 = new PersistentTransition("sieben", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition8 = new PersistentTransition("acht", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		original = Arrays.asList(persistentTransition7, persistentTransition5, persistentTransition8,
				persistentTransition6, persistentTransition5, persistentTransition5,
				persistentTransition7, persistentTransition6);


		currentState1 = Arrays.asList(persistentTransition, persistentTransition5, persistentTransition8,
				persistentTransition6, persistentTransition5, persistentTransition5,
				persistentTransition, persistentTransition6);


		currentState2 = Arrays.asList(persistentTransition7, persistentTransition3, persistentTransition8,
				persistentTransition6, persistentTransition3, persistentTransition3,
				persistentTransition7, persistentTransition6);


		List<PersistentTransition> result = TraceModifier.unifyTransitionList(currentState1, currentState2, original);

		List<PersistentTransition> expected = Arrays.asList(persistentTransition, persistentTransition3, persistentTransition8,
				persistentTransition6, persistentTransition3, persistentTransition3,
				persistentTransition, persistentTransition6);


		Assert.assertEquals(expected, result);

	}


	@Test
	void changeAmbiguous_test(){
		Delta delta1 = new Delta("hallo", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta2 = new Delta("welt", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta3 = new Delta("wie", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		Delta delta4 = new Delta("mir", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta5 = new Delta("gehts", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta6 = new Delta("gut", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		Map<String, List<Delta>> change = new HashMap<>();

		change.put("eins", Arrays.asList(delta1, delta2 ,delta3));
		change.put("drei", Arrays.asList(delta4, delta5 ,delta6));


		PersistentTransition persistentTransition = new PersistentTransition("eins", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition3 = new PersistentTransition("drei", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition5 = new PersistentTransition("fuenf", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition6 = new PersistentTransition("sechs", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());



		List<PersistentTransition> original = Arrays.asList(persistentTransition, persistentTransition5, persistentTransition3,
				persistentTransition6, persistentTransition5, persistentTransition5,
				persistentTransition, persistentTransition6);


		List<PersistentTransition> firstPart = TraceModifier.changeAll("eins", delta1, original);
		List<PersistentTransition> secondPart = TraceModifier.changeAll("eins", delta2, original);
		List<PersistentTransition> thirdPart = TraceModifier.changeAll("eins", delta3, original);

		List<PersistentTransition> expected1 = TraceModifier.changeAll("drei", delta4, firstPart);
		List<PersistentTransition> expected2 = TraceModifier.changeAll("drei", delta5, firstPart);
		List<PersistentTransition> expected3 = TraceModifier.changeAll("drei", delta6, firstPart);

		List<PersistentTransition> expected4 = TraceModifier.changeAll("drei", delta4, secondPart);
		List<PersistentTransition> expected5 = TraceModifier.changeAll("drei", delta5, secondPart);
		List<PersistentTransition> expected6 = TraceModifier.changeAll("drei", delta6, secondPart);

		List<PersistentTransition> expected7 = TraceModifier.changeAll("drei", delta4, thirdPart);
		List<PersistentTransition> expected8 = TraceModifier.changeAll("drei", delta5, thirdPart);
		List<PersistentTransition> expected9 = TraceModifier.changeAll("drei", delta6, thirdPart);


		List<List<PersistentTransition>> expected =
				Arrays.asList(expected1,expected2,expected3,expected4,expected5,expected6,expected7,expected8,expected9);


		List<List<PersistentTransition>> result = TraceModifier.changeAmbiguous(change, original);

		Assert.assertEquals(expected, result);

	}


	@Test
	void changeAmbiguous_empty_delta_test(){

		Map<String, List<Delta>> change = new HashMap<>();


		PersistentTransition persistentTransition = new PersistentTransition("eins", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition3 = new PersistentTransition("drei", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition5 = new PersistentTransition("fuenf", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition6 = new PersistentTransition("sechs", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());



		List<PersistentTransition> original = Arrays.asList(persistentTransition, persistentTransition5, persistentTransition3,
				persistentTransition6, persistentTransition5, persistentTransition5,
				persistentTransition, persistentTransition6);


		List<List<PersistentTransition>> result = TraceModifier.changeAmbiguous(change, original);

		Assert.assertEquals(Collections.singletonList(original), result);

	}



}
