package de.prob.check;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.github.krukow.clj_lang.PersistentVector;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.PersistenceDelta;
import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TraceExplorerTest {


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
	public void integration_1() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift.mch"));

		PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> result =
				TraceExplorer.replayTrace(Collections.emptyList(), stateSpace, stateSpace.getLoadedMachine().getOperations());

		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void integration_2_initialisation_clause() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> result =
				TraceExplorer.replayTrace(Collections.singletonList(init), stateSpace, stateSpace.getLoadedMachine().getOperations());


		PersistenceDelta expected = result.entrySet().stream().findFirst().get().getValue().get(0);

		Assert.assertEquals(1, result.entrySet().size());
		Assert.assertEquals(expected.getOldTransition(), init);
		Assert.assertEquals(expected.getNewTransitions().get(0), init);
	}


	@Test
	public void integration_3_three_transitions_with_exact_match() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", Collections.emptyMap(),
				Collections.emptyMap(), Collections.singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		PersistenceDelta delta1 = new PersistenceDelta(init, Collections.singletonList(init));
		PersistenceDelta delta2 = new PersistenceDelta(first, Collections.singletonList(first));
		PersistenceDelta delta3 = new PersistenceDelta(second, Collections.singletonList(second));

		List<PersistenceDelta> expected = Arrays.asList(delta1,delta2,delta3);


		PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> result =
				TraceExplorer.replayTrace(Arrays.asList(init, first, second), stateSpace, stateSpace.getLoadedMachine().getOperations());



		Assert.assertEquals(expected, new ArrayList<>(result.entrySet().stream().findFirst().get().getValue()));
	}


	@Test
	public void integration_4_three_transitions_with_1_change_ignore_init() throws IOException, ModelTranslationError {


		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",  "Lift2.mch"));

		PersistentTransition init = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition first = new PersistentTransition("inc", Collections.emptyMap(),
				Collections.emptyMap(), Collections.singletonMap("floors", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition second = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), Collections.singletonMap("floors", "0"), Collections.emptySet(), Collections.emptyList());


		PersistentTransition initNew = new PersistentTransition(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(),
				Collections.emptyMap(), Collections.singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition firstNew = new PersistentTransition("inc", Collections.singletonMap("x", "0"),
				Collections.emptyMap(), Collections.singletonMap("levels", "1"), Collections.emptySet(), Collections.emptyList());

		PersistentTransition secondNew = new PersistentTransition("dec", Collections.emptyMap(),
				Collections.emptyMap(), Collections.singletonMap("levels", "0"), Collections.emptySet(), Collections.emptyList());


		PersistenceDelta delta1 = new PersistenceDelta(init, Collections.singletonList(initNew));
		PersistenceDelta delta2 = new PersistenceDelta(first, Collections.singletonList(firstNew));
		PersistenceDelta delta3 = new PersistenceDelta(second, Collections.singletonList(secondNew));

		List<PersistenceDelta> expected = Arrays.asList(delta1,delta2,delta3);

		PersistentHashMap<Trace, PersistentVector<PersistenceDelta>> result =
				TraceExplorer.replayTrace(Arrays.asList(init, first, second), stateSpace, stateSpace.getLoadedMachine().getOperations());



		Assert.assertEquals(expected, new ArrayList<>(result.entrySet().stream().findFirst().get().getValue()));
	}
}
