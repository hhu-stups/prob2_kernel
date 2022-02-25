package de.prob.model.representation;

import java.util.Map;

public abstract class Machine extends AbstractElement implements Named {

	protected final String name;

	public Machine(final String name, Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children) {
		super(children);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
