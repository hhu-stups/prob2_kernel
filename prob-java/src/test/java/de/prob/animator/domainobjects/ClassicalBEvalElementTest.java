package de.prob.animator.domainobjects;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassicalBEvalElementTest {

	@Test
	public void testExpression() {
		ClassicalB element = new ClassicalB("9");
		assertEquals(EvalElementType.EXPRESSION, element.getKind());
	}

	@Test
	public void testExpressionParserError() {
		Assertions.assertThrows(EvaluationException.class, () -> {
			ClassicalB element = new ClassicalB("9 + ");
			assertEquals(EvalElementType.EXPRESSION, element.getKind());
		});
	}

	@Test
	public void testExpressionInstantParserError() {
		// this is undocumented behaviour but nevertheless important
		Assertions.assertThrows(EvaluationException.class, () -> new ClassicalB("9 + "));
	}

	@Test
	public void testPredicate() {
		ClassicalB element = new ClassicalB("9:NAT");
		assertEquals(EvalElementType.PREDICATE, element.getKind());
	}

	@Test
	public void testPredicateParserError() {
		Assertions.assertThrows(EvaluationException.class, () -> {
			ClassicalB element = new ClassicalB("9:NAT & ");
			assertEquals(EvalElementType.PREDICATE, element.getKind());
		});
	}

	@Test
	public void testPredicateInstantParserError() {
		Assertions.assertThrows(EvaluationException.class, () -> new ClassicalB("9:NAT & "));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testAssignmentDeprecated() {
		ClassicalB element = new ClassicalB("x:=42", FormulaExpand.EXPAND, true);
		assertEquals(EvalElementType.ASSIGNMENT, element.getKind());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testAssignmentDeprecatedParserError() {
		// even correct assignments throw an exception because they are not allowed in the standard constructor
		Assertions.assertThrows(EvaluationException.class, () -> {
			ClassicalB element = new ClassicalB("x:=42 :=", FormulaExpand.EXPAND, true);
			assertEquals(EvalElementType.PREDICATE, element.getKind());
		});
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testAssignmentDeprecatedInstantParserError() {
		Assertions.assertThrows(EvaluationException.class, () -> new ClassicalB("x:=42 :=", FormulaExpand.EXPAND, true));
	}

	@Test
	public void testAssignment() throws BCompoundException {
		String formula = "x:=42";
		Start ast = new BParser().parseSubstitution(formula);
		ClassicalB element = new ClassicalB(ast, formula);
		assertEquals(EvalElementType.ASSIGNMENT, element.getKind());
	}

	@Test
	public void testAssignmentStandardConstructorError() {
		// even correct assignments throw an exception because they are not allowed in the standard constructor
		Assertions.assertThrows(EvaluationException.class, () -> {
			ClassicalB element = new ClassicalB("x:=42");
			assertEquals(EvalElementType.PREDICATE, element.getKind());
		});
	}

	@Test
	public void testAssignmentStandardConstructorInstantError() {
		Assertions.assertThrows(EvaluationException.class, () -> new ClassicalB("x:=42"));
	}
}
