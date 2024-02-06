package de.prob.animator.domainobjects;

import de.be4.ltl.core.parser.LtlParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
final class LtlParserTest {
	@Test
	void parseClassicalBLtlFailsForEventBFormula() {
		Assertions.assertThrows(LtlParseException.class, () -> new LTL("G {gear(front) = extended}"));
	}

	@Test
	void parseEventBLtl() throws LtlParseException {
		new LTL("G {gear(front) = extended}", new EventBParserBase());
	}
}
