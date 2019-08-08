package de.prob.model.eventb;

public class FormulaParseException extends ModelGenerationException {
	private static final long serialVersionUID = -5076084942068072351L;

	private final String formula;

	public FormulaParseException(final String formula) {
		super(formatMessage(formula));
		
		this.formula = formula;
	}

	public FormulaParseException(final String formula, final Throwable cause) {
		super(formatMessage(formula), cause);
		
		this.formula = formula;
	}

	private static String formatMessage(final String formula) {
		return "Could not parse formula: " + formula;
	}

	public String getFormula() {
		return formula;
	}
}
