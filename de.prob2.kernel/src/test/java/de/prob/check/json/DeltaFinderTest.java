package de.prob.check.json;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.MainModule;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.check.CheckerInterface;
import de.prob.check.tracereplay.check.DeltaFinder;
import de.prob.check.tracereplay.check.PrepareOperationsInterface;
import de.prob.check.tracereplay.check.Triple;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.Transition;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class DeltaFinderTest {



	@Test
	void checkDeterministicPairs_test(){
		CheckerInterface fakeCheckerInterface = (prepareOperations, candidate) -> new HashMap<>();


		PrepareOperationsInterface fakePrepareOperationsInterface = (operation) -> new Triple<>(new ListPrologTerm(),
				new ListPrologTerm(), new CompoundPrologTerm("a"));


		Map<String, CompoundPrologTerm> oldOperation = new HashMap<>();
		Map<String, CompoundPrologTerm> newOperation = new HashMap<>();
		Set<String> candidates = new HashSet<>();

		Map<String, Map<String, String>> result = DeltaFinder.checkDeterministicPairs(oldOperation, newOperation, candidates,
				fakeCheckerInterface, fakePrepareOperationsInterface);


		Assert.assertEquals(new HashMap<>(), result);


	}


	@Test
	void checkDeterministicPairs_test_one_gets_removed(){

		CheckerInterface fakeCheckerInterface = (prepareOperations, candidate) -> {
			if(prepareOperations.getThird().equals(new CompoundPrologTerm("inc"))){
				return new HashMap<>();
			}else{
				Map<String, String> result = new HashMap<>();
				result.put("a", "b");
				return result;
			}
		};


		PrepareOperationsInterface fakePrepareOperationsInterface = (operation) ->
				new Triple<>(new ListPrologTerm(), new ListPrologTerm(), operation);

		Map<String, CompoundPrologTerm> oldOperation = new HashMap<>();
		oldOperation.put("inc", new CompoundPrologTerm("inc"));
		oldOperation.put("dec", new CompoundPrologTerm("dec"));
		oldOperation.put("getFloors", new CompoundPrologTerm("getFloors"));

		Map<String, CompoundPrologTerm> newOperation = new HashMap<>();
		oldOperation.put("inc", new CompoundPrologTerm("inc"));
		oldOperation.put("dec", new CompoundPrologTerm("dec"));
		oldOperation.put("getFloors", new CompoundPrologTerm("getFloors"));

		Set<String> candidates = new HashSet<>(Arrays.asList("inc", "dec", "getFloors"));

		Map<String, Map<String, String>> result = DeltaFinder.checkDeterministicPairs(oldOperation, newOperation, candidates,
				fakeCheckerInterface, fakePrepareOperationsInterface);

		Map<String, String> expectedInner = new HashMap<>();
		expectedInner.put("a", "b");

		Map<String, Map<String, String>> expectedOuter = new HashMap<>();
		expectedOuter.put("dec", expectedInner);
		expectedOuter.put("getFloors", expectedInner);

		Assert.assertEquals(expectedOuter, result);
	}


	@Test
	void checkNondeterministicPairs_test_one_gets_removed(){

		CheckerInterface fakeCheckerInterface = (prepareOperations, candidate) -> {

			if(candidate.equals(new CompoundPrologTerm("inc23")) ||
					candidate.equals(new CompoundPrologTerm("deccc")) ){
				return new HashMap<>();
			}else{
				Map<String, String> result = new HashMap<>();
				result.put("a", "b");
				return result;
			}
		};


		PrepareOperationsInterface fakePrepareOperationsInterface = (operation) ->
				new Triple<>(new ListPrologTerm(), new ListPrologTerm(), operation);

		Map<String, CompoundPrologTerm> oldOperation = new HashMap<>();
		oldOperation.put("inc", new CompoundPrologTerm("inc"));
		oldOperation.put("dec", new CompoundPrologTerm("dec"));
		oldOperation.put("getFloors", new CompoundPrologTerm("getFloors"));

		Map<String, CompoundPrologTerm> newOperation = new HashMap<>();
		newOperation.put("inccc", new CompoundPrologTerm("inccc"));
		newOperation.put("inc4", new CompoundPrologTerm("inc4"));
		newOperation.put("inc23", new CompoundPrologTerm("inc23"));
		newOperation.put("deccc", new CompoundPrologTerm("deccc"));
		newOperation.put("GetFloors", new CompoundPrologTerm("GetFloors"));
		newOperation.put("getFloor", new CompoundPrologTerm("getFloor"));
		newOperation.put("currentFloors", new CompoundPrologTerm("currentFloors"));

		Map<String, Set<String>> candidates = new HashMap<>();

		candidates.put("inc", new HashSet<>(Arrays.asList("inccc", "inc4", "inc23")));
		candidates.put("dec", new HashSet<>(Collections.singletonList("deccc")));
		candidates.put("getFloors", new HashSet<>(Arrays.asList("GetFloors", "getFloor", "currentFloors")));


		Map<String, Map<String, Map<String, String>>> result = DeltaFinder.checkNondeterministicPairs(oldOperation, newOperation, candidates,
				fakeCheckerInterface, fakePrepareOperationsInterface);

		Map<String, String> expectedInner = new HashMap<>();
		expectedInner.put("a", "b");

		Map<String, Map<String, String>> expectedOuter1 = new HashMap<>();
		expectedOuter1.put("GetFloors", expectedInner);
		expectedOuter1.put("currentFloors", expectedInner);
		expectedOuter1.put("getFloor", expectedInner);



		Map<String, Map<String, String>> expectedOuter2 = new HashMap<>();
		expectedOuter2.put("inccc", expectedInner);
		expectedOuter2.put("inc4", expectedInner);


		Map<String, Map<String, Map<String, String>>> expectedResult = new HashMap<>();
		expectedResult.put("getFloors", expectedOuter1);
		expectedResult.put("inc", expectedOuter2);


		Assert.assertEquals(expectedResult, result);

	}


	@Test
	void test_getOldOperations() throws IOException, ModelTranslationError {
		System.setProperty("prob.home", "/home/sebastian/prob_prolog");

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "machineWithOneOperation.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine.mch");
		String pathAsString = path.toAbsolutePath().toString();


		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsString.substring(pathAsString.lastIndexOf(".")+1)));
		factory.extract(pathAsString).loadIntoStateSpace(reusableAnimator.createStateSpace());



		DeltaFinder deltaFinder = new DeltaFinder(Collections.emptySet(), Collections.emptyMap(), reusableAnimator, "", "",
				injector);


		Map<String, CompoundPrologTerm> oldOperations = deltaFinder.getOperations(pathAsStringOld);



		Assert.assertTrue(oldOperations.containsKey("inccc"));
	}


	@Test
	void test_getOldNewOperations() throws IOException, ModelTranslationError {
		System.setProperty("prob.home", "/home/sebastian/prob_prolog");

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "machineWithOneOperation.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine.mch");
		String pathAsString = path.toAbsolutePath().toString();


		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsString.substring(pathAsString.lastIndexOf(".")+1)));
		factory.extract(pathAsString).loadIntoStateSpace(reusableAnimator.createStateSpace());



		DeltaFinder deltaFinder = new DeltaFinder(Collections.emptySet(), Collections.emptyMap(), reusableAnimator, "", "",
				injector);


		Map<String, CompoundPrologTerm> newOperations = deltaFinder.getOperations(pathAsString);
		Map<String, CompoundPrologTerm> oldOperations = deltaFinder.getOperations(pathAsStringOld);


		Assert.assertTrue(newOperations.containsKey("inccc"));
		Assert.assertTrue(oldOperations.containsKey("inccc"));
	}


	@Test
	public void prepareOperationsInterface_test() throws IOException, ModelTranslationError {
		System.setProperty("prob.home", "/home/sebastian/prob_prolog");

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);

		DeltaFinder deltaFinder = new DeltaFinder(Collections.emptySet(), Collections.emptyMap(),
				reusableAnimator, pathAsStringOld, pathAsString, injector);

		Map<String, CompoundPrologTerm> newOperations = deltaFinder.getOperations(pathAsString);

		Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> result =
				deltaFinder.prepareOperationsInterface.prepareOperation(newOperations.get("getfloors"));


		Assert.assertEquals(new ListPrologTerm(new CompoundPrologTerm("floors"),
				new CompoundPrologTerm("getfloors"),
				new CompoundPrologTerm("out")), result.getFirst());

	}


	@Test
	public void checkerInterface_test() throws IOException, ModelTranslationError {
		System.setProperty("prob.home", "/home/sebastian/prob_prolog");

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);

		DeltaFinder deltaFinder = new DeltaFinder(Collections.emptySet(), Collections.emptyMap(), reusableAnimator,
				pathAsStringOld, pathAsString, injector);

		Map<String, CompoundPrologTerm> oldOperations = deltaFinder.getOperations(pathAsStringOld);
		Map<String, CompoundPrologTerm> newOperations = deltaFinder.getOperations(pathAsString);

		Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> oldInc =
				deltaFinder.prepareOperationsInterface.prepareOperation(oldOperations.get("inc"));


		Map<String, String> result = deltaFinder.checkerInterface.checkTypeII(oldInc, newOperations.get("inc"));

		Assert.assertEquals("inc", result.get("inc"));

	}


	@Test
	public void integration_test() throws IOException, ModelTranslationError {
		System.setProperty("prob.home", "/home/sebastian/prob_prolog");

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift3.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);

		DeltaFinder deltaFinder = new DeltaFinder(Collections.emptySet(), Collections.emptyMap(), reusableAnimator,
				pathAsStringOld, pathAsString, injector);

		Set<String> candidates = new HashSet<>(Arrays.asList("inc", "dec", "getfloors", "$initialise_machine"));
		Map<String, CompoundPrologTerm> newOperations = deltaFinder.getOperations(pathAsString);
		Map<String, CompoundPrologTerm> oldOperations = deltaFinder.getOperations(pathAsStringOld);


		Map<String, Map<String, String>> result = DeltaFinder.checkDeterministicPairs(oldOperations, newOperations,
				candidates, deltaFinder.checkerInterface, deltaFinder.prepareOperationsInterface);

		Map<String, Map<String, String>> expected = new HashMap<>();
		Map<String, String> initMap = new HashMap<>();
		initMap.put("floors", "level");
		expected.put(Transition.INITIALISE_MACHINE_NAME, initMap);
		Map<String, String> opMap1 = new HashMap<>();
		opMap1.put("floors", "level");
		opMap1.put("dec", "dec");
		expected.put("dec", opMap1);

		Map<String, String> opMap2 = new HashMap<>();
		opMap2.put("floors", "level");
		opMap2.put("getfloors", "getfloors");
		opMap2.put("out", "out");
		expected.put("getfloors", opMap2);


		Map<String, String> opMap3 = new HashMap<>();
		opMap3.put("floors", "level");
		opMap3.put("inc", "inc");
		expected.put("inc", opMap3);

		Assert.assertEquals(expected, result);

	}
}
