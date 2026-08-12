package de.prob.scripting;

import de.be4.classicalb.core.parser.node.*;
import de.prob.cli.CliTestCommon;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class LoadClassicalBTest {

	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void testLoadFromASTWithDefinitionCalls() throws IOException {
		Start ast = new Start(new AAbstractMachineParseUnit(
				new AMachineMachineVariant(),
				new AMachineHeader(Collections.singletonList(new TIdentifierLiteral("TestMch")), new LinkedList<>()),
				Arrays.asList(
						new ADefinitionsMachineClause(Collections.singletonList(
								new AExpressionDefinitionDefinition(
										new TIdentifierLiteral("TestDef"),
										new LinkedList<>(),
										new ABooleanTrueExpression()
								)
						)),
						new APropertiesMachineClause(
								new AEqualPredicate(
										new ADefinitionExpression(new TIdentifierLiteral("TestDef"), new LinkedList<>()),
										new ABooleanTrueExpression()
								)
						)
				)
		), new EOF());
		api.b_load(ast).kill();
	}

}
