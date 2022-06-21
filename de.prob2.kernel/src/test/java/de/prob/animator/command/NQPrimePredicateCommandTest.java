package de.prob.animator.command;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NQPrimePredicateCommandTest {
	private static Api api;
	private static StateSpace stateSpace;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@BeforeEach
	void beforeEach() throws IOException, URISyntaxException {
		String example_mch = Paths.get(NQPrimePredicateCommandTest.class.getClassLoader()
				.getResource("de/prob/testmachines/b/VariablesOnly.mch")
				.toURI()).toString();
		stateSpace = api.b_load(example_mch);
	}

	@AfterEach
	void afterEach() {
		stateSpace.kill();
	}

	@Test
	public void should_prime_var_greater_zero() {
		String formula = "a>0";
		String expected = "a′ > 0";
		assertPriming(formula, expected);
	}

	@Test
	public void should_prime_machine_var_greater_zero() {
		String formula = "x>0";
		String expected = "x′ > 0";
		assertPriming(formula, expected);
	}

	@Test
	public void should_prime_int_membership() {
		String formula = "a:INTEGER";
		String expected = "a′ : INTEGER";
		assertPriming(formula, expected);
	}

	@Test
	public void should_prime_machine_var_int_membership() {
		String formula = "x:INTEGER";
		String expected = "x′ : INTEGER";
		assertPriming(formula, expected);
	}

	@Test
	public void should_prime_machine_var_gt_relation_when_old() {
		String formula = "x>x-1";
		String expected = "x' > x' - 1";
		assertOldPriming(formula, expected);
	}

	@Test
	public void should_prime_with_quantifier_nonmachine_var_gt_relation_when_old() {
		String formula = "a>a-1";
		String expected = "#a'.(a' > a' - 1)";
		assertOldPriming(formula, expected);
	}

	@Test
	public void should_prime_without_quantifier_nonmachine_var_gt_relation() {
		String formula = "a>a-1";
		String expected = "a′ > a′ - 1";
		assertPriming(formula, expected);
	}

	@Test
	public void should_prime_gt_relation_with_machine_var_and_free_var() {
		String formula = "x>y";
		String expected = "x′ > y′";
		assertPriming(formula, expected);
	}


	@Test
	public void should_prime_gt_relation_with_quantifier_when_using_old_command() {
		String formula = "x>y";
		String expected = "#y'.(x' > y')";
		assertOldPriming(formula, expected);
	}


	@Test
	public void should_not_prime_function_faithfully_when_old_command() {
		String formula = "f : INT +-> INT";
		String expected = "f' : INTEGER +-> INTEGER";

		// Funny anecdote:
		// The result uses INTEGER, but if we use "f : INTEGER +-> INTEGER"
		// as input, the output is "#INTEGER'.(f' : INTEGER' +-> INTEGER')"

		assertOldPriming(formula, expected);
	}


	@Test
	public void should_prime_function_properly()  {
		String formula = "f : INT +-> INT";
		String expected = "f′ : INT +-> INT";
		assertPriming(formula, expected);
	}


	@Test
	public void should_prime_non_machinevar_function_properly()  {
		String formula = "g : INT +-> INT";
		String expected = "g′ : INT +-> INT";
		assertPriming(formula, expected);
	}


	/**
	 * Runs the basic test conducted in each of the test cases. Just boils down on boiler plate code.
	 */
	private void assertPriming(String formula, String expected) {
		ClassicalB pred = new ClassicalB(formula, FormulaExpand.EXPAND);

		NQPrimePredicateCommand cmd = new NQPrimePredicateCommand(pred);
		stateSpace.execute(cmd);
		String actual = cmd.getPrimedPredicate();

		assertEquals(expected, actual);
	}

	/**
	 * See {@link #assertPriming(String, String)}.
	 */
	private void assertOldPriming(String formula, String expected) {
		// The old implementation fails when ClassicalB is used, hence EventB.
		EventB pred = new EventB(formula, FormulaExpand.EXPAND);

		PrimePredicateCommand cmd = new PrimePredicateCommand(pred);
		stateSpace.execute(cmd);
		String actual = cmd.getPrimedPredicate().getCode();

		assertEquals(expected, actual);
	}

}
