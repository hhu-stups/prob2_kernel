package de.prob.model.eventb

import de.prob.model.eventb.Event.EventType
import de.prob.model.representation.ModelElementList

class MachineModifier {
	private UUID uuid = UUID.randomUUID()
	private ctr = 0
	EventBMachine machine

	def MachineModifier(EventBMachine machine) {
		this.machine = machine
	}

	/**
	 * Adds a variable to the given machine.
	 * @param variable to be added
	 * @param typingInvariant to specify the type of the variable
	 * @param initialisationAction to specify how the variable should be initialized
	 * @return the new {@link VariableBlock} that has been created containing the new elements
	 */
	def VariableBlock addVariable(String variable, String typingInvariant, String initialisationAction) {
		def var = new EventBVariable(variable, null)
		def c = ctr++
		machine.variables << var
		def inv = new EventBInvariant("generated-typing-inv-${uuid.toString()}${c}", typingInvariant, false, Collections.emptySet())
		machine.invariants << inv
		Event initialisation = machine.events.INITIALISATION
		def init = new EventBAction(initialisation, "generated-init-${uuid.toString()}-${c}", initialisationAction, Collections.emptySet())
		initialisation.actions << init
		def x = new VariableBlock(var, inv, init)
	}

	/**
	 * Removes a variable and its typing/initialisation information from the machine
	 * @param block containing the added variable, typing invariant, and initialisation
	 * @return if the removal of all elements from the machine was successful.
	 */
	def boolean removeVariableBlock(VariableBlock block) {
		def a = machine.variables.remove(block.getVariable())
		def b = machine.invariants.remove(block.getTypingInvariant())
		def c = machine.events.INITIALISATION.actions.remove(block.getInitialisationAction())
		return a & b & c
	}

	/**
	 * Adds an invariant to a given machine
	 * @param predicate to be added as an invariant
	 * @return the {@link EventBInvariant} object that has been added to the machine
	 */
	def EventBInvariant addInvariant(String predicate) {
		def invariant = new EventBInvariant("generated-inv-{uuid.toString()}-${ctr++}", predicate, false, Collections.emptySet())
		machine.invariants << invariant
		invariant
	}

	/**
	 * Removes an invariant from the machine.
	 * @param invariant to be removed
	 * @return whether or not the removal was successful
	 */
	def boolean removeInvariant(EventBInvariant invariant) {
		return machine.invariants.remove(invariant)
	}


	/**
	 * This method searches for the {@link Event} with the specified name in the
	 * {@link EventBMachine}. If found, an {@link EventModifier} is created to allow the
	 * modification of the specified event. Otherwise, an {@link Event} is added to the
	 * machine via {@link #addEvent(String)}
	 * @param name of event to be added
	 * @return an {@link EventModifier} to modify the specified {@link Event}
	 */
	def EventModifier getEvent(String name) {
		if (machine.events.hasProperty(name)) {
			return new EventModifier(machine.events.getProperty(name))
		}
		addEvent(name)
	}

	/**
	 * Creates a new {@link Event} object and adds it to the machine.
	 * An {@link EventModifier} object is then created and returned to allow
	 * the modification of the specified {@link Event}.
	 * NOTE: This will override an existing {@link Event} in the model with
	 * the same name. To modify an existing {@link Event} use {@link #getEvent(String)}
	 * @param name of event to be added
	 * @return an {@link EventModifier} to modify the specified {@link Event}
	 */
	def EventModifier addEvent(String name) {
		Event event = new Event(machine, name, EventType.ORDINARY)
		event.addActions(new ModelElementList<EventBAction>())
		event.addGuards(new ModelElementList<EventBGuard>())
		event.addParameters(new ModelElementList<EventParameter>())
		event.addRefines(new ModelElementList<Event>())
		event.addWitness(new ModelElementList<Witness>())
		machine.events << event
		return new EventModifier(event)
	}

	/**
	 * Removes an event from the machine.
	 * @param event to be removed
	 * @return whether or not the removal was successful
	 */
	def boolean removeEvent(Event event) {
		return machine.events.remove(event)
	}
}
