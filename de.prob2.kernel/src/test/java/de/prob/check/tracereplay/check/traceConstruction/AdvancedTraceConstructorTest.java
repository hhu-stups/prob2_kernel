package de.prob.check.tracereplay.check.traceConstruction;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AdvancedTraceConstructorTest {



	private static TraceManager traceManager;
	private static Api api;

	@BeforeAll
	static void beforeAll() {

		traceManager = CliTestCommon.getInjector().getInstance(TraceManager.class);
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void test_freestyle_replay() throws IOException, TraceConstructionError {
		StateSpace stateSpace = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND.mch").toString());

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND.prob2trace"));

		List<Transition> resultRaw = AdvancedTraceConstructor.constructTraceWithOptions(jsonFile.getTransitionList(), stateSpace, ReplayOptions.replayJustNames());

		List<PersistentTransition> resultPre = PersistentTransition.createFromList(resultRaw);

		List<String> result = resultPre.stream().map(PersistentTransition::getOperationName).collect(toList());
		List<String> expected = jsonFile.getTransitionList().stream().map(PersistentTransition::getOperationName).collect(toList());

		Assertions.assertEquals(expected, result);

		stateSpace.kill();
	}

	@Test
	public void test_freestyle_replay_fails() throws IOException {
		StateSpace stateSpace = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND.mch").toString());

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "one_time_intermediate_operation", "ISLAND.prob2trace"));

		PersistentTransition fakeTransition = new PersistentTransition("dummy", emptyMap(), emptyMap(), emptyMap(), emptySet(), emptyList(), emptyList(), "");
		List<PersistentTransition> persistentTransitions = new ArrayList<>(jsonFile.getTransitionList());
		persistentTransitions.add(fakeTransition);
		assertThrows(TraceConstructionError.class, () -> AdvancedTraceConstructor.constructTraceWithOptions(persistentTransitions, stateSpace, ReplayOptions.replayJustNames()));

		stateSpace.kill();
	}





}
