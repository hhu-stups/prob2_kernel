package de.prob.animator.domainobjects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventBEvalElementTest {

	@Test
	public void testExpression() {
		EventB element = new EventB("9");
		assertEquals(EvalElementType.EXPRESSION, element.getKind());
	}

	@Test
	public void testExpressionParserError() {
		Assertions.assertThrows(EvaluationException.class, () -> {
			EventB element = new EventB("9 + ");
			element.ensureParsed();
			// EventB#getKind swallows the EvaluationException
			// assertEquals(EvalElementType.EXPRESSION, element.getKind());
		});
	}

	@Test
	public void testPredicate() {
		EventB element = new EventB("9:NAT");
		assertEquals(EvalElementType.PREDICATE, element.getKind());
	}

	@Test
	public void testPredicateParserError() {
		Assertions.assertThrows(EvaluationException.class, () -> {
			EventB element = new EventB("9:NAT & ");
			element.ensureParsed();
			// EventB#getKind swallows the EvaluationException
			// assertEquals(EvalElementType.PREDICATE, element.getKind());
		});
	}

	@Test
	public void testAssignmentDeprecated() {
		EventB element = new EventB("x:=42");
		assertEquals(EvalElementType.ASSIGNMENT, element.getKind());
	}

	@Test
	public void testAssignmentParserError() {
		Assertions.assertThrows(EvaluationException.class, () -> {
			EventB element = new EventB("x:=42 :=");
			element.ensureParsed();
			// EventB#getKind swallows the EvaluationException
			// assertEquals(EvalElementType.ASSIGNMENT, element.getKind());
		});
	}
}
