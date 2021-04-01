package de.prob.check.tracereplay.check.renaming;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Injector;

import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.check.renamig.CheckerInterface;
import de.prob.check.tracereplay.check.renamig.DeltaCalculationException;
import de.prob.check.tracereplay.check.renamig.DynamicRenamingAnalyzer;
import de.prob.check.tracereplay.check.renamig.PrepareOperationsInterface;
import de.prob.check.tracereplay.check.renamig.PrologTermNotDefinedException;
import de.prob.check.tracereplay.check.renamig.RenamingDelta;
import de.prob.check.tracereplay.check.renamig.Triple;
import de.prob.cli.CliTestCommon;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.Collections.*;


public class DynamicRenamingAnalyzerTest {



	@Test
	void checkDeterministicPairs_test() throws PrologTermNotDefinedException {
		CheckerInterface fakeCheckerInterface = (prepareOperations, candidate) -> new HashMap<>();


		PrepareOperationsInterface fakePrepareOperationsInterface = (operation) -> new Triple<>(new ListPrologTerm(),
				new ListPrologTerm(), new CompoundPrologTerm("a"));


		Map<String, CompoundPrologTerm> oldOperation = new HashMap<>();
		Map<String, CompoundPrologTerm> newOperation = new HashMap<>();
		Set<String> candidates = new HashSet<>();

		Map<String, Map<String, String>> result = DynamicRenamingAnalyzer.checkDeterministicPairs(oldOperation, newOperation, candidates,
				fakeCheckerInterface, fakePrepareOperationsInterface);


		Assertions.assertEquals(new HashMap<>(), result);


	}


	@Test
	void checkDeterministicPairs_test_one_gets_removed() throws PrologTermNotDefinedException {

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

		Map<String, Map<String, String>> result = DynamicRenamingAnalyzer.checkDeterministicPairs(oldOperation, newOperation, candidates,
				fakeCheckerInterface, fakePrepareOperationsInterface);

		Map<String, String> expectedInner = new HashMap<>();
		expectedInner.put("a", "b");

		Map<String, Map<String, String>> expectedOuter = new HashMap<>();
		expectedOuter.put("dec", expectedInner);
		expectedOuter.put("getFloors", expectedInner);

		Assertions.assertEquals(expectedOuter, result);
	}


	@Test
	void checkNondeterministicPairs_test_one_gets_removed() throws PrologTermNotDefinedException {

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


		Map<String, Map<String, Map<String, String>>> result = DynamicRenamingAnalyzer.checkNondeterministicPairs(oldOperation, newOperation, candidates,
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


		Assertions.assertEquals(expectedResult, result);

	}



	@Test
	void test_getOldNewOperations() throws IOException {

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "examplesForOperations", "machineWithOneOperation.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "examplesForOperations", "machineWithOneOperation.mch");
		String pathAsString = path.toAbsolutePath().toString();


		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".")+1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsStringOld).loadIntoStateSpace(stateSpace);



		DynamicRenamingAnalyzer dynamicRenamingAnalyzer = new DynamicRenamingAnalyzer(Collections.emptySet(), Collections.emptyMap(), false,
				 "", "", injector, stateSpace.getLoadedMachine().getOperations());


		Map<String, CompoundPrologTerm> newOperations = dynamicRenamingAnalyzer.getOperations(pathAsString);
		Map<String, CompoundPrologTerm> oldOperations = dynamicRenamingAnalyzer.getOperations(pathAsStringOld);


