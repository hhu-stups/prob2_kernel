package de.prob.model.eventb;

import java.util.Collections;
import java.util.Map;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.BEvent;
import de.prob.model.representation.ElementComment;
import de.prob.model.representation.Invariant;
import de.prob.model.representation.Machine;
import de.prob.model.representation.ModelElementList;
import de.prob.model.representation.Variable;

public class EventBMachine extends Machine {

	public EventBMachine(final String name) {
		this(name, Collections.emptyMap());
	}

	private EventBMachine(
			final String name,
			Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children) {
		super(name, children);
	}

	public EventBMachine set(Class<? extends AbstractElement> clazz,
			ModelElementList<? extends AbstractElement> elements) {
		return new EventBMachine(name, assoc(clazz, elements));
	}



	public <T extends AbstractElement> EventBMachine addTo(Class<T> clazz,
			T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new EventBMachine(name, assoc(clazz, list.addElement(element)));
	}

	public <T extends AbstractElement> EventBMachine removeFrom(Class<T> clazz,
			T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new EventBMachine(name,
				assoc(clazz, list.removeElement(element)));
	}

	public <T extends AbstractElement> EventBMachine replaceIn(Class<T> clazz,
			T oldElement, T newElement) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new EventBMachine(name, assoc(clazz,
				list.replaceElement(oldElement, newElement)));
	}

	public String getComment() {
		return ElementComment.getCommentTextFromElement(this);
	}

	public EventBMachine withComment(final String comment) {
		return this.set(ElementComment.class, new ModelElementList<>(Collections.singletonList(new ElementComment(comment))));
	}

	/**
	 * @deprecated Use {@link #getRefinesMachine()} instead. An Event-B machine cannot refine more than one machine.
	 */
	@Deprecated
	public ModelElementList<EventBMachine> getRefines() {
		return getChildrenAndCast(Machine.class, EventBMachine.class);
	}

	public EventBMachine getRefinesMachine() {
		final ModelElementList<EventBMachine> refines = getChildrenAndCast(Machine.class, EventBMachine.class);
		if (refines.isEmpty()) {
			return null;
		} else if (refines.size() == 1) {
			return refines.get(0);
		} else {
			throw new IllegalStateException("An Event-B machine cannot refine more than one machine");
		}
	}

	public EventBMachine withRefinesMachine(final EventBMachine refinedMachine) {
		ModelElementList<EventBMachine> refines = new ModelElementList<>();
		if (refinedMachine != null) {
			refines = refines.addElement(refinedMachine);
		}
		return this.set(Machine.class, refines);
	}

	public ModelElementList<Context> getSees() {
		return getChildrenOfType(Context.class);
	}

	public EventBMachine withSees(final ModelElementList<Context> sees) {
		return this.set(Context.class, sees);
	}

	@Override
	public ModelElementList<EventBVariable> getVariables() {
		return getChildrenAndCast(Variable.class, EventBVariable.class);
	}

	public EventBMachine withVariables(final ModelElementList<EventBVariable> variables) {
		return this.set(Variable.class, variables);
	}

	@Override
	public ModelElementList<EventBInvariant> getInvariants() {
		return getChildrenAndCast(Invariant.class, EventBInvariant.class);
	}

	public EventBMachine withInvariants(final ModelElementList<EventBInvariant> invariants) {
		return this.set(Invariant.class, invariants);
	}

	public ModelElementList<EventBInvariant> getAllInvariants() {
		ModelElementList<EventBInvariant> invs = new ModelElementList<>();
		if (getRefinesMachine() != null) {
			invs = invs.addMultiple(getRefinesMachine().getAllInvariants());
		}
		invs = invs.addMultiple(getInvariants());
		return invs;
	}

	public Variant getVariant() {
		ModelElementList<Variant> variant = getChildrenOfType(Variant.class);
		return variant.isEmpty() ? null : variant.get(0);
	}

	public EventBMachine withVariant(final Variant variant) {
		ModelElementList<Variant> variants = new ModelElementList<>();
		if (variant != null) {
			variants = variants.addElement(variant);
		}
		return this.set(Variant.class, variants);
	}

	public ModelElementList<ProofObligation> getProofs() {
		return getChildrenOfType(ProofObligation.class);
	}

	public EventBMachine withProofs(final ModelElementList<ProofObligation> proofs) {
		return this.set(ProofObligation.class, proofs);
	}

	@Override
	public ModelElementList<Event> getEvents() {
		return getChildrenAndCast(BEvent.class, Event.class);
	}

	public EventBMachine withEvents(final ModelElementList<Event> events) {
		// The proper class to use here is BEvent,
		// but MachineXmlHandler also sets the same list under Event,
		// so there might be existing code that expects either of the two classes.
		// So to be safe, continue setting both.
		return this.set(BEvent.class, events).set(Event.class, events);
	}

	/**
	 * @deprecated Use {@link #getEvents()} instead, which matches Event-B terminology.
	 */
	@Deprecated
	public ModelElementList<Event> getOperations() {
		return this.getEvents();
	}

	@Override
	public Event getEvent(final String eventName) {
		return getEvents().getElement(eventName);
	}
}
