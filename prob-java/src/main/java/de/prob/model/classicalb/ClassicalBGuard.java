package de.prob.model.classicalb;

import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.model.representation.Guard;
import de.prob.model.representation.Named;

public class ClassicalBGuard extends Guard implements Named {

	private final String name;

	public ClassicalBGuard(final Start guard) {
		this(guard, null);
	}

	public ClassicalBGuard(final Start start, final String name) {
		super(new ClassicalB(start));
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
