package de.prob.model.representation;

import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.statespace.Trace;
import de.prob.unicode.UnicodeTranslator;

public abstract class Constant extends AbstractFormulaElement implements Named {

	protected final IEvalElement expression;
	@Deprecated
	protected AbstractEvalResult result;

	public Constant(final IEvalElement expression) {
		this.expression = expression;
	}

	public IEvalElement getExpression() {
		return expression;
	}

	@Override
	public IEvalElement getFormula() {
		return expression;
	}

	@Override
	public String toString() {
		return UnicodeTranslator.toUnicode(expression.getCode());
	}

	/**
	 * @deprecated This method does not behave correctly if the constant has multiple possible values (more than one SETUP_CONSTANTS transition).
	 */
	@Deprecated
	public AbstractEvalResult getValue(final Trace h) {
		if (result == null) {
			result = h.evalCurrent(getFormula());
		}
		return result;
	}

}
