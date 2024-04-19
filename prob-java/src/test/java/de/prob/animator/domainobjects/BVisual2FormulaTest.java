package de.prob.animator.domainobjects;

import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BVisual2FormulaTest {

	private static Api api;
	private StateSpace stateSpace;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@BeforeEach
	void beforeEach() throws Exception {
		String machine = Paths.get(BVisual2Formula.class.getResource("/de/prob/testmachines/b/LongString.mch").toURI()).toString();
		stateSpace = api.b_load(machine);
	}

	@AfterEach
	void afterEach() {
		stateSpace.kill();
	}

	@Test
	void testLongStringEvaluate() {
		String expected = "\"" + IntStream.range(0, 100).mapToObj(i -> "1234567890").collect(Collectors.joining()) + "\"";

		State init = stateSpace.getRoot().perform(Transition.INITIALISE_MACHINE_NAME);
		BVisual2Formula s = BVisual2Formula.fromFormula(stateSpace, new ClassicalB("s"));
		String result = s.evaluateUnlimited(init).toString();
		Assertions.assertEquals(expected, result);
	}

	@Test
	void testUnlimitedLongStringEvaluate() {
		String expected = "\"" + IntStream.range(0, 100).mapToObj(i -> "1234567890").collect(Collectors.joining()) + "\"";

		State init = stateSpace.getRoot().perform(Transition.INITIALISE_MACHINE_NAME);
		BVisual2Formula s = BVisual2Formula.fromFormula(stateSpace, new ClassicalB("s"));
		String result = s.evaluate(init).toString();
		Assertions.assertTrue(result.length() < expected.length()); // truncation!
	}
}
