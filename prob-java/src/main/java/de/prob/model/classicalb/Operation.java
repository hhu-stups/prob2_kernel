package de.prob.model.classicalb;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.BEvent;
import de.prob.model.representation.ModelElementList;


public class Operation extends BEvent {
	public Operation(final String name, final List<String> parameters, final List<String> output) {
		this(name, parameters, output, Collections.emptyMap());
	}

	private Operation(
		final String name,
		final List<String> parameters,
		final List<String> output,
		Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children
	) {
		super(name, children);
		this.parameters = parameters;
		this.output = output;
	}

	public <T extends AbstractElement> Operation addTo(T element) {
		@SuppressWarnings("unchecked")
		ModelElementList<T> kids = (ModelElementList<T>) getChildrenOfType(element.getClass());
		return new Operation(getName(), parameters, output, assoc(element.getClass(), kids.addElement(element)));
	}

	public Operation set(Class<? extends AbstractElement> clazz, ModelElementList<? extends AbstractElement> elements) {
		return new Operation(getName(), parameters, output, assoc(clazz, elements));
	}

	public ModelElementList<ClassicalBGuard> addGuards() {
		return getChildrenOfType(ClassicalBGuard.class);
	}

	public ModelElementList<ClassicalBAction> addActions() {
		return getChildrenOfType(ClassicalBAction.class);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (!output.isEmpty()) {
			for (String string : output) {
				sb.append(string);
				sb.append(" <-- ");
			}

		}

		sb.append(getName());
		if (!parameters.isEmpty()) {
			sb.append('(');
			sb.append(String.join(",", parameters));
			sb.append(')');
		}

		return sb.toString();
	}

	public final List<String> getParameters() {
		return parameters;
	}

	public final List<String> getOutput() {
		return output;
	}

	private final List<String> parameters;
	private final List<String> output;
}
