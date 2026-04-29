package de.prob.animator.command;

import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.Language;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetAnimationModeCommandTest {

	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void classicalB() throws IOException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b",  "Lift.mch");
		StateSpace stateSpace = api.b_load(file.toString());
		GetAnimationModeCommand cmd = new GetAnimationModeCommand();
		stateSpace.execute(cmd);
		Assertions.assertEquals(Language.CLASSICAL_B, cmd.getLanguage());
		stateSpace.kill();
	}

	@Test
	public void eventB() throws IOException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "trafficLight",  "mac1.bum");
		StateSpace stateSpace = api.eventb_load(file.toString());
		GetAnimationModeCommand cmd = new GetAnimationModeCommand();
		stateSpace.execute(cmd);
		Assertions.assertEquals(Language.EVENT_B, cmd.getLanguage());
		stateSpace.kill();
	}

	@Test
	public void rulesDSL() throws IOException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "brules",  "SimpleRulesMachine.rmch");
		StateSpace stateSpace = api.brules_load(file.toString());
		GetAnimationModeCommand cmd = new GetAnimationModeCommand();
		stateSpace.execute(cmd);
		Assertions.assertEquals(Language.B_RULES, cmd.getLanguage());
		stateSpace.kill();
	}

	@Test
	public void tla() throws IOException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "tla",  "Foo.tla");
		StateSpace stateSpace = api.tla_load(file.toString());
		GetAnimationModeCommand cmd = new GetAnimationModeCommand();
		stateSpace.execute(cmd);
		Assertions.assertEquals(Language.TLA, cmd.getLanguage());
		stateSpace.kill();
	}

}
