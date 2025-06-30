package de.prob.animator.domainobjects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TLAEvalElementTest {

	@Test
	public void testExpression() {
		TLA element = new TLA("9");
		assertEquals(EvalElementType.EXPRESSION, element.getKind());
	}

	@Test
	public void testPredicate() {
		TLA element = new TLA("9 \\in Int");
		assertEquals(EvalElementType.PREDICATE, element.getKind());
	}

	@Test
	public void testInstantParserError() {
		// this is undocumented behaviour but nevertheless important
		Assertions.assertThrows(EvaluationException.class, () -> new TLA("9 + "));
	}
}
