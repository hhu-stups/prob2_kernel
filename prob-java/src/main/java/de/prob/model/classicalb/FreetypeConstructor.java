package de.prob.model.classicalb;

import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.representation.AbstractFormulaElement;
import de.prob.model.representation.Named;

public class FreetypeConstructor extends AbstractFormulaElement implements Named {

	private final String name;
	private final ClassicalB argument;

	public FreetypeConstructor(final String name) {
		this(name, null);
	}

	public FreetypeConstructor(final String name, final Start argument) {
		this.name = name;
		this.argument = argument != null ? new ClassicalB(argument) : null;
	}

	public boolean hasArgument() {
		return this.argument != null;
	}

	public ClassicalB getArgument() {
		return this.argument;
	}

	@Override
	public IEvalElement getFormula() {
		return this.getArgument();
	}

	@Override
	public String getName() {
		return this.name;
	}
}
