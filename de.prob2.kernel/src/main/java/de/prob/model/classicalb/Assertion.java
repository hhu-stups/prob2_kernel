package de.prob.model.classicalb;

import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.representation.AbstractTheoremElement;
import de.prob.model.representation.Named;
import de.prob.unicode.UnicodeTranslator;

public class Assertion extends AbstractTheoremElement implements Named {

	private final ClassicalB predicate;
	private final String name;

	public Assertion(final Start start) {
		this(start, null);
	}

	public Assertion(final Start start, final String name) {
		predicate = new ClassicalB(start);
		this.name = name;
	}

	public ClassicalB getPredicate() {
		return this.predicate;
	}

	@Override
	public IEvalElement getFormula() {
		return this.getPredicate();
	}

	@Override
	public boolean isTheorem() {
		return true;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return UnicodeTranslator.toUnicode(this.predicate.getCode());
	}
}
