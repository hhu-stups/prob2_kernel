package de.prob.model.classicalb;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.ModelElementList;
import de.prob.model.representation.Named;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Freetype extends AbstractElement implements Named {

	private final String name;
	private final List<String> parameters;

	public Freetype(final String name, final List<String> parameters) {
		this(name, parameters, Collections.emptyMap());
	}

	public Freetype(final String name, final List<String> parameters, Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children) {
		super(children);
		this.name = name;
		this.parameters = parameters;
	}

	public <T extends AbstractElement> Freetype addTo(T child) {
		@SuppressWarnings("unchecked")
		ModelElementList<T> children = (ModelElementList<T>) getChildrenOfType(child.getClass());
		return new Freetype(this.name, this.parameters, assoc(child.getClass(), children.addElement(child)));
	}

	public Freetype set(Class<? extends AbstractElement> clazz, ModelElementList<? extends AbstractElement> children) {
		return new Freetype(this.name, this.parameters, assoc(clazz, children));
	}

	public ModelElementList<FreetypeConstructor> getConstructors() {
		return this.getChildrenOfType(FreetypeConstructor.class);
	}

	public FreetypeConstructor getConstructor(String name) {
		return this.getConstructors().get(name);
	}

	@Override
	public String getName() {
		return this.name;
	}

	public List<String> getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		if (!this.parameters.isEmpty()) {
			sb.append('(');
			sb.append(String.join(",", this.parameters));
			sb.append(')');
		}
		return sb.toString();
	}
}
