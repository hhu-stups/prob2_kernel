package de.prob.animator.command;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import de.prob.statespace.SyncedTraces;
import de.prob.statespace.Transition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class ConsturctTraceCommandTest {



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
	public void test() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "ABZ.mch"));

		ConstructTraceCommand constructTraceCommand = new ConstructTraceCommand(stateSpace,
				stateSpace.getRoot(),
				Arrays.asList(Transition.SETUP_CONSTANTS_NAME, Transition.INITIALISE_MACHINE_NAME),
				Arrays.asList(new ClassicalB("1=1", FormulaExpand.EXPAND), new ClassicalB("1=1", FormulaExpand.EXPAND)));

		stateSpace.execute(constructTraceCommand);

		System.out.println(constructTraceCommand.getErrors());

		System.out.println(constructTraceCommand.getTrace(stateSpace).getTransitionList());

	}
	


}
