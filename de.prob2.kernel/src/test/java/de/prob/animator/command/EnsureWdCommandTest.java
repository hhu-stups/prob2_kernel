package de.prob.animator.command;

import java.nio.file.Paths;

import java.io.IOException;
import java.net.URISyntaxException;

import de.prob.animator.domainobjects.ClassicalB;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class EnsureWdCommandTest {
	private static Api api;
	private static StateSpace stateSpace;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@BeforeEach
	void beforeEach() throws IOException, URISyntaxException {
		String example_mch = Paths.get(CbcSolveCommand.class
			.getResource("/de/prob/testmachines/b/VariablesOnly.mch")
			.toURI()).toString();
		stateSpace = api.b_load(example_mch);
	}

	@Test
	void shouldIntroduceWdCondition() {
		String predicate = "x / y = 2";
		ClassicalB pred = new ClassicalB(predicate);

		EnsureWdCommand cmd = new EnsureWdCommand(pred);
		stateSpace.execute(cmd);
		String actual = cmd.getWdPred();

		String expected = "y /= 0 & x / y = 2";

		assertEquals(expected, actual);
	}


	@Test
	void shouldNotDoubleWdCondition() {
		String predicate = "y /= 0 & x / y = 2";
		ClassicalB pred = new ClassicalB(predicate);

		EnsureWdCommand cmd = new EnsureWdCommand(pred);
		stateSpace.execute(cmd);
		String actual = cmd.getWdPred();

		String expected = "y /= 0 & x / y = 2";

		assertEquals(expected, actual);
	}
}
