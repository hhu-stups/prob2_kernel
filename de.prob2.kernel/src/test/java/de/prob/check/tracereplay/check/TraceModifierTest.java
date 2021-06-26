package de.prob.check.tracereplay.check;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.check.tracereplay.check.renamig.DeltaCalculationException;
import de.prob.check.tracereplay.check.renamig.PrologTermNotDefinedException;
import de.prob.check.tracereplay.check.renamig.RenamingDelta;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TraceModifierTest {
	private static TraceManager traceManager;
	private static ProBKernelStub proBKernelStub;

	@BeforeAll
	static void beforeAll() {
		traceManager = CliTestCommon.getInjector().getInstance(TraceManager.class);
		proBKernelStub = CliTestCommon.getInjector().getInstance(ProBKernelStub.class);
	}

	@AfterAll
	static void afterAll() {
		proBKernelStub.killCurrentAnimator();
	}

	@Test
	public void changeAll_test_changed_parameter_names(){
		List<PersistentTransition> currentState;
		List<PersistentTransition> expected;

		PersistentTransition persistentTransition = new PersistentTransition("hallo", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition persistentTransition2 = new PersistentTransition("welt", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");

		currentState = Arrays.asList(persistentTransition, persistentTransition2, persistentTransition);


		PersistentTransition expectedTransition = new PersistentTransition("hallihallo", Collections.singletonMap("b", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		RenamingDelta renamingDelta = new RenamingDelta("hallo", "hallihallo", Collections.singletonMap("x", "b"), Collections.emptyMap(), Collections.emptyMap());

		expected = Arrays.asList(expectedTransition, persistentTransition2, expectedTransition);

		List<PersistentTransition> result = TraceModifier.changeAll(renamingDelta, currentState);


		Assertions.assertEquals(expected, result);
	}


	@Test
	public void changeAll_everything_changed(){
		List<PersistentTransition> currentState;
		List<PersistentTransition> expected;

		PersistentTransition persistentTransition = new PersistentTransition("hallo", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition persistentTransition2 = new PersistentTransition("welt", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");

		currentState = Arrays.asList(persistentTransition, persistentTransition2, persistentTransition);


		PersistentTransition expectedTransition = new PersistentTransition("hallihallo", Collections.singletonMap("b", "1"),
				Collections.singletonMap("g", "1"), Collections.singletonMap("f", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		RenamingDelta renamingDelta = new RenamingDelta("hallo", "hallihallo", Collections.singletonMap("x", "b"),
				Collections.singletonMap("y", "g"),
				Collections.singletonMap("z", "f"));

		expected = Arrays.asList(expectedTransition, persistentTransition2, expectedTransition);

		List<PersistentTransition> result = TraceModifier.changeAll(renamingDelta, currentState);


		Assertions.assertEquals(expected, result);
	}

	@Test
	public void changeAll_nothing_changed(){
		List<PersistentTransition> currentState;
		List<PersistentTransition> expected;

		PersistentTransition persistentTransition = new PersistentTransition("hallo", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition persistentTransition2 = new PersistentTransition("welt", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");

		currentState = Arrays.asList(persistentTransition, persistentTransition2, persistentTransition);




		RenamingDelta renamingDelta = new RenamingDelta("hallo", "hallo", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

		expected = Arrays.asList(persistentTransition, persistentTransition2, persistentTransition);

		List<PersistentTransition> result = TraceModifier.changeAll(renamingDelta, currentState);


		Assertions.assertEquals(expected, result);
	}

	@Test
	void unifyTransitionList_test(){
		List<PersistentTransition> currentState1;
		List<PersistentTransition> currentState2;
		List<PersistentTransition> original;


		PersistentTransition persistentTransition = new PersistentTransition("eins", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition3 = new PersistentTransition("drei", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition5 = new PersistentTransition("fuenf", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition6 = new PersistentTransition("sechs", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition7 = new PersistentTransition("sieben", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition8 = new PersistentTransition("acht", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


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


		Assertions.assertEquals(expected, result);

	}
/*

	@Test
	void changeAmbiguous_test(){
		RenamingDelta delta1 = new RenamingDelta("eins", "hallo", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta delta2 = new RenamingDelta("eins", "welt", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta delta3 = new RenamingDelta("eins", "wie", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		RenamingDelta delta4 = new RenamingDelta("drei", "mir", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta delta5 = new RenamingDelta("drei", "gehts", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta delta6 = new RenamingDelta("drei", "gut", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		Map<String, List<RenamingDelta>> change = new HashMap<>();

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

		Map<String, List<RenamingDelta>> change = new HashMap<>();


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
*/

	@Test
	public void deltaPermutation_test_1(){
		RenamingDelta renamingDelta1 = new RenamingDelta("inc", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta2 = new RenamingDelta("inc", "inc1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta3 = new RenamingDelta("inc", "inc2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta4 = new RenamingDelta("dec", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta5 = new RenamingDelta("dec", "dec1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta6 = new RenamingDelta("dec", "dec2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		
		List<RenamingDelta> list1 = new ArrayList<>();
		list1.add(renamingDelta1);
		list1.add(renamingDelta2);
		list1.add(renamingDelta3);
		List<RenamingDelta> list2 = new ArrayList<>();
		list2.add(renamingDelta4);
		list2.add(renamingDelta5);
		list2.add(renamingDelta6);
		
		Set<List<RenamingDelta>> delta = new HashSet<>();
		delta.add(list1);
		delta.add(list2);
		
		List<Set<RenamingDelta>> result = TraceModifier.deltaPermutation(delta);
		Assertions.assertEquals(9, result.size());

	}

	@Test
	public void deltaPermutation_test_2(){
		RenamingDelta renamingDelta1 = new RenamingDelta("inc", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta2 = new RenamingDelta("inc", "inc1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta3 = new RenamingDelta("inc", "inc2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta4 = new RenamingDelta("dec", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta5 = new RenamingDelta("dec", "dec1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta6 = new RenamingDelta("dec", "dec2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta7 = new RenamingDelta("get", "binc", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta8 = new RenamingDelta("get", "get1", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta9 = new RenamingDelta("get", "get2", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		List<RenamingDelta> list1 = new ArrayList<>();
		list1.add(renamingDelta1);
		list1.add(renamingDelta2);
		list1.add(renamingDelta3);

		List<RenamingDelta> list2 = new ArrayList<>();
		list2.add(renamingDelta4);
		list2.add(renamingDelta5);
		list2.add(renamingDelta6);

		List<RenamingDelta> list3 = new ArrayList<>();
		list3.add(renamingDelta7);
		list3.add(renamingDelta8);
		list3.add(renamingDelta9);

		Set<List<RenamingDelta>> delta = new HashSet<>();
		delta.add(list1);
		delta.add(list2);
		delta.add(list3);


		List<Set<RenamingDelta>> result = TraceModifier.deltaPermutation(delta);
		Assertions.assertEquals(27, result.size());

	}


	@Test
	public void deltaPermutation_test_empty_parameter(){
		List<Set<RenamingDelta>> result = TraceModifier.deltaPermutation(Collections.emptySet());
		Assertions.assertEquals(Collections.emptyList(), result);

	}


	@Test
	public void applyMultipleChanges_test_empty(){
		PersistentTransition persistentTransition = new PersistentTransition("eins", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition3 = new PersistentTransition("drei", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition5 = new PersistentTransition("fuenf", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition6 = new PersistentTransition("sechs", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");



		List<PersistentTransition> original = Arrays.asList(persistentTransition, persistentTransition5, persistentTransition3,
				persistentTransition6, persistentTransition5, persistentTransition5,
				persistentTransition, persistentTransition6);

		List<PersistentTransition> result = TraceModifier.applyMultipleChanges(Collections.emptySet(), original);

		Assertions.assertEquals(original, result);


	}


	@Test
	public void applyMultipleChanges_test(){
		Set<RenamingDelta> changes = new HashSet<>();

		changes.add(new RenamingDelta("eins", "zwei", Collections.singletonMap("x", "a"), Collections.emptyMap(), Collections.emptyMap()));
		changes.add(new RenamingDelta("fuenf", "sieben", Collections.singletonMap("x", "a"), Collections.emptyMap(), Collections.emptyMap()));


		PersistentTransition persistentTransition = new PersistentTransition("eins", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition3 = new PersistentTransition("drei", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition5 = new PersistentTransition("fuenf", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition6 = new PersistentTransition("sechs", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");



		List<PersistentTransition> original = Arrays.asList(persistentTransition, persistentTransition5, persistentTransition3,
				persistentTransition6, persistentTransition5, persistentTransition5,
				persistentTransition, persistentTransition6);


		PersistentTransition persistentTransition2 = new PersistentTransition("zwei", Collections.singletonMap("a", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition7 = new PersistentTransition("sieben", Collections.singletonMap("a", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		List<PersistentTransition> expected = Arrays.asList(persistentTransition2, persistentTransition7, persistentTransition3,
				persistentTransition6, persistentTransition7, persistentTransition7,
				persistentTransition2, persistentTransition6);

		List<PersistentTransition> result = TraceModifier.applyMultipleChanges(changes, original);


		Assertions.assertEquals(expected, result);
	}



	@Test
	public void concat_test(){
		List<String> first = Arrays.asList("a", "b", "c");
		List<String> second = Arrays.asList("1", "2", "3");

		List<String> result = TraceModifier.concat(first, second);

		List<String> expected = Arrays.asList("a","b", "c", "1", "2", "3");

		Assertions.assertEquals(expected, result);

	}


	@Test
	public void concat_empty_test(){
		List<String> first = Collections.emptyList();
		List<String> second = Collections.emptyList();

		List<String> result = TraceModifier.concat(first, second);

		List<String> expected = Collections.emptyList();

		Assertions.assertEquals(expected, result);
	}


	@Test
	public void changelog_test(){
		RenamingDelta renamingDelta1 = new RenamingDelta("eins", "hallo", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta2 = new RenamingDelta("eins", "welt", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		RenamingDelta renamingDelta4 = new RenamingDelta("drei", "mir", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
		RenamingDelta renamingDelta5 = new RenamingDelta("drei", "gehts", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());


		Set<List<RenamingDelta>> change = new HashSet<>();

		change.add( Arrays.asList(renamingDelta1, renamingDelta2));
		change.add( Arrays.asList(renamingDelta4, renamingDelta5));


		PersistentTransition persistentTransition = new PersistentTransition("eins", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition3 = new PersistentTransition("drei", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition5 = new PersistentTransition("fuenf", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");


		PersistentTransition persistentTransition6 = new PersistentTransition("sechs", Collections.singletonMap("x", "1"),
				Collections.singletonMap("y", "1"), Collections.singletonMap("z", "1"), Collections.singleton("a"),
				Collections.emptyList(), Collections.emptyList(), "");



		List<PersistentTransition> original = Arrays.asList(persistentTransition, persistentTransition5, persistentTransition3,
				persistentTransition6, persistentTransition5, persistentTransition5,
				persistentTransition, persistentTransition6);


		List<PersistentTransition> firstPart = TraceModifier.changeAll(renamingDelta1, original);
		List<PersistentTransition> secondPart = TraceModifier.changeAll(renamingDelta2, original);

		List<PersistentTransition> expected1 = TraceModifier.changeAll(renamingDelta4, firstPart);
		List<PersistentTransition> expected2 = TraceModifier.changeAll(renamingDelta5, firstPart);

		List<PersistentTransition> expected4 = TraceModifier.changeAll(renamingDelta4, secondPart);
		List<PersistentTransition> expected5 = TraceModifier.changeAll(renamingDelta5, secondPart);



		Set<RenamingDelta> key1 = new HashSet<>();
		key1.add(renamingDelta1);
		key1.add(renamingDelta4);
		Map<Set<RenamingDelta>, List<PersistentTransition>> expected = new HashMap<>() ;
		expected.put(key1, expected1);


		Set<RenamingDelta> key2 = new HashSet<>();
		key2.add(renamingDelta1);
		key2.add(renamingDelta5);
		expected.put(key2, expected2);

		Set<RenamingDelta> key3 = new HashSet<>();
		key3.add(renamingDelta2);
		key3.add(renamingDelta4);
		expected.put(key3, expected4);


		Set<RenamingDelta> key4 = new HashSet<>();
		key4.add(renamingDelta2);
		key4.add(renamingDelta5);
		expected.put(key4, expected5);



		Map<Set<RenamingDelta>, List<PersistentTransition>> result = TraceModifier.changeLog(change, original);


		Assertions.assertEquals(expected, result);

	}


	@Test
	public void changelog_empty_test(){

		Map<Set<RenamingDelta>, List<PersistentTransition>> result = TraceModifier.changeLog(Collections.emptySet(), Collections.emptyList());


		Assertions.assertEquals(Collections.emptyMap(), result);

	}

	@Test
	public void is_dirty_type_III_test_1() throws IOException, PrologTermNotDefinedException, DeltaCalculationException {

		Path oldPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "complexExample", "PitmanController_v6.mch");
		StateSpace stateSpace1 = proBKernelStub.createStateSpace(oldPath);
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "complexExample", "PitmanController_v6_v5.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "complexExample", "traceShort.prob2trace"));

		TraceChecker traceChecker = new TraceChecker(
				jsonFile.getTransitionList(),
				jsonFile.getMachineOperationInfos(),
				newInfos, new HashSet<>(oldVars),
				new HashSet<>(stateSpace2.getLoadedMachine().getVariableNames()),
				oldPath.toString(),
				newPath.toString(),
				CliTestCommon.getInjector(),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());

		TraceModifier bla = traceChecker.getTraceModifier();

		Assertions.assertEquals(2, bla.tracesStoredInTypeIII());

	}
}
