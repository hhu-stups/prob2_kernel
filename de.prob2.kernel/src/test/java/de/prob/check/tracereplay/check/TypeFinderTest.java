package de.prob.check.tracereplay.check;

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

import com.google.inject.Injector;

import de.prob.ProBKernelStub;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.check.TypeFinder;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static de.prob.statespace.OperationInfo.Type.CLASSICAL_B;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

@Deprecated
public class TypeFinderTest {
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
	public void findCandidates_test_3_candidates(){
		List<String> inputParas = Arrays.asList("a", "b", "c");
		List<String> outputParas = Arrays.asList("d", "f", "g");
		List<String> detModifiedVars = Collections.singletonList("x");
		List<String> nonDetModifiedVars = Collections.emptyList();
		List<String> readVariables = Collections.emptyList();

		List<String> inputParas2 = Arrays.asList("a", "c");
		List<String> outputParas2 = Arrays.asList("d", "f", "g", "h");
		List<String> detModifiedVars2 = Arrays.asList("i", "o");
		List<String> nonDetModifiedVars2 = Collections.singletonList("j");
		List<String> readVariables2 = Collections.singletonList("p");


		// Expected
		OperationInfo operationInfo1 = new OperationInfo("inc", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo2 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo3 = new OperationInfo("inc2", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		//Unexpected
		OperationInfo operationInfo4 = new OperationInfo("inc3", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo5 = new OperationInfo("inc4", inputParas, outputParas2, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());


		OperationInfo operationInfo6 = new OperationInfo("inc5", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars, emptyMap());


		OperationInfo operationInfo8 = new OperationInfo("inc7", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars2, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo9 = new OperationInfo("inc8", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars2, emptyMap());

		Map<String, OperationInfo> operations = new HashMap<>();
		operations.put(operationInfo1.getOperationName(), operationInfo1);
		operations.put(operationInfo2.getOperationName(), operationInfo2);
		operations.put(operationInfo3.getOperationName(), operationInfo3);
		operations.put(operationInfo4.getOperationName(), operationInfo4);
		operations.put(operationInfo5.getOperationName(), operationInfo5);
		operations.put(operationInfo6.getOperationName(), operationInfo6);
		operations.put(operationInfo8.getOperationName(), operationInfo8);
		operations.put(operationInfo9.getOperationName(), operationInfo9);

		Set<String> expectedResult = new HashSet<>();
		expectedResult.add(operationInfo1.getOperationName());
		expectedResult.add(operationInfo2.getOperationName());
		expectedResult.add(operationInfo3.getOperationName());

		Set<String> candidates = TypeFinder.findCandidates(inputParas.size(), outputParas.size(), detModifiedVars.size(),
				0, 0, operations);


		Assertions.assertEquals(expectedResult, candidates);
	}

	@Test
	public void checkIfOperationCandidatesFulfillSuperficialCriteriaForBeingACloneOrRenamed_test(){
		Set<String> functionNamesInQuestion = new HashSet<>(Arrays.asList("inc", "inc2", "inc1"));

		List<String> inputParas = Arrays.asList("a", "b", "c");
		List<String> outputParas = Arrays.asList("d", "f", "g");
		List<String> detModifiedVars = Collections.singletonList("x");
		List<String> nonDetModifiedVars = Collections.emptyList();
		List<String> readVariables = Collections.emptyList();

		List<String> inputParas2 = Arrays.asList("a", "c");
		List<String> outputParas2 = Arrays.asList("d", "f", "g", "h");
		List<String> detModifiedVars2 = Arrays.asList("i", "o");
		List<String> nonDetModifiedVars2 = Collections.singletonList("j");
		List<String> readVariables2 = Collections.singletonList("p");

		// Expected
		OperationInfo operationInfo1 = new OperationInfo("inc", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo2 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo3 = new OperationInfo("inc2", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars, emptyMap());

		//Unexpected
		OperationInfo operationInfo4 = new OperationInfo("inc3", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo5 = new OperationInfo("inc4", inputParas, outputParas2, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());



		OperationInfo operationInfo8 = new OperationInfo("inc7", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars2, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo9 = new OperationInfo("inc8", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars2, emptyMap());

		Map<String, OperationInfo> oldOperations = new HashMap<>();
		oldOperations.put(operationInfo1.getOperationName(), operationInfo1);
		oldOperations.put(operationInfo2.getOperationName(), operationInfo2);
		oldOperations.put(operationInfo3.getOperationName(), operationInfo3);


		Map<String, OperationInfo> newOperations = new HashMap<>();
		newOperations.put(operationInfo1.getOperationName(), operationInfo1);
		newOperations.put(operationInfo2.getOperationName(), operationInfo2);
		newOperations.put(operationInfo4.getOperationName(), operationInfo4);
		newOperations.put(operationInfo5.getOperationName(), operationInfo5);
		newOperations.put(operationInfo8.getOperationName(), operationInfo8);
		newOperations.put(operationInfo9.getOperationName(), operationInfo9);


		Set<String> expectedInner = new HashSet<>(Arrays.asList(operationInfo1.getOperationName(), operationInfo2.getOperationName()));
		Map<String, Set<String>> expected = new HashMap<>();
		expected.put(operationInfo1.getOperationName(), expectedInner);
		expected.put(operationInfo2.getOperationName(), expectedInner);

		Map<String, Set<String>> results = TypeFinder.checkIfOperationCandidatesFulfillSuperficialCriteriaForBeingACloneOrRenamed(functionNamesInQuestion, oldOperations, newOperations);

		Assertions.assertEquals(expected, results);

	}


	@Test
	public void findOperationsWithSameParameterLength_test(){

		Set<String> functionNamesInQuestion = new HashSet<>(Arrays.asList("inc", "inc2", "inc1"));

		List<String> inputParas = Arrays.asList("a", "b", "c");
		List<String> outputParas = Arrays.asList("d", "f", "g");
		List<String> detModifiedVars = Collections.singletonList("x");
		List<String> nonDetModifiedVars = Collections.emptyList();
		List<String> readVariables = Collections.emptyList();

		List<String> inputParas2 = Arrays.asList("a", "c");
		List<String> readVariables2 = Collections.singletonList("p");

		// Expected
		OperationInfo operationInfo1 = new OperationInfo("inc", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo2 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo3 = new OperationInfo("inc2", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars, emptyMap());

		//Unexpected
		OperationInfo operationInfo4 = new OperationInfo("inc2", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());


		OperationInfo operationInfo9 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());


		Map<String, OperationInfo> oldOperations = new HashMap<>();
		oldOperations.put(operationInfo1.getOperationName(), operationInfo1);
		oldOperations.put(operationInfo2.getOperationName(), operationInfo2);
		oldOperations.put(operationInfo3.getOperationName(), operationInfo3);


		Map<String, OperationInfo> newOperations = new HashMap<>();
		newOperations.put(operationInfo1.getOperationName(), operationInfo1);
		newOperations.put(operationInfo9.getOperationName(), operationInfo2);
		newOperations.put(operationInfo4.getOperationName(), operationInfo4);

		Set<String> result = TypeFinder.findOperationsWithSameParameterLength(functionNamesInQuestion, oldOperations, newOperations);

		Set<String> expected = new HashSet<>(Arrays.asList("inc", "inc1"));

		Assertions.assertEquals(expected, result);

	}


	@Test
	public void usedOperation_test() throws IOException {

		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine10Steps.prob2trace"));

		Set<String> result = TraceCheckerUtils.usedOperations(bla.getTransitionList());

		Set<String> expected = new HashSet<>(Arrays.asList("dec", "getfloors", "inc", Transition.INITIALISE_MACHINE_NAME));

		Assertions.assertEquals(expected, result);

	}

	@Test
	public void check_no_type_II_permutation_test() throws IOException {

		List<String> inputParas = Collections.emptyList();
		List<String> outputParas = Collections.emptyList();
		List<String> detModifiedVars = Collections.singletonList("x");
		List<String> nonDetModifiedVars = Collections.emptyList();
		List<String> readVariables = Collections.emptyList();

		List<String> inputParas2 = Arrays.asList("a", "c");
		List<String> readVariables2 = Collections.singletonList("p");

		// Expected
		OperationInfo operationInfo1 = new OperationInfo("inc", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo2 = new OperationInfo("dec", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo3 = new OperationInfo("getFloor", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars, emptyMap());


		Map<String, OperationInfo> oldOperations = new HashMap<>();
		oldOperations.put(operationInfo1.getOperationName(), operationInfo1);
		oldOperations.put(operationInfo2.getOperationName(), operationInfo2);
		oldOperations.put(operationInfo3.getOperationName(), operationInfo3);

		OperationInfo operationInfo4 = new OperationInfo("inc2", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo5 = new OperationInfo("dec", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());


		OperationInfo operationInfo9 = new OperationInfo("inc", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());


		Map<String, OperationInfo> newOperations = new HashMap<>();
		newOperations.put(operationInfo4.getOperationName(), operationInfo4);
		newOperations.put(operationInfo5.getOperationName(), operationInfo5);
		newOperations.put(operationInfo9.getOperationName(), operationInfo9);


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine10Steps.prob2trace"));


		TypeFinder typeFinder = new TypeFinder(bla.getTransitionList(), oldOperations, newOperations, Collections.emptySet(), Collections.emptySet());

		typeFinder.check();


		Set<String> typeI = new HashSet<>(Collections.singletonList("inc"));
		Set<String> typeIII = new HashSet<>(Collections.singletonList("dec"));
		Set<String> typeIV = new HashSet<>(Collections.singletonList("getfloors"));


		Assertions.assertEquals(typeI, typeFinder.getTypeIorII());
		Assertions.assertEquals(typeIII, typeFinder.getTypeIII());
		Assertions.assertEquals(typeIV, typeFinder.getTypeIV());
	}


	@Test
	public void check_typ_II_permutation_test() throws IOException {

		List<String> inputParas = Collections.emptyList();
		List<String> outputParas = Collections.emptyList();
		List<String> detModifiedVars = Collections.singletonList("x");
		List<String> nonDetModifiedVars = Collections.emptyList();
		List<String> readVariables = Collections.emptyList();

		List<String> inputParas2 = Arrays.asList("a", "c");
		List<String> readVariables2 = Collections.singletonList("p");

		// Expected
		OperationInfo operationInfo1 = new OperationInfo("inc", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo2 = new OperationInfo("dec", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo3 = new OperationInfo("getFloor", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars, emptyMap());


		Map<String, OperationInfo> oldOperations = new HashMap<>();
		oldOperations.put(operationInfo1.getOperationName(), operationInfo1);
		oldOperations.put(operationInfo2.getOperationName(), operationInfo2);
		oldOperations.put(operationInfo3.getOperationName(), operationInfo3);

		OperationInfo operationInfo4 = new OperationInfo("inc2", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo5 = new OperationInfo("dec", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());

		OperationInfo operationInfo9 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars, emptyMap());


		Map<String, OperationInfo> newOperations = new HashMap<>();
		newOperations.put(operationInfo4.getOperationName(), operationInfo4);
		newOperations.put(operationInfo5.getOperationName(), operationInfo5);
		newOperations.put(operationInfo9.getOperationName(), operationInfo9);


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine10Steps.prob2trace"));


		TypeFinder typeFinder = new TypeFinder(bla.getTransitionList(), oldOperations, newOperations, Collections.emptySet(), Collections.emptySet());

		typeFinder.check();


		Map<String,Set<String>> typeII_per = Collections.singletonMap("inc", singleton("inc1"));

		Assertions.assertEquals(typeII_per, typeFinder.getTypeIIPermutation());

	}


	@Test
	public void check_init_type_test() throws IOException {

		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines","traces", "testTraceMachine10Steps.prob2trace"));

		Set<String> oldVars = singleton("world");

		TypeFinder typeFinder = new TypeFinder(bla.getTransitionList(), Collections.emptyMap(), Collections.emptyMap(), oldVars, Collections.emptySet());

		typeFinder.check();

		Assertions.assertFalse(typeFinder.getInitIsTypeIorIICandidate());
	}


	@Test
	public void check_init_type_test_2() throws IOException {


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine10Steps.prob2trace"));


		Set<String> oldVars = singleton("world");

		Set<String> newVars = singleton("hallo");

		TypeFinder typeFinder = new TypeFinder(bla.getTransitionList(), Collections.emptyMap(), Collections.emptyMap(), oldVars, newVars);

		typeFinder.check();



		Assertions.assertTrue(typeFinder.getInitIsTypeIorIICandidate());

	}

	@Test
	public void lift_with_no_old_file() throws IOException {



		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto2.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));


		TypeFinder typeFinder = new TypeFinder(jsonFile.getTransitionList(), jsonFile.getMachineOperationInfos(), newInfos, new HashSet<>(jsonFile.getVariableNames()), new HashSet<>(newVars));


		typeFinder.check();


		Set<String> expected2 = new HashSet<>();
		expected2.add("getfloors");
		Set<String> expected3 = singleton("inc");


		Assertions.assertEquals(expected2, typeFinder.getTypeIIPermutation().keySet());
		Assertions.assertEquals(expected3, typeFinder.getTypeIII());
	}

	@Test
	public void wrong_variable_count_test() throws IOException {

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND2.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND.prob2trace");
		TraceJsonFile traceJsonFile = CliTestCommon.getInjector().getInstance(TraceManager.class).load(path);


		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".") + 1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsStringOld).loadIntoStateSpace(stateSpace);

		TypeFinder typeFinder = new TypeFinder(traceJsonFile.getTransitionList(), traceJsonFile.getMachineOperationInfos(), stateSpace.getLoadedMachine().getOperations(), new HashSet<>(traceJsonFile.getVariableNames()), new HashSet<>(stateSpace.getLoadedMachine().getVariableNames()));

		typeFinder.check();
		Assertions.assertEquals("off", typeFinder.getTypeIorII().stream().findFirst().get());


	}
}
