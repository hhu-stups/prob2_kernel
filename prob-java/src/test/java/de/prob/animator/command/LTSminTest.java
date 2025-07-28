package de.prob.animator.command;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.prob.check.LTSminModelCheckingOptions;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("LTSmin is not installed by default")
class LTSminTest {
	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	void lift() throws Exception {
		Path path = Paths.get(LTSminTest.class
				.getResource("/de/prob/testmachines/b/Lift.mch")
				.toURI());
		StateSpace stateSpace = api.b_load(path.toString());
		stateSpace.addConsoleOutputListener(System.out::println);
		LTSminModelCheckingCommand cmd = new LTSminModelCheckingCommand(LTSminModelCheckingOptions.DEFAULT);
		stateSpace.execute(cmd);
		System.out.println(cmd.getResult());
		stateSpace.kill();
	}

	@Test
	void liftFail() throws Exception {
		Path path = Paths.get(LTSminTest.class
				.getResource("/de/prob/testmachines/b/LiftFail.mch")
				.toURI());
		StateSpace stateSpace = api.b_load(path.toString());
		stateSpace.addConsoleOutputListener(System.out::println);
		LTSminModelCheckingCommand cmd = new LTSminModelCheckingCommand(LTSminModelCheckingOptions.DEFAULT);
		stateSpace.execute(cmd);
		System.out.println(cmd.getResult());
		stateSpace.kill();
	}
}
