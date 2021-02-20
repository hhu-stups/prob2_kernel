package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.*;
import de.prob.check.tracereplay.check.exceptions.DeltaCalculationException;
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
	public void integration_short_constructor() throws IOException, ModelTranslationError, DeltaCalculationException {


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


		TraceModifier modifier = traceChecker.getTraceModifier();

		Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> result = modifier.getChangelogPhase3II();



		Assertions.assertEquals(2, modifier.getChangelogPhase2().size());
		Assertions.assertEquals(12, modifier.getSizeTypeIII());

	}


	@Test
	public void integration_short_constructor_2() throws IOException, ModelTranslationError, DeltaCalculationException {
		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "tropical_island", "version_2", "Island2.mch");

		StateSpace stateSpace = proBKernelStub.createStateSpace(newPath);

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "typeIV", "tropical_island", "version_2", "Island2.prob2trace"));

		TraceChecker traceChecker = new TraceChecker(
				jsonFile.getTrace().getTransitionList(),
				jsonFile.getMachineOperationInfos(),
				stateSpace.getLoadedMachine().getOperations(),
				new HashSet<>(jsonFile.getVariableNames()),
				new HashSet<>(stateSpace.getLoadedMachine().getVariableNames()),
				newPath.toString(),
				Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule()),
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory()
		);


		TraceModifier modifier = traceChecker.getTraceModifier();

		Assertions.assertEquals(1, modifier.getChangelogPhase2().size());
		Assertions.assertEquals(1, modifier.getSizeTypeIII());


	}


	@Test
	public void integration_long_constructor() throws IOException, ModelTranslationError, PrologTermNotDefinedException, DeltaCalculationException {


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

		Assertions.assertEquals(2, modifier.getChangelogPhase2().size());
		Assertions.assertEquals(12, modifier.getSizeTypeIII());

	}



}
