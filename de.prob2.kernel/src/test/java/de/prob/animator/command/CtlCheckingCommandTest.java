package de.prob.animator.command;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.be4.ltl.core.parser.LtlParseException;
import de.prob.animator.domainobjects.CTL;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CtlCheckingCommandTest {
	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	void empty_formula() {
		Assertions.assertThrows(LtlParseException.class, () -> new CTL(""));
	}

	@Test
	void lift_1() throws URISyntaxException, IOException, LtlParseException {
		Path path = Paths.get(CtlCheckingCommand.class.getClassLoader()
				.getResource("de/prob/testmachines/b/Lift.mch")
				.toURI());
		StateSpace stateSpace = api.b_load(path.toString());
		CTL ctl = new CTL("EF {floors = 100}");
		CtlCheckingCommand cmd = new CtlCheckingCommand(stateSpace, ctl, 1000);
		stateSpace.execute(cmd);
		stateSpace.kill();
	}

	@Test
	void lift_2() throws URISyntaxException, IOException, LtlParseException {
		Path path = Paths.get(CtlCheckingCommand.class.getClassLoader()
				.getResource("de/prob/testmachines/b/Lift.mch")
				.toURI());
		StateSpace stateSpace = api.b_load(path.toString());
		CTL ctl = new CTL("EF {floors = 101}");
		CtlCheckingCommand cmd = new CtlCheckingCommand(stateSpace, ctl, 1000);
		stateSpace.execute(cmd);
		stateSpace.kill();
	}

	@Test
	void lift_3() throws URISyntaxException, IOException, LtlParseException {
		Path path = Paths.get(CtlCheckingCommand.class.getClassLoader()
				.getResource("de/prob/testmachines/b/Lift.mch")
				.toURI());
		StateSpace stateSpace = api.b_load(path.toString());
		CTL ctl = new CTL("AF {floors = 2}");
		CtlCheckingCommand cmd = new CtlCheckingCommand(stateSpace, ctl, 1000);
		stateSpace.execute(cmd);
		stateSpace.kill();
	}

	@Test
	void lift_4() throws URISyntaxException, IOException, LtlParseException {
		Path path = Paths.get(CtlCheckingCommand.class.getClassLoader()
				.getResource("de/prob/testmachines/b/Lift.mch")
				.toURI());
		StateSpace stateSpace = api.b_load(path.toString());
		CTL ctl = new CTL("AF EG{1 = 2}");
		CtlCheckingCommand cmd = new CtlCheckingCommand(stateSpace, ctl, 1000);
		stateSpace.execute(cmd);
		stateSpace.kill();
	}
}
