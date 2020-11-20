package de.prob.check.json;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.Delta;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.TraceModifier;
import org.apache.groovy.util.Maps;
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


		Delta delta = new Delta("hallo", "hallihallo", Collections.singletonMap("x", "b"), Collections.emptyMap(), Collections.emptyMap());

		expected = Arrays.asList(expectedTransition, persistentTransition2, expectedTransition);

		List<PersistentTransition> result = TraceModifier.changeAll(delta, currentState);


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


		Delta delta = new Delta("hallo", "hallihallo", Collections.singletonMap("x", "b"),
				Collections.singletonMap("y", "g"),
				Collections.singletonMap("z", "f"));

		expected = Arrays.asList(expectedTransition, persistentTransition2, expectedTransition);

		List<PersistentTransition> result = TraceModifier.changeAll(delta, currentState);


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




		Delta delta = new Delta("hallo", "hallo", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

		expected = Arrays.asList(persistentTransition, persistentTransition2, persistentTransition);

		List<PersistentTransition> result = TraceModifier.changeAll( delta, currentState);


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
		Delta delta1 = new Delta("eins", "hallo", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta2 = new Delta("eins", "welt", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta3 = new Delta("eins", "wie", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		Delta delta4 = new Delta("drei", "mir", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta5 = new Delta("drei", "gehts", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta6 = new Delta("drei", "gut", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


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


		List<PersistentTransition> firstPart = TraceModifier.changeAll(delta1, original);
		List<PersistentTransition> secondPart = TraceModifier.changeAll(delta2, original);
		List<PersistentTransition> thirdPart = TraceModifier.changeAll(delta3, original);

		List<PersistentTransition> expected1 = TraceModifier.changeAll( delta4, firstPart);
		List<PersistentTransition> expected2 = TraceModifier.changeAll( delta5, firstPart);
		List<PersistentTransition> expected3 = TraceModifier.changeAll( delta6, firstPart);

		List<PersistentTransition> expected4 = TraceModifier.changeAll( delta4, secondPart);
		List<PersistentTransition> expected5 = TraceModifier.changeAll( delta5, secondPart);
		List<PersistentTransition> expected6 = TraceModifier.changeAll( delta6, secondPart);

		List<PersistentTransition> expected7 = TraceModifier.changeAll( delta4, thirdPart);
		List<PersistentTransition> expected8 = TraceModifier.changeAll( delta5, thirdPart);
		List<PersistentTransition> expected9 = TraceModifier.changeAll( delta6, thirdPart);


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


	@Test
	public void deltaPermutation_test_1(){
		Delta delta1 = new Delta("inc", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta2 = new Delta("inc", "inc1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta3 = new Delta("inc", "inc2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta4 = new Delta("dec", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta5 = new Delta("dec", "dec1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta6 = new Delta("dec", "dec2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		
		List<Delta> list1 = new ArrayList<>();
		list1.add(delta1);
		list1.add(delta2);
		list1.add(delta3);
		List<Delta> list2 = new ArrayList<>();
		list2.add(delta4);
		list2.add(delta5);
		list2.add(delta6);
		
		List<List<Delta>> delta = new ArrayList<>();
		delta.add(list1);
		delta.add(list2);
		
		List<Set<Delta>> result = TraceModifier.deltaPermutation(delta);
		Assert.assertEquals(9, result.size());

	}

	@Test
	public void deltaPermutation_test_2(){
		Delta delta1 = new Delta("inc", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta2 = new Delta("inc", "inc1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta3 = new Delta("inc", "inc2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta4 = new Delta("dec", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta5 = new Delta("dec", "dec1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta6 = new Delta("dec", "dec2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta7 = new Delta("get", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta8 = new Delta("get", "get1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta9 = new Delta("get", "get2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		List<Delta> list1 = new ArrayList<>();
		list1.add(delta1);
		list1.add(delta2);
		list1.add(delta3);

		List<Delta> list2 = new ArrayList<>();
		list2.add(delta4);
		list2.add(delta5);
		list2.add(delta6);

		List<Delta> list3 = new ArrayList<>();
		list3.add(delta7);
		list3.add(delta8);
		list3.add(delta9);

		List<List<Delta>> delta = new ArrayList<>();
		delta.add(list1);
		delta.add(list2);
		delta.add(list3);


		List<Set<Delta>> result = TraceModifier.deltaPermutation(delta);
		Assert.assertEquals(27, result.size());

	}


	@Test
	public void deltaPermutation_test_empty_parameter(){
		List<Set<Delta>> result = TraceModifier.deltaPermutation(Collections.emptyList());
		Assert.assertEquals(Collections.emptyList(), result);

	}


	@Test
	public void applyMultipleChanges_test_empty(){
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

		List<PersistentTransition> result = TraceModifier.applyMultipleChanges(Collections.emptySet(), original);

		Assert.assertEquals(original, result);


	}


	@Test
	public void applyMultipleChanges_test(){
		Set<Delta> changes = new HashSet<>();

		changes.add(new Delta("eins", "zwei", Collections.singletonMap("x", "a"), Collections.emptyMap(), Collections.emptyMap()));
		changes.add(new Delta("fuenf", "sieben", Collections.singletonMap("x", "a"), Collections.emptyMap(), Collections.emptyMap()));


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


		PersistentTransition persistentTransition2 = new PersistentTransition("zwei", Collections.singletonMap("a", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		PersistentTransition persistentTransition7 = new PersistentTransition("sieben", Collections.singletonMap("a", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList());


		List<PersistentTransition> expected = Arrays.asList(persistentTransition2, persistentTransition7, persistentTransition3,
				persistentTransition6, persistentTransition7, persistentTransition7,
				persistentTransition2, persistentTransition6);

		List<PersistentTransition> result = TraceModifier.applyMultipleChanges(changes, original);


		Assert.assertEquals(expected, result);
	}



	@Test
	public void concat_test(){
		List<String> first = Arrays.asList("a", "b", "c");
		List<String> second = Arrays.asList("1", "2", "3");

		List<String> result = TraceModifier.concat(first, second);

		List<String> expected = Arrays.asList("a","b", "c", "1", "2", "3");

		Assert.assertEquals(expected, result);

	}


	@Test
	public void concat_empty_test(){
		List<String> first = Collections.emptyList();
		List<String> second = Collections.emptyList();

		List<String> result = TraceModifier.concat(first, second);

		List<String> expected = Collections.emptyList();

		Assert.assertEquals(expected, result);
	}


	@Test
	public void changelog_test(){
		Delta delta1 = new Delta("eins", "hallo", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta2 = new Delta("eins", "welt", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		Delta delta4 = new Delta("drei", "mir", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		Delta delta5 = new Delta("drei", "gehts", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		Set<List<Delta>> change = new HashSet<>();

		change.add( Arrays.asList(delta1, delta2));
		change.add( Arrays.asList(delta4, delta5));


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


		List<PersistentTransition> firstPart = TraceModifier.changeAll(delta1, original);
		List<PersistentTransition> secondPart = TraceModifier.changeAll(delta2, original);

		List<PersistentTransition> expected1 = TraceModifier.changeAll( delta4, firstPart);
		List<PersistentTransition> expected2 = TraceModifier.changeAll( delta5, firstPart);

		List<PersistentTransition> expected4 = TraceModifier.changeAll( delta4, secondPart);
		List<PersistentTransition> expected5 = TraceModifier.changeAll( delta5, secondPart);



		Set<Delta> key1 = new HashSet<>();
		key1.add(delta1);
		key1.add(delta4);
		Map<Set<Delta>, List<PersistentTransition>> expected = new HashMap<>() ;
		expected.put(key1, expected1);


		Set<Delta> key2 = new HashSet<>();
		key2.add(delta1);
		key2.add(delta5);
		expected.put(key2, expected2);

		Set<Delta> key3 = new HashSet<>();
		key3.add(delta2);
		key3.add(delta4);
		expected.put(key3, expected4);


		Set<Delta> key4 = new HashSet<>();
		key4.add(delta2);
		key4.add(delta5);
		expected.put(key4, expected5);



		Map<Set<Delta>, List<PersistentTransition>> result = TraceModifier.changeLog(change, original);


		Assert.assertEquals(expected, result);

	}
}