		Assertions.assertTrue(newOperations.containsKey("on"));
		Assertions.assertTrue(oldOperations.containsKey("on"));
	}


	@Test
	public void prepareOperationsInterface_test() throws IOException, PrologTermNotDefinedException {

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".")+1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsStringOld).loadIntoStateSpace(stateSpace);



		DynamicRenamingAnalyzer dynamicRenamingAnalyzer = new DynamicRenamingAnalyzer(Collections.emptySet(), Collections.emptyMap(), false,
				pathAsStringOld, pathAsString, injector, stateSpace.getLoadedMachine().getOperations());

		Map<String, CompoundPrologTerm> newOperations = dynamicRenamingAnalyzer.getOperations(pathAsString);

		Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> result =
				dynamicRenamingAnalyzer.prepareOperationsInterface.prepareOperation(newOperations.get("getfloors"));


		Assertions.assertEquals(new ListPrologTerm(new CompoundPrologTerm("floors"),
				new CompoundPrologTerm("getfloors"),
				new CompoundPrologTerm("out")), result.getFirst());

	}


	@Test
	public void checkerInterface_test() throws IOException, PrologTermNotDefinedException {

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".")+1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsStringOld).loadIntoStateSpace(stateSpace);


		DynamicRenamingAnalyzer dynamicRenamingAnalyzer = new DynamicRenamingAnalyzer(Collections.emptySet(), Collections.emptyMap(), false,
				pathAsStringOld, pathAsString, injector, stateSpace.getLoadedMachine().getOperations());

		Map<String, CompoundPrologTerm> oldOperations = dynamicRenamingAnalyzer.getOperations(pathAsStringOld);
		Map<String, CompoundPrologTerm> newOperations = dynamicRenamingAnalyzer.getOperations(pathAsString);

		Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> oldInc =
				dynamicRenamingAnalyzer.prepareOperationsInterface.prepareOperation(oldOperations.get("inc"));


		Map<String, String> result = dynamicRenamingAnalyzer.checkerInterface.checkTypeII(oldInc, newOperations.get("inc"));

		Assertions.assertEquals("inc", result.get("inc"));

	}


	@Test
	public void integration_test() throws IOException, PrologTermNotDefinedException {

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "LiftWithLevels.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".")+1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsStringOld).loadIntoStateSpace(stateSpace);


		DynamicRenamingAnalyzer dynamicRenamingAnalyzer = new DynamicRenamingAnalyzer(Collections.emptySet(), Collections.emptyMap(), false,
				pathAsStringOld, pathAsString, injector, stateSpace.getLoadedMachine().getOperations());

		Set<String> candidates = new HashSet<>(Arrays.asList("inc", "dec", "getfloors", "$initialise_machine"));
		Map<String, CompoundPrologTerm> newOperations = dynamicRenamingAnalyzer.getOperations(pathAsString);
		Map<String, CompoundPrologTerm> oldOperations = dynamicRenamingAnalyzer.getOperations(pathAsStringOld);


		Map<String, Map<String, String>> result = DynamicRenamingAnalyzer.checkDeterministicPairs(oldOperations, newOperations,
				candidates, dynamicRenamingAnalyzer.checkerInterface, dynamicRenamingAnalyzer.prepareOperationsInterface);

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

		Assertions.assertEquals(expected, result);

	}


	@Test
	public void deltaFinder_initialisation_test() throws IOException, PrologTermNotDefinedException, DeltaCalculationException {
		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift", "LiftWithLevels.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".")+1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsStringOld).loadIntoStateSpace(stateSpace);


		DynamicRenamingAnalyzer dynamicRenamingAnalyzer = new DynamicRenamingAnalyzer(Collections.emptySet(), Collections.emptyMap(), true,
				pathAsStringOld, pathAsString, injector, stateSpace.getLoadedMachine().getOperations());


		dynamicRenamingAnalyzer.calculateDelta();

		Map<String, String> result = dynamicRenamingAnalyzer.getResultTypeIIInit();

		Map<String, String> expected = new HashMap<>();
		expected.put("floors", "level");

		Assertions.assertEquals(expected, result);

	}



	@Test
	public void deltaFinder_correction_of_the_categorization() throws IOException, PrologTermNotDefinedException, DeltaCalculationException {

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto2.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".")+1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsStringOld).loadIntoStateSpace(stateSpace);


		Map<String, Set<String>> typeIICandidates = new HashMap<>();
		typeIICandidates.put("getfloors", singleton("getlevels"));

		DynamicRenamingAnalyzer dynamicRenamingAnalyzer = new DynamicRenamingAnalyzer(Collections.emptySet(), typeIICandidates, true,
				pathAsStringOld, pathAsString, injector, stateSpace.getLoadedMachine().getOperations());


		dynamicRenamingAnalyzer.calculateDelta();

		Map<String, Map<String, String>> result = dynamicRenamingAnalyzer.getResultTypeII();

		Map<String, Map<String, String>> expected = new HashMap<>();
		Map<String, String> helper = new HashMap<>();
		helper.put("getfloors", "getlevels");
		helper.put("out", "out");
		helper.put("floors", "levels");
		expected.put("getfloors", helper);
		Assertions.assertEquals(expected, result);

	}


	@Test
	public void deltaFinder_correction_refinement() throws IOException, PrologTermNotDefinedException, DeltaCalculationException {

		//resources/de/prob/testmachines/traces/refinements/TrafficLightRef.ref
		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "TrafficLightRef.ref");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();



		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "TrafficLight.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".")+1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsString).loadIntoStateSpace(stateSpace);

		Set<String> typeIorIICandidates = new HashSet<>();
		typeIorIICandidates.add("set_peds_go");
		typeIorIICandidates.add("set_peds_stop");
		typeIorIICandidates.add("set_cars");

		DynamicRenamingAnalyzer dynamicRenamingAnalyzer = new DynamicRenamingAnalyzer(typeIorIICandidates, emptyMap(), true,
				pathAsStringOld, pathAsString, injector, stateSpace.getLoadedMachine().getOperations());


		dynamicRenamingAnalyzer.calculateDelta();

		Map<String, Map<String, String>> result = dynamicRenamingAnalyzer.getResultTypeII();
		Map<String, String> initResult = dynamicRenamingAnalyzer.getResultTypeIIInit();

		Assertions.assertEquals(emptyMap(), result);
		Assertions.assertEquals(emptyMap(), initResult);

	}

	@Test
	public void deltaFinder_typeII_with_candidates() throws IOException, PrologTermNotDefinedException, DeltaCalculationException {

		//resources/de/prob/testmachines/traces/refinements/TrafficLightRef.ref
		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "complexExample", "PitmanController_v6.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();



		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "complexExample", "PitmanController_v6_v2.mch");
		String pathAsString = path.toAbsolutePath().toString();

		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".")+1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsStringOld).loadIntoStateSpace(stateSpace);

		Map<String, Set<String>> typeIorIICandidates = new HashMap<>();
		typeIorIICandidates.put("ENV_Turn_EngineOn",singleton("ENV_Turn_Engine_On") );
		typeIorIICandidates.put("ENV_Turn_EngineOff", singleton("ENV_Turn_Engine_Off"));

		DynamicRenamingAnalyzer dynamicRenamingAnalyzer = new DynamicRenamingAnalyzer(emptySet(), typeIorIICandidates, true,
				pathAsStringOld, pathAsString, injector, stateSpace.getLoadedMachine().getOperations());


		dynamicRenamingAnalyzer.calculateDelta();


		String expected1 = "ENV_Turn_Engine_Off";
		String expected2 = "ENV_Turn_Engine_On";
		Set<String> expected = new HashSet<>(Arrays.asList(expected1, expected2));

		List<RenamingDelta> expectedRenamingDelta = dynamicRenamingAnalyzer.getResultTypeIIAsDeltaList();
		Set<String> resultNames = expectedRenamingDelta.stream().map(RenamingDelta::getDeltaName).collect(Collectors.toSet());

		Assertions.assertEquals(2, expectedRenamingDelta.size());
		Assertions.assertEquals(expected, resultNames);

	}


}
