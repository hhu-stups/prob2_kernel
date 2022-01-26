package de.prob.animator.domainobjects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicalBEvalElementTest {

	@Test
	public void testExpression() {
		ClassicalB element = new ClassicalB("9", FormulaExpand.EXPAND);
		assertEquals(EvalElementType.EXPRESSION, element.getKind());
	}

	@Test
	public void testExpressionParserError() {
		Assertions.assertThrows(EvaluationException.class, () -> {
			ClassicalB element = new ClassicalB("9 + ", FormulaExpand.EXPAND);
			assertEquals(EvalElementType.EXPRESSION, element.getKind());
		});
	}

	@Test
	public void testPredicate() {
		ClassicalB element = new ClassicalB("9:NAT", FormulaExpand.EXPAND);
		assertEquals(EvalElementType.PREDICATE, element.getKind());
	}

	@Test
	public void testPredicateParserError() {
		Assertions.assertThrows(EvaluationException.class, () -> {
			ClassicalB element = new ClassicalB("9:NAT & ", FormulaExpand.EXPAND);
			assertEquals(EvalElementType.PREDICATE, element.getKind());
		});
	}

}
