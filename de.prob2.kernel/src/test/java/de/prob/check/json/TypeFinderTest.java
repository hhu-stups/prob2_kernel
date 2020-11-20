package de.prob.check.json;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.TypeFinder;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.statespace.OperationInfo;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static de.prob.statespace.OperationInfo.Type.CLASSICAL_B;

public class TypeFinderTest {


	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null && proBKernelStub==null) {
			System.setProperty("prob.home", "/home/sebastian/prob_prolog");
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}

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
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo2 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo3 = new OperationInfo("inc2", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		//Unexpected
		OperationInfo operationInfo4 = new OperationInfo("inc3", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo5 = new OperationInfo("inc4", inputParas, outputParas2, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);


		OperationInfo operationInfo6 = new OperationInfo("inc5", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars);


		OperationInfo operationInfo8 = new OperationInfo("inc7", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars2, nonDetModifiedVars);

		OperationInfo operationInfo9 = new OperationInfo("inc8", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars2);

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


		Assert.assertEquals(expectedResult, candidates);
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
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo2 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo3 = new OperationInfo("inc2", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars);

		//Unexpected
		OperationInfo operationInfo4 = new OperationInfo("inc3", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo5 = new OperationInfo("inc4", inputParas, outputParas2, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);



		OperationInfo operationInfo8 = new OperationInfo("inc7", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars2, nonDetModifiedVars);

		OperationInfo operationInfo9 = new OperationInfo("inc8", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars2);

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


		Map<String, Set<String>> results = TypeFinder.checkIfOperationCandidatesFulfillSuperficialCriteriaForBeingACloneOrRenamed(functionNamesInQuestion, oldOperations, newOperations);
		System.out.println(results);
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
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo2 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo3 = new OperationInfo("inc2", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars);

		//Unexpected
		OperationInfo operationInfo4 = new OperationInfo("inc2", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);


		OperationInfo operationInfo9 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);


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

		Assert.assertEquals(expected, result);

	}


	@Test
	public void usedOperation_test() throws IOException {

		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "testTraceMachine10Steps.prob2trace"));

		Set<String> result = TypeFinder.usedOperations(bla.getTrace());

		Set<String> expected = new HashSet<>(Arrays.asList("dec", "getfloors", "inc"));

		Assert.assertEquals(expected, result);

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
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo2 = new OperationInfo("dec", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo3 = new OperationInfo("getFloor", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars);


		Map<String, OperationInfo> oldOperations = new HashMap<>();
		oldOperations.put(operationInfo1.getOperationName(), operationInfo1);
		oldOperations.put(operationInfo2.getOperationName(), operationInfo2);
		oldOperations.put(operationInfo3.getOperationName(), operationInfo3);

		OperationInfo operationInfo4 = new OperationInfo("inc2", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo5 = new OperationInfo("dec", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);


		OperationInfo operationInfo9 = new OperationInfo("inc", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);


		Map<String, OperationInfo> newOperations = new HashMap<>();
		newOperations.put(operationInfo4.getOperationName(), operationInfo4);
		newOperations.put(operationInfo5.getOperationName(), operationInfo5);
		newOperations.put(operationInfo9.getOperationName(), operationInfo9);


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "testTraceMachine10Steps.prob2trace"));


		TypeFinder typeFinder = new TypeFinder(bla.getTrace(), oldOperations, newOperations, Collections.emptySet(), Collections.emptySet());

		typeFinder.check();


		Set<String> typeI = new HashSet<>(Collections.singletonList("inc"));
		Set<String> typeIII = new HashSet<>(Collections.singletonList("dec"));
		Set<String> typeIV = new HashSet<>(Collections.singletonList("getfloors"));


		Assert.assertEquals(typeI, typeFinder.getTypeIorII());
		Assert.assertEquals(typeIII, typeFinder.getTypeIII());
		Assert.assertEquals(typeIV, typeFinder.getTypeIV());
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
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo2 = new OperationInfo("dec", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo3 = new OperationInfo("getFloor", inputParas, outputParas, true,
				CLASSICAL_B, readVariables2, detModifiedVars, nonDetModifiedVars);


		Map<String, OperationInfo> oldOperations = new HashMap<>();
		oldOperations.put(operationInfo1.getOperationName(), operationInfo1);
		oldOperations.put(operationInfo2.getOperationName(), operationInfo2);
		oldOperations.put(operationInfo3.getOperationName(), operationInfo3);

		OperationInfo operationInfo4 = new OperationInfo("inc2", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);

		OperationInfo operationInfo5 = new OperationInfo("dec", inputParas2, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);


		OperationInfo operationInfo9 = new OperationInfo("inc1", inputParas, outputParas, true,
				CLASSICAL_B, readVariables, detModifiedVars, nonDetModifiedVars);


		Map<String, OperationInfo> newOperations = new HashMap<>();
		newOperations.put(operationInfo4.getOperationName(), operationInfo4);
		newOperations.put(operationInfo5.getOperationName(), operationInfo5);
		newOperations.put(operationInfo9.getOperationName(), operationInfo9);


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "testTraceMachine10Steps.prob2trace"));


		TypeFinder typeFinder = new TypeFinder(bla.getTrace(), oldOperations, newOperations, Collections.emptySet(), Collections.emptySet());

		typeFinder.check();


		Map<String,Set<String>> typeII_per = Collections.singletonMap("inc", Collections.singleton("inc1"));

		Assert.assertEquals(typeII_per, typeFinder.getTypeIIPermutation());

	}


	@Test
	public void check_init_type_test() throws IOException {


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "testTraceMachine10Steps.prob2trace"));


		Set<String> oldVars = Collections.singleton("world");



		TypeFinder typeFinder = new TypeFinder(bla.getTrace(), Collections.emptyMap(), Collections.emptyMap(), oldVars, Collections.emptySet());

		typeFinder.check();



		Assert.assertFalse(typeFinder.getInitIsTypeIorIICandidate());

	}


	@Test
	public void check_init_type_test_2() throws IOException {


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "testTraceMachine10Steps.prob2trace"));


		Set<String> oldVars = Collections.singleton("world");

		Set<String> newVars = Collections.singleton("hallo");



		TypeFinder typeFinder = new TypeFinder(bla.getTrace(), Collections.emptyMap(), Collections.emptyMap(), oldVars, newVars);

		typeFinder.check();



		Assert.assertTrue(typeFinder.getInitIsTypeIorIICandidate());

	}
}
