package de.prob.model.eventb;

import de.prob.animator.domainobjects.EventB;

public class FormulaTypeException extends ModelGenerationException {
	private static final long serialVersionUID = 492703593594896699L;

	private final EventB formula;
	private final String expected;

	public FormulaTypeException(final EventB formula, final String expected) {
		super(formatMessage(formula, expected));

		this.formula = formula;
		this.expected = expected;
	}

	public FormulaTypeException(final EventB formula, final String expected, final Throwable cause) {
		super(formatMessage(formula, expected), cause);

		this.formula = formula;
		this.expected = expected;
	}

	private static String formatMessage(final EventB formula, final String expected) {
		return "Expected " + formula + " to be of type " + expected;
	}

	public EventB getFormula() {
		return formula;
	}

	public String getExpected() {
		return expected;
	}
}
