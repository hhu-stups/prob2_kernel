package de.prob.model.eventb;

import java.util.Collections;
import java.util.Map;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.Action;
import de.prob.model.representation.BEvent;
import de.prob.model.representation.ElementComment;
import de.prob.model.representation.Guard;
import de.prob.model.representation.ModelElementList;

public class Event extends BEvent {

	private final EventType type;
	private final boolean extended;

	public enum EventType {
		ORDINARY, CONVERGENT, ANTICIPATED
	}

	public Event(final String name, final EventType type, final boolean extended) {
		this(name, type, extended, Collections.emptyMap());
	}

	private Event(
			final String name,
			final EventType type,
			final boolean extended,
			Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children) {
		super(name, children);
		this.type = type;
		this.extended = extended;
	}

	public Event set(Class<? extends AbstractElement> clazz,
			ModelElementList<? extends AbstractElement> elements) {
		return new Event(name, type, extended, assoc(clazz, elements));
	}

	public <T extends AbstractElement> Event addTo(Class<T> clazz, T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new Event(name, type, extended, assoc(clazz,
				list.addElement(element)));
	}

	public <T extends AbstractElement> Event removeFrom(Class<T> clazz,
			T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new Event(name, type, extended, assoc(clazz,
				list.removeElement(element)));
	}

	public <T extends AbstractElement> Event replaceIn(Class<T> clazz,
			T oldElement, T newElement) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new Event(name, type, extended, assoc(clazz,
				list.replaceElement(oldElement, newElement)));
	}

	public String getComment() {
		return ElementComment.getCommentTextFromElement(this);
	}

	public Event withComment(final String comment) {
		return this.set(ElementComment.class, new ModelElementList<>(Collections.singletonList(new ElementComment(comment))));
	}

	/**
	 * @deprecated Use {@link #getRefinesEvent()} instead. An Event-B event cannot refine more than one event.
	 */
	@Deprecated
	public ModelElementList<Event> getRefines() {
		return getChildrenOfType(Event.class);
	}

	/**
	 * The {@link Event} saves a reference to the name of the refined events.
	 * However, this is just a reference, and in order to retrieve the actual
	 * refined events the parent machine needs to be passed in as an argument.
	 *
	 * @return the event that this event refines, if any. Otherwise null.
	 */
	public Event getRefinesEvent() {
		final ModelElementList<Event> refines = getChildrenOfType(Event.class);
		if (refines.isEmpty()) {
			return null;
		} else if (refines.size() == 1) {
			return refines.get(0);
		} else {
			throw new IllegalStateException("An Event-B event cannot refine more than one event");
		}
	}

	public Event withRefinesEvent(final Event refinesEvent) {
		ModelElementList<Event> refines = new ModelElementList<>();
		if (refinesEvent != null) {
			refines = refines.addElement(refinesEvent);
		}
		return this.set(Event.class, refines);
	}

	public ModelElementList<EventBGuard> getGuards() {
		return getChildrenAndCast(Guard.class, EventBGuard.class);
	}

	public Event withGuards(final ModelElementList<EventBGuard> guards) {
		return this.set(Guard.class, guards);
	}

	public ModelElementList<EventBGuard> getAllGuards() {
		ModelElementList<EventBGuard> acts = new ModelElementList<>();
		for (Event e : getRefines()) {
			acts = acts.addMultiple(e.getAllGuards());
		}
		return acts.addMultiple(getGuards());
	}

	public ModelElementList<EventBAction> getActions() {
		return getChildrenAndCast(Action.class, EventBAction.class);
	}

	public Event withActions(final ModelElementList<EventBAction> actions) {
		return this.set(Action.class, actions);
	}

	public ModelElementList<EventBAction> getAllActions() {
		ModelElementList<EventBAction> acts = new ModelElementList<>();
		for (Event e : getRefines()) {
			acts = acts.addMultiple(e.getAllActions());
		}
		return acts.addMultiple(getActions());
	}

	public ModelElementList<Witness> getWitnesses() {
		return getChildrenOfType(Witness.class);
	}

	public Event withWitnesses(final ModelElementList<Witness> witnesses) {
		return this.set(Witness.class, witnesses);
	}

	public ModelElementList<EventParameter> getParameters() {
		return getChildrenOfType(EventParameter.class);
	}

	public Event withParameters(final ModelElementList<EventParameter> parameters) {
		return this.set(EventParameter.class, parameters);
	}

	public EventType getType() {
		return type;
	}

	public boolean isExtended() {
		return extended;
	}

	public Event withName(final String name) {
		return new Event(name, type, extended, getChildren());
	}

	public Event changeType(EventType type) {
		return new Event(name, type, extended, getChildren());
	}

	public Event toggleExtended(boolean extended) {
		if (extended == this.extended) {
			return this;
		}
		return new Event(name, type, extended, getChildren());
	}

	@Override
	public String toString() {
		return getName();
	}
}
