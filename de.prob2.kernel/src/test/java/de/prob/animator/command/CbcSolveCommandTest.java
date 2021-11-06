package de.prob.animator.command;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.ComputationNotCompletedResult;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		String example_mch = CbcSolveCommand.class.getClassLoader()
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


	@Test
	void should_solve_with_dpllt_solver() {
		String predicate = "1=1";

		ClassicalB pred = new ClassicalB(predicate, FormulaExpand.EXPAND);

		CbcSolveCommand cmd = new CbcSolveCommand(pred, CbcSolveCommand.Solvers.DPLLT);
		stateSpace.execute(cmd);

		AbstractEvalResult expected = EvalResult.TRUE;
		AbstractEvalResult actual = cmd.getValue();

		assertEquals(expected, actual);
	}


	@Test
	void should_get_free_variables_and_solution_when_solving_with_DPLLT() {
		String predicate = "x:INTEGER & y:INTEGER & x>y";

		ClassicalB pred = new ClassicalB(predicate, FormulaExpand.EXPAND);

		CbcSolveCommand cmd = new CbcSolveCommand(pred, CbcSolveCommand.Solvers.DPLLT);
		stateSpace.execute(cmd);


		Set<String> expectedVars = new HashSet<>();
		expectedVars.add("x");
		expectedVars.add("y");
		Set<String> actualVars = new HashSet<>(cmd.getFreeVariables());

		EvalResult value = (EvalResult) cmd.getValue();
		Integer xVal = Integer.parseInt(value.getSolution("x"));
		Integer yVal = Integer.parseInt(value.getSolution("y"));

		assertAll(
				() -> assertEquals(expectedVars, actualVars),
				() -> assertTrue(xVal > yVal)
		);
	}

	@Test
	@Disabled("Only works for y<x somehow?")
	void should_solve_in_state_when_dpllt() throws IOException {
		State state = stateSpace.getRoot().explore();
		state = state.getTransitions().get(0).getDestination().explore();
		String predicate = "x:INTEGER & y:INTEGER & y=x+1";

		ClassicalB pred = new ClassicalB(predicate, FormulaExpand.EXPAND);

		CbcSolveCommand cmd = new CbcSolveCommand(pred, CbcSolveCommand.Solvers.DPLLT, state);
		stateSpace.execute(cmd);

		EvalResult value = (EvalResult) cmd.getValue();
		Integer expected = 6; // Value for x=5 defined in the machine.
		Integer actual = Integer.parseInt(value.getSolution("y"));

		assertEquals(expected, actual);
	}

}
