package de.prob.model.eventb;

import java.util.Collections;
import java.util.Map;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.Action;
import de.prob.model.representation.BEvent;
import de.prob.model.representation.ElementComment;
import de.prob.model.representation.Guard;
import de.prob.model.representation.ModelElementList;

import static java.util.Collections.singletonList;

public class Event extends BEvent {

	private final EventType type;
	private final Inheritance inheritance;

	public enum EventType {
		ORDINARY, CONVERGENT, ANTICIPATED
	}

	/**
	 * Per se extend all new events from skip. However, this is not always usefull so we distinguish three types
	 * 'None' are those events that extend from skip (skip extends from itself)
	 */
	public enum Inheritance{
		REFINES, EXTENDS, NONE
	}

	public Event(final String name, final EventType type, final Inheritance inheritance) {
		this(name, type, inheritance, Collections.emptyMap());
	}

	private Event(
			final String name,
			final EventType type,
			final Inheritance inheritance,
			Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children) {
		super(name, children);
		this.type = type;
		this.inheritance = inheritance;
	}

	public Event set(Class<? extends AbstractElement> clazz,
			ModelElementList<? extends AbstractElement> elements) {
		return new Event(name, type, inheritance, assoc(clazz, elements));
	}

	public <T extends AbstractElement> Event addTo(Class<T> clazz, T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new Event(name, type, inheritance, assoc(clazz,
				list.addElement(element)));
	}

	public <T extends AbstractElement> Event removeFrom(Class<T> clazz,
			T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new Event(name, type, inheritance, assoc(clazz,
				list.removeElement(element)));
	}

	public <T extends AbstractElement> Event replaceIn(Class<T> clazz,
			T oldElement, T newElement) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new Event(name, type, inheritance, assoc(clazz,
				list.replaceElement(oldElement, newElement)));
	}

	public String getComment() {
		return ElementComment.getCommentTextFromElement(this);
	}

	public Event withComment(final String comment) {
		return this.set(ElementComment.class, new ModelElementList<>(singletonList(new ElementComment(comment))));
	}

	/**
	 * @deprecated Use {@link #getParentEvent()} instead. An Event-B event cannot refine more than one event.
	 */
	@Deprecated
	public ModelElementList<Event> getRefines() {
		return getChildrenOfType(Event.class);
	}


	/**
	 * @deprecated Use {@link #getRefinesEvent()} ()} instead.
	 */
	@Deprecated
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
	
	/**
	 * The {@link Event} saves a reference to the name of the parent event.
	 * However, this is just a reference, and in order to retrieve the actual
	 * refined events the parent machine needs to be passed in as an argument.
	 *
	 * @return the event that this event refines, if any. Otherwise null.
	 */
	public Event getParentEvent() {
		final ModelElementList<Event> refines = getChildrenOfType(Event.class);
		if (refines.isEmpty()) {
			return null;
		} else if (refines.size() == 1) {
			return refines.get(0);
		} else {
			throw new IllegalStateException("An Event-B event cannot refine more than one event");
		}
	}

	/**
	 * @deprecated Use {@link #withParentEvent(Event)} ()} ()} instead.
	 */
	@Deprecated
	public Event withRefinesEvent(final Event parentEvent) {
		if (parentEvent != null) {
			return this.set(Event.class, new ModelElementList<>(singletonList(parentEvent)));
		}
		return this.set(Event.class, new ModelElementList<>());
	}


	public Event withParentEvent(final Event parentEvent) {
		if (parentEvent != null) {
			return this.set(Event.class, new ModelElementList<>(singletonList(parentEvent)));
		}
		return this.set(Event.class, new ModelElementList<>());
	}

	public ModelElementList<EventBGuard> getGuards() {
		return getChildrenAndCast(Guard.class, EventBGuard.class);
	}

	public Event withGuards(final ModelElementList<EventBGuard> guards) {
		return this.set(Guard.class, guards);
	}

	public ModelElementList<EventBGuard> getAllGuards() {
		ModelElementList<EventBGuard> acts = new ModelElementList<>();
		if (this.getParentEvent() != null) {
			acts = acts.addMultiple(this.getParentEvent().getAllGuards());
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
		if (this.getParentEvent() != null) {
			acts = acts.addMultiple(this.getParentEvent().getAllActions());
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

	/**
	 * @deprecated Use {@link #hasParent()}  instead.
	 */
	@Deprecated
	public Inheritance isExtended() {
		return inheritance;
	}


	public Inheritance hasParent() {
		return inheritance;
	}


	public Event withName(final String name) {
		return new Event(name, type, inheritance, getChildren());
	}

	public Event changeType(EventType type) {
		return new Event(name, type, inheritance, getChildren());
	}

	public Event changeInheritance(Inheritance inheritance){
		return new Event(name, type, inheritance, getChildren());
	}

	/**
	 * @deprecated Use {@link #changeInheritance(Inheritance)} ()} instead.
	 */
	@Deprecated
	public Event toggleExtended(boolean extended) {
		if (Inheritance.EXTENDS == this.inheritance && extended) {
			return this;
		}
		return new Event(name, type, Inheritance.EXTENDS, getChildren());
	}

	/**
	 * @return only the "header", i.e., name, termination and extension information of the event
	 */
	public Event stripBody(){
		return new Event(this.name, this.type, this.inheritance).withParentEvent(this.getParentEvent());
	}


	@Override
	public boolean equals(Object o){
		if(o instanceof Event){
			return ((Event) o).getName().equals(name) && ((Event) o).type == type && ((Event) o).inheritance == inheritance;
		} //TODO add body comparison
		return false;
	}



	@Override
	public String toString() {
		return getName() + " " + getType() + " " + hasParent() + " " + getParentEvent();
	}


}
