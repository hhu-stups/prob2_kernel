import java.nio.file.Paths

import de.prob.model.eventb.Context
import de.prob.model.eventb.Event
import de.prob.model.eventb.EventBInvariant
import de.prob.model.eventb.EventBMachine
import de.prob.model.eventb.EventParameter
import de.prob.model.eventb.ProofObligation
import de.prob.model.eventb.Witness
import de.prob.model.eventb.translate.ModelToXML
import de.prob.model.representation.Action
import de.prob.model.representation.BEvent
import de.prob.model.representation.Guard
import de.prob.model.representation.Invariant
import de.prob.model.representation.Machine
import de.prob.model.representation.ModelElementList
import de.prob.model.representation.Variable
import de.prob.scripting.EventBFactory

static <T> ModelElementList<T> mergeModelElementLists(ModelElementList<T> list1, ModelElementList<T> list2) {
	final merged = new LinkedHashSet(list1)
	merged.addAll(list2)
	return new ModelElementList<>(merged)
}

static EventBInvariant prefixInvariantName(String namespace, EventBInvariant invariant) {
	new EventBInvariant(namespace + "__" + invariant.name, invariant.predicate, invariant.theorem, invariant.comment)
}

static Event mergeOneRefinementLevelForEvent(EventBMachine absMachine, Event refEvent) {
	if (refEvent.refines.isEmpty()) {
		return refEvent
	}
	assert refEvent.refines.size() == 1
	final absEvent = absMachine.getEvent(refEvent.refines.first().name)
	
	def merged = refEvent
	merged = merged.toggleExtended(absEvent.extended)
	merged = merged.set(Event, absEvent.refines)
	// FIXME Why is this inherited automatically? This may be unintended behavior in ProB 2's Rodin XML reader.
	if (false && refEvent.extended) {
		merged = merged.set(Guard, absEvent.guards.addMultiple(refEvent.guards))
		merged = merged.set(Action, absEvent.actions.addMultiple(refEvent.actions))
		merged = merged.set(EventParameter, mergeModelElementLists(absEvent.parameters, refEvent.parameters))
	}
	merged = merged.set(Witness, absEvent.witnesses) // TODO Is it safe to just delete witnesses and abstract parameters?
	merged
}

static EventBMachine mergeOneRefinementLevel(EventBMachine refMachine) {
	if (refMachine.refines.isEmpty()) {
		return refMachine
	}
	assert refMachine.refines.size() == 1
	final absMachine = refMachine.refines.first()
	
	def merged = refMachine
	merged = merged.set(Machine, absMachine.refines)
	merged = merged.set(Context, mergeModelElementLists(absMachine.sees, refMachine.sees))
	merged = merged.set(Variable, mergeModelElementLists(absMachine.variables, refMachine.variables)) // TODO Handle data refinement (variables in abstract but not refined machine)
	merged = merged.set(Invariant, new ModelElementList<EventBInvariant>(absMachine.invariants.collect {prefixInvariantName(absMachine.name, it)}).addMultiple(refMachine.invariants))
	// TODO Do we need to transform the variant somehow (if any)?
	merged = merged.set(ProofObligation, new ModelElementList<>()) // TODO Can we keep PO information somehow?
	final mergedEvents = new ModelElementList<>(refMachine.events.collect {mergeOneRefinementLevelForEvent(absMachine, it)})
	// Both BEvent and Event need to have the same value, because ProB 2 hates us all
	merged = merged.set(BEvent, mergedEvents)
	merged = merged.set(Event, mergedEvents)
	merged
}

final dir = Paths.get("/Users/Shared/Uni/SHK/ProB misc/IVOIRE/Abstraction_Examples/Train_Abrial")
final eventBFactory = injector.getInstance(EventBFactory)

final extracted = eventBFactory.extract(dir.resolve("Train_Abrial").resolve("train_4.bum").toString())
final model = extracted.model
if (extracted.mainComponent !instanceof EventBMachine) {
	println("Can only flatten a machine, not a context")
	return
}
final mainMachine = extracted.mainComponent as EventBMachine

def mergedMachine = mainMachine
def machine = mainMachine
while (!machine.refines.isEmpty()) {
	mergedMachine = mergeOneRefinementLevel(mergedMachine)
	machine = machine.refines.first()
}

final mergedModel = model.set(Machine, new ModelElementList<>([mergedMachine])).calculateDependencies()
new ModelToXML().writeToRodin(mergedModel, "Train_Abrial_flat", dir.toString())
