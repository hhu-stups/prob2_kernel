package de.prob.animator.command;


import de.be4.ltl.core.parser.LtlParseException;
import de.prob.ProBKernelStub;
import de.prob.animator.domainobjects.CTL;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;


class CtlCheckingCommandTest {
	private static ProBKernelStub stub;

	@BeforeAll
	static void beforeAll() {
		stub = CliTestCommon.getInjector().getInstance(ProBKernelStub.class);
	}

	@AfterAll
	static void afterAll() {
		stub.killCurrentAnimator();
	}

	@Test
	void empty_formula() throws URISyntaxException, IOException {
		Path path = Paths.get(CtlCheckingCommand.class.getClassLoader()
				.getResource("de/prob/testmachines/b/Lift.mch")
				.toURI());
		stub.createStateSpace(path);
		Assertions.assertThrows(LtlParseException.class, () -> new CTL(""));
	}

	@Test
	void lift_1() throws URISyntaxException, IOException, LtlParseException {
		Path path = Paths.get(CtlCheckingCommand.class.getClassLoader()
				.getResource("de/prob/testmachines/b/Lift.mch")
				.toURI());
		StateSpace stateSpace = stub.createStateSpace(path);
		CTL ctl = new CTL("EF {floors = 100}");
		CtlCheckingCommand cmd = new CtlCheckingCommand(stateSpace, ctl, 1000);
		stateSpace.execute(cmd);
	}

	@Test
	void lift_2() throws URISyntaxException, IOException, LtlParseException {
		Path path = Paths.get(CtlCheckingCommand.class.getClassLoader()
				.getResource("de/prob/testmachines/b/Lift.mch")
				.toURI());
		StateSpace stateSpace = stub.createStateSpace(path);
		CTL ctl = new CTL("EF {floors = 101}");
		CtlCheckingCommand cmd = new CtlCheckingCommand(stateSpace, ctl, 1000);
		stateSpace.execute(cmd);
	}

	@Test
	void lift_3() throws URISyntaxException, IOException, LtlParseException {
		Path path = Paths.get(CtlCheckingCommand.class.getClassLoader()
				.getResource("de/prob/testmachines/b/Lift.mch")
				.toURI());
		StateSpace stateSpace = stub.createStateSpace(path);
		CTL ctl = new CTL("AF {floors = 2}");
		CtlCheckingCommand cmd = new CtlCheckingCommand(stateSpace, ctl, 1000);
		stateSpace.execute(cmd);
	}
}
