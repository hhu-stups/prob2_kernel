package de.prob.animator.domainobjects;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlantUmlVisualizationCommandTest {

	private static Api api;
	private StateSpace stateSpace;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@BeforeEach
	void beforeEach() throws Exception {
		String machine = Paths.get(PlantUmlVisualizationCommandTest.class.getResource("/de/prob/testmachines/b/Lift.mch").toURI()).toString();
		stateSpace = api.b_load(machine);
	}

	@AfterEach
	void afterEach() {
		stateSpace.kill();
	}

	@Test
	void testGetAll() {
		Trace trace = new Trace(stateSpace).execute(Transition.INITIALISE_MACHINE_NAME);

		List<PlantUmlVisualizationCommand> commands = PlantUmlVisualizationCommand.getAll(trace);
		Assertions.assertNotNull(commands);
	}

	@Test
	void testExecute() {
		Trace trace = new Trace(stateSpace)
				              .execute(Transition.INITIALISE_MACHINE_NAME)
				              .execute("inc")
				              .execute("inc")
				              .execute("dec")
				              .execute("inc");

		PlantUmlVisualizationCommand command = PlantUmlVisualizationCommand.getByName(PlantUmlVisualizationCommand.SEQUENCE_CHART, trace);
		String puml = command.visualizeAsPlantUmlToString(Collections.emptyList());

		Assertions.assertNotNull(puml);
		Assertions.assertFalse(puml.isEmpty());
	}
}
