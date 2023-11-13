package de.prob.model.classicalb;

import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.representation.AbstractFormulaElement;
import de.prob.model.representation.Named;

public class Constraint extends AbstractFormulaElement implements Named {

	private final ClassicalB predicate;
	private final String name;

	public Constraint(final Start start) {
		this(start, null);
	}

	public Constraint(final Start start, final String name) {
		this.predicate = new ClassicalB(start);
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
	public String getName() {
		return this.name;
	}
}
