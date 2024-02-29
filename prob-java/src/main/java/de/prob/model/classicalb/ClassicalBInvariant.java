package de.prob.model.classicalb;

import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.model.representation.Invariant;
import de.prob.model.representation.Named;

public class ClassicalBInvariant extends Invariant implements Named {

	private final String name;

	public ClassicalBInvariant(final Start start) {
		this(start, null);
	}

	public ClassicalBInvariant(final Start start, final String name) {
		super(new ClassicalB(start));
		this.name = name;
	}

	@Override
	public boolean isTheorem() {
		return false;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
