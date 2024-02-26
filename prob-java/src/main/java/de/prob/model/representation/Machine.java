package de.prob.model.representation;

import java.util.Map;

/**
 * <p>Common superclass for classical B and Event-B machines.</p>
 * <p>
 * This superclass doesn't have methods for accessing sets and constants,
 * because Event-B does not allow them in machines
 * (they have to be in a separate context).
 * The interface {@link ConstantsComponent} can be used to generically access
 * an Event-B context or the constant parts of a classical B machine.
 * </p>
 */
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

	public abstract ModelElementList<? extends Variable> getVariables();

	public abstract ModelElementList<? extends Invariant> getInvariants();

	// TODO Perhaps also add getAllInvariants (currently only implemented for Event-B)?

	public abstract ModelElementList<? extends BEvent> getEvents();

	public abstract BEvent getEvent(final String eventName);

	@Override
	public String toString() {
		return name;
	}
}
