package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.*;
import de.prob.check.tracereplay.check.exceptions.PrologTermNotDefinedException;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

public class TraceCheckerTest {

	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null && proBKernelStub==null) {
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}

	}

	@AfterEach
	public void cleanUp(){
		proBKernelStub.killCurrentAnimator();
	}


	@Test
	public void integration_short_constructor() throws IOException, ModelTranslationError {


		StateSpace stateSpace1 = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto.mch"));
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto2.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));


		
		TraceChecker traceChecker = new TraceChecker(jsonFile.getTrace().getTransitionList(),
				oldInfos,
				newInfos,
				new HashSet<>(oldVars),
				new HashSet<>(newVars), 
				newPath.toString(),
				Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule()),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());


		TraceModifier modifer = traceChecker.getTraceModifier();

		Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> result = modifer.getChangelogPhase3II();
		
		/*
		Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result = new TraceExplorer(false, new TestUtils.StubFactoryImplementation(), new TestUtils.ProgressStubFactory())
				.replayTrace(
						jsonFile.getTrace().getTransitionList(),
						stateSpace2,
						newInfos,
						oldInfos,
						Arrays.asList("inc", "dec", "getfloors"),
						emptySet());
*/
		/*
		
	
		 
		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> resultCleaned = TraceExplorer.removeHelperVariableMappings(result);

		Set<Map<String, String>> resultToCompare = resultCleaned.keySet().stream().flatMap(entry -> entry.values().stream()).collect(toSet());

		System.out.println(resultToCompare);
*/
		/*
		Set<Map<String, String>> expectedHelper = TraceCheckerUtils.allDiagonals(Arrays.asList("x", "y"), Arrays.asList("x", "y", "z"));
		Set<Map<String, String>> expected = expectedHelper.stream().peek(entry -> entry.put("floors", "levels")).collect(toSet());

		Assertions.assertEquals(expected, resultToCompare);

 */
	}


	@Test
	public void integration_long_constructor() throws IOException, ModelTranslationError, PrologTermNotDefinedException {


		Path oldPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto.mch");
		StateSpace stateSpace1 = proBKernelStub.createStateSpace(oldPath);
		Map<String, OperationInfo> oldInfos = stateSpace1.getLoadedMachine().getOperations();
		List<String> oldVars = stateSpace1.getLoadedMachine().getVariableNames();

		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII",  "LiftProto2.mch");
		StateSpace stateSpace2 = proBKernelStub.createStateSpace(newPath);
		Map<String, OperationInfo> newInfos = stateSpace2.getLoadedMachine().getOperations();
		List<String> newVars = stateSpace2.getLoadedMachine().getVariableNames();

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));



		TraceChecker traceChecker = new TraceChecker(jsonFile.getTrace().getTransitionList(),
				oldInfos,
				newInfos,
				new HashSet<>(oldVars),
				new HashSet<>(newVars),
				oldPath.toString(),
				newPath.toString(),
				Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule()),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());


		TraceModifier modifier = traceChecker.getTraceModifier();


		Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> result = modifier.getChangelogPhase3II();


		Assertions.assertEquals(2, modifier.getChangelogPhase2().size());
		Assertions.assertEquals(12, modifier.getSizeTypeIII());

	}
}
