package de.prob.check;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.TraceChecker;
import de.prob.check.tracereplay.check.exceptions.PrologTermNotDefinedException;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TraceCheckerTest {

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
	void integration_test1() throws IOException, ModelTranslationError {
		TraceManager traceManager = new TraceManager(new ObjectMapper());
		Path machinePath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift.mch");
		Path machinePathNew = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "LiftWithLevels.mch");

		Path tracePath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift.prob2trace");


		TraceJsonFile traceJsonFile = traceManager.load(tracePath.toAbsolutePath());

		LoadedMachine old = proBKernelStub.load(machinePath);
		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());

/*
		TraceChecker traceChecker =
				new TraceChecker(traceJsonFile.getTrace(), old.getOperations(), traceJsonFile.getMachineOperationInfos(),
						new HashSet<>(old.getVariableNames()), new HashSet<>(traceJsonFile.getVariableNames()), 
						machinePath.toAbsolutePath().toString(), machinePathNew.toAbsolutePath().toString(), injector, );

		System.out.println(traceChecker.getTraceModifier().getLastChange().get(0));
		*/
 
	}


	@Test
	void integration_test3() throws IOException, ModelTranslationError, PrologTermNotDefinedException {
		TraceManager traceManager = new TraceManager(new ObjectMapper());
		Path machinePath = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "ABZ.mch");
		Path machinePathNew = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "ABZ.mch");

		Path tracePath = Paths.get("src", "test", "resources", "de", "prob",  "traces", "ABZ.prob2trace");


		TraceJsonFile traceJsonFile = traceManager.load(tracePath.toAbsolutePath());

		LoadedMachine old = proBKernelStub.load(machinePath);
		Map<String, OperationInfo> oldInfos = old.getOperations();
		List<String> oldVars = old.getVariableNames();
		proBKernelStub.killCurrent();
		LoadedMachine newOld = proBKernelStub.createStateSpace(machinePathNew).getLoadedMachine();
		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());


		TraceChecker traceChecker = new TraceChecker(
				traceJsonFile.getTrace().getTransitionList(),
				oldInfos,
				newOld.getOperations(),
				new HashSet<>(oldVars),
				new HashSet<>(newOld.getVariableNames()),
				new HashSet<>(newOld.getSetNames()),
				new HashSet<>(newOld.getConstantNames()),
				machinePath.toString(),
				machinePathNew.toString(),
				injector,
				new TestUtils.StubFactoryImplementation());


		System.out.println(traceChecker.getTraceModifier().getChangelogPhase3II());
	}

}
