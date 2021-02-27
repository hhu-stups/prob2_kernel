package de.prob.animator.domainobjects;

import com.google.inject.Singleton;

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
