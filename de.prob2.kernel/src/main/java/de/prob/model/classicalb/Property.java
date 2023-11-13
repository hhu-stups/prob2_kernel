package de.prob.model.classicalb;

import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.model.representation.Axiom;
import de.prob.model.representation.Named;

public class Property extends Axiom implements Named {

	private final String name;

	public Property(final Start start) {
		this(start, null);
	}

	public Property(final Start start, final String name) {
		super(new ClassicalB(start));
		this.name = name;
	}

	@Override
	public boolean isTheorem() {
		// TODO: is this true?
		return false;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
