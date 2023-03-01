package de.prob.animator.domainobjects;

import com.google.inject.Singleton;

import de.prob.model.representation.AbstractModel;

/**
 * @deprecated No direct replacement is provided. Use {@link AbstractModel#parseFormula(String)} or the {@link IEvalElement} constructors instead and store the formula language separately if needed.
 */
@Deprecated
@Singleton
public class EvalElementFactory {

	public IEvalElement deserialize(final String content) {
		if (content.startsWith("#ClassicalB:")) {
			return toClassicalB(content);
		}
		if (content.startsWith("#EventB:")) {
			return toEventB(content);
		}

		throw new IllegalArgumentException("String with format " + content
				+ " cannot be deserialized to an IEvalElement");
	}

	private EventB toEventB(final String content) {
		return new EventB(content.substring(content.indexOf(':') + 1), FormulaExpand.EXPAND);
	}

	private ClassicalB toClassicalB(final String content) {
		return new ClassicalB(content.substring(content.indexOf(':') + 1), FormulaExpand.EXPAND,true); // TODO: check if AllowSubst required
	}
}
