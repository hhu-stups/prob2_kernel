package de.prob.animator.command;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.ComputationNotCompletedResult;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CbcSolveCommandTest {
	private static Api api;
	private static StateSpace stateSpace;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@BeforeEach
	void beforeEach() throws IOException {
		String example_mch = NQPrimePredicateCommandTest.class.getClassLoader()
				.getResource("de/prob/testmachines/b/VariablesOnly.mch")
				.getFile();
		stateSpace = api.b_load(example_mch);
	}

	@Test
	void should_solve_trivial_predicate() {
		// This test was written to ensure that the switch to the timed version
		// of the call was correct and not introducing any Prolog-errors.
		String predicate = "1=1";

		ClassicalB pred = new ClassicalB(predicate, FormulaExpand.EXPAND);

		CbcSolveCommand cmd = new CbcSolveCommand(pred);
		stateSpace.execute(cmd);

		AbstractEvalResult expected = EvalResult.TRUE;
		AbstractEvalResult actual = cmd.getValue();

		assertEquals(expected, actual);
	}

	@Test
	void should_extract_solving_time() {
		String predicate = "1=1";

		ClassicalB pred = new ClassicalB(predicate, FormulaExpand.EXPAND);

		CbcSolveCommand cmd = new CbcSolveCommand(pred);
		stateSpace.execute(cmd);

		assertNotNull(cmd.getMilliSeconds());
	}

	@Test
	void should_extract_solving_time_when_false_predicate() {
		String predicate = "1=2";

		ClassicalB pred = new ClassicalB(predicate, FormulaExpand.EXPAND);

		CbcSolveCommand cmd = new CbcSolveCommand(pred);
		stateSpace.execute(cmd);

		assertNotNull(cmd.getMilliSeconds());
	}

}
