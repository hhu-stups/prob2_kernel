package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.PersistenceDelta;
import de.prob.check.tracereplay.check.TraceChecker;
import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

public class TraceCheckerIntegrationTest {


	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;
	Injector injector = null;

	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null && proBKernelStub==null && injector == null) {
			System.setProperty("prob.home", "/home/sebastian/prob_prolog");
			injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}

	}



	@Test
	public void integration_1_realWorldExample() throws IOException, ModelTranslationError {
		Path newPath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "LiftProto2.mch");
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "LiftProto2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "LiftProto.prob2trace"));

		TraceChecker traceChecker = new TraceChecker(jsonFile.getTrace().getTransitionList(), stateSpace.getLoadedMachine().getOperations(),
				new HashSet<>(stateSpace.getLoadedMachine().getVariableNames()), newPath.toString(), injector);


		System.out.println(traceChecker.getTraceModifier().getChangelogPhase3II());
	}

}
