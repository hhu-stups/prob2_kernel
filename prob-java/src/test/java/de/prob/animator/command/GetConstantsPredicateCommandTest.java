package de.prob.animator.command;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

public class GetConstantsPredicateCommandTest {
	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void simpleConstants1() throws IOException, BCompoundException {
		Start ast = new BParser("SimpleConstants").parseMachine("MACHINE SimpleConstants\n" +
				"CONSTANTS a, b, c, d\n" +
				"PROPERTIES a = TRUE & b : 1..3 & c <: d & d <: {a} * {b}\n" +
				"END");
		final StateSpace stateSpace = api.b_load(ast);
		stateSpace.changePreferences(Collections.singletonMap("MAX_INITIALISATIONS","20"));
		GetConstantsPredicateCommand getConstantsPredicate = new GetConstantsPredicateCommand();
		stateSpace.execute(getConstantsPredicate);
		Assertions.assertEquals("( a=TRUE & b=1 & c={} & d={} )"
				+ " or ( a=TRUE & b=1 & c={} & d={(TRUE|->1)} )"
				+ " or ( a=TRUE & b=1 & c={(TRUE|->1)} & d={(TRUE|->1)} )"
				+ " or ( a=TRUE & b=2 & c={} & d={} )"
				+ " or ( a=TRUE & b=2 & c={} & d={(TRUE|->2)} )"
				+ " or ( a=TRUE & b=2 & c={(TRUE|->2)} & d={(TRUE|->2)} )"
				+ " or ( a=TRUE & b=3 & c={} & d={} )"
				+ " or ( a=TRUE & b=3 & c={} & d={(TRUE|->3)} )"
				+ " or ( a=TRUE & b=3 & c={(TRUE|->3)} & d={(TRUE|->3)} )",
				getConstantsPredicate.getPredicate());
		Assertions.assertTrue(getConstantsPredicate.getPredicateComplete());
		stateSpace.kill();
	}

	@Test
	public void simpleConstants2() throws IOException, BCompoundException {
		Start ast = new BParser("SimpleConstants").parseMachine("MACHINE SimpleConstants\n" +
				"CONSTANTS a, b\n" +
				"PROPERTIES a = {(25, 5.0),(36, 6.0)} & b = {a(25), a(36)}\n" +
				"END");
		final StateSpace stateSpace = api.b_load(ast);
		GetConstantsPredicateCommand getConstantsPredicate = new GetConstantsPredicateCommand();
		stateSpace.execute(getConstantsPredicate);
		Assertions.assertEquals("( a={(25|->5.0),(36|->6.0)} & b={5.0,6.0} )", getConstantsPredicate.getPredicate());
		Assertions.assertTrue(getConstantsPredicate.getPredicateComplete());
		stateSpace.kill();
	}

	@Test
	public void simpleConstants3() throws IOException, BCompoundException {
		Start ast = new BParser("SimpleConstants").parseMachine("MACHINE SimpleConstants\n" +
				"CONSTANTS a, b\n" +
				"PROPERTIES a = 1 & b = 1\n" +
				"END");
		final StateSpace stateSpace = api.b_load(ast);
		GetConstantsPredicateCommand getConstantsPredicate = new GetConstantsPredicateCommand();
		stateSpace.execute(getConstantsPredicate);
		Assertions.assertEquals("( a=1 & b=1 )", getConstantsPredicate.getPredicate());
		Assertions.assertTrue(getConstantsPredicate.getPredicateComplete());
		stateSpace.kill();
	}

	@Test
	public void incompleteSetupConstants() throws IOException, BCompoundException {
		Start ast = new BParser("IncompleteConstants").parseMachine("MACHINE IncompleteConstants\n" +
				"CONSTANTS a\n" +
				"PROPERTIES a : 1..2\n" +
				"END");
		final StateSpace stateSpace = api.b_load(ast);
		stateSpace.changePreferences(Collections.singletonMap("MAX_INITIALISATIONS","0"));
		GetConstantsPredicateCommand getConstantsPredicate = new GetConstantsPredicateCommand();
		stateSpace.execute(getConstantsPredicate);
		Assertions.assertEquals("1=1", getConstantsPredicate.getPredicate());
		Assertions.assertFalse(getConstantsPredicate.getPredicateComplete());
		stateSpace.kill();
	}
}
