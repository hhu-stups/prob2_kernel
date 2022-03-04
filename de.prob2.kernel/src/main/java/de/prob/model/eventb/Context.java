package de.prob.model.eventb;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.Axiom;
import de.prob.model.representation.Constant;
import de.prob.model.representation.ConstantsComponent;
import de.prob.model.representation.ElementComment;
import de.prob.model.representation.ModelElementList;
import de.prob.model.representation.Set;

public class Context extends AbstractElement implements ConstantsComponent {

	private final String name;

	public Context(final String name) {
		this(name, Collections.emptyMap());
	}

	private Context(
			final String name,
			Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children) {
		super(children);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public Context withName(final String name) {
		return new Context(name, this.getChildren());
	}

	public Context set(Class<? extends AbstractElement> clazz,
			ModelElementList<? extends AbstractElement> elements) {
		return new Context(name, assoc(clazz, elements));
	}

	public <T extends AbstractElement> Context addTo(Class<T> clazz, T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new Context(name, assoc(clazz, list.addElement(element)));
	}

	public <T extends AbstractElement> Context removeFrom(Class<T> clazz,
			T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new Context(name, assoc(clazz, list.removeElement(element)));
	}

	public <T extends AbstractElement> Context replaceIn(Class<T> clazz,
			T oldElement, T newElement) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new Context(name, assoc(clazz,
				list.replaceElement(oldElement, newElement)));
	}

	public String getComment() {
		final ModelElementList<ElementComment> comments = getChildrenOfType(ElementComment.class);
		if (comments == null) {
			return null;
		} else {
			return comments.stream()
				.map(ElementComment::getComment)
				.collect(Collectors.joining("\n"));
		}
	}

	public Context withComment(final String comment) {
		return this.set(ElementComment.class, new ModelElementList<>(Collections.singletonList(new ElementComment(comment))));
	}

	public ModelElementList<Context> getExtends() {
		return getChildrenOfType(Context.class);
	}

	public Context withExtends(final ModelElementList<Context> contexts) {
		return this.set(Context.class, contexts);
	}

	@Override
	public ModelElementList<EventBConstant> getConstants() {
		return getChildrenAndCast(Constant.class, EventBConstant.class);
	}

	public Context withConstants(final ModelElementList<EventBConstant> constants) {
		return this.set(Constant.class, constants);
	}

	@Override
	public ModelElementList<EventBAxiom> getAxioms() {
		return getChildrenAndCast(Axiom.class, EventBAxiom.class);
	}

	public Context withAxioms(final ModelElementList<EventBAxiom> axioms) {
		return this.set(Axiom.class, axioms);
	}

	public ModelElementList<EventBAxiom> getAllAxioms() {
		ModelElementList<EventBAxiom> axms = new ModelElementList<>();
		for (Context ctx : getExtends()) {
			axms = axms.addMultiple(ctx.getAllAxioms());
		}
		axms = axms.addMultiple(getAxioms());
		return axms;
	}

	@Override
	public ModelElementList<Set> getSets() {
		return getChildrenOfType(Set.class);
	}

	public Context withSets(final ModelElementList<Set> sets) {
		return this.set(Set.class, sets);
	}

	public ModelElementList<ProofObligation> getProofs() {
		return getChildrenOfType(ProofObligation.class);
	}

	public Context withProofs(final ModelElementList<ProofObligation> proofs) {
		return this.set(ProofObligation.class, proofs);
	}

	@Override
	public String toString() {
		return getName();
	}

}
