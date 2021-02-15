package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.ReplayOptions;
import de.prob.check.tracereplay.check.TraceChecker;
import de.prob.check.tracereplay.check.TraceModifier;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class TraceCheckerIntegrationTest {


	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;
	Injector injector = null;

	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null && proBKernelStub==null && injector == null) {
			injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
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
	public void integration_1_realWorldExample() throws IOException, ModelTranslationError {
		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift", "changedTypeIIandTypeIII", "LiftProto2.mch");
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift", "changedTypeIIandTypeIII", "LiftProto2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));

		TraceChecker traceChecker = new TraceChecker(
				jsonFile.getTrace().getTransitionList(),
				jsonFile.getMachineOperationInfos(),
				stateSpace.getLoadedMachine().getOperations(),
				new HashSet<>(jsonFile.getVariableNames()),
				new HashSet<>(stateSpace.getLoadedMachine().getVariableNames()),
				newPath.toString(),
				injector,
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory());

		TraceModifier traceModifier = traceChecker.getTraceModifier();

		Assertions.assertEquals(0, traceModifier.getSizeTypeDetII());
	}

	@Test
	public void integration_2_realWorldExample() throws IOException, ModelTranslationError {
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
				injector,
				new TestUtils.StubFactoryImplementation(),
				new ReplayOptions(),
				new TestUtils.ProgressStubFactory()
		);

		TraceModifier traceModifier = traceChecker.getTraceModifier();


		Assertions.assertEquals(0, traceModifier.getSizeTypeDetII());


	}
	


}
