package de.prob.statespace;

import java.io.IOException;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.Definitions;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.ParseOptions;
import de.be4.classicalb.core.parser.analysis.checking.DefinitionCollector;
import de.be4.classicalb.core.parser.analysis.transforming.CoupleToIdentifierTransformation;
import de.be4.classicalb.core.parser.analysis.transforming.OpSubstitutions;
import de.be4.classicalb.core.parser.analysis.transforming.SyntaxExtensionTranslator;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.cli.CliTestCommon;
import de.prob.exception.ProBError;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InitialiseWithErrorsTest {

	private static ClassicalBFactory factory;

	@BeforeAll
	static void beforeAll() throws IOException {
		factory = CliTestCommon.getInjector().getInstance(ClassicalBFactory.class);
	}

	@Test
	void testMachineWithoutInitClause() throws Exception {
		// issue: https://github.com/hhu-stups/prob-issues/issues/381
		String machine = "MACHINE MyAwesomeLift\n" +
				"CONSTANTS top_floor\n" +
				"PROPERTIES 3<=top_floor & top_floor<=10\n" +
				"VARIABLES current_floor\n" +
				"INVARIANT -1<=current_floor & current_floor<=top_floor\n" +
				"OPERATIONS\n" +
				"up(n) = SELECT 0 < n & current_floor+n<=top_floor THEN current_floor := current_floor+n END;\n" +
				"down = SELECT -1 < current_floor THEN current_floor := pred(current_floor) END\n" +
				"END\n";
		ParseOptions parseOptions = new ParseOptions();
		parseOptions.setApplyASTTransformations(false); // so we can create the AST without INITIALISATION
		BParser parser = new BParser("from_string", parseOptions);
		Start ast = parser.parseMachine(machine);

		// just to be safe we do the same transformation the parser does
		ast.apply(new CoupleToIdentifierTransformation());
		ast.apply(new SyntaxExtensionTranslator());
		IDefinitions definitions = new Definitions();
		DefinitionCollector collector = new DefinitionCollector(definitions);
		collector.collectDefinitions(ast);
		OpSubstitutions.transform(ast, definitions);

		StateSpace ss = factory.create(ast).load();
		State root = ss.getRoot();

		State result = root.perform(Transition.SETUP_CONSTANTS_NAME, "top_floor=3");
		assertNotNull(result);

		State result2 = root.perform(Transition.SETUP_CONSTANTS_NAME, "top_floor=3");
		assertNotNull(result2);

		assertEquals(result, result2);
		assertThrows(ProBError.class, result::explore);
	}
}
