import java.nio.file.Paths

import de.prob.model.classicalb.ClassicalBModel
import de.prob.model.eventb.EventBModel
import de.prob.model.representation.AbstractModel
import de.prob.model.representation.DependencyGraph.ERefType
import de.prob.statespace.StateSpace
import de.prob.statespace.Trace

/*
 * Tests loading of EventB to make sure that all components are there. 
 */

final s = api.eventb_load(Paths.get(dir, "machines", "Lift", "lift0.bcm").toString())
assert s != null

final l = s.totalNumberOfErrors

final m = s as EventBModel
assert m.is(s as AbstractModel)

var thrown1 = false
try {
	s as ClassicalBModel
} catch (ClassCastException ignored) {
	thrown1 = true
}
if (!thrown1) {
	throw new AssertionError("Event-B StateSpace was cast to ClassicalBModel - this isn't supposed to be possible!")
}

var thrown2 = false
try {
	s as Number
} catch (ClassCastException ignored) {
	thrown2 = true
}
if (!thrown2) {
	throw new AssertionError("StateSpace was cast to Number - this isn't supposed to be possible!")
}

final t = s as Trace
assert s.is(t.stateSpace)
assert s.is(t as StateSpace)
assert t.model.is(t as AbstractModel)
assert t.model.is(t as EventBModel)

var thrown3 = false
try {
	t as ClassicalBModel
} catch (ClassCastException ignored) {
	thrown3 = true
}
if (!thrown3) {
	throw new AssertionError("Event-B Trace was cast to ClassicalBModel - this isn't supposed to be possible!")
}

var thrown4 = false
try {
	t as Number
} catch (ClassCastException ignored) {
	thrown4 = true
}
if (!thrown4) {
	throw new AssertionError("Trace was cast to Number - this isn't supposed to be possible!")
}

assert ERefType.SEES == m.getRelationship("lift0", "levels")

final levels = m.levels
assert levels != null
final constants = levels.constants

assert constants != null
assert constants.size() == 6
assert constants.collect { it.name } == [
	"L0",
	"L1",
	"L2",
	"L3",
	"down",
	"up"
]
assert !constants.any {it.isAbstract}

final axioms = levels.axioms
assert axioms != null
assert axioms.size() == 3

final sets = levels.sets
assert sets != null
assert sets.size() == 1
assert sets[0].name == "levels"

final lift0 = m.lift0
assert lift0 != null

final variables = lift0.variables
assert variables != null
assert variables.size() == 1
assert variables[0].name == "level"

final invariants = lift0.invariants
assert invariants != null
assert invariants.size() == 1

final events = lift0.events
assert events != null
assert events.size() == 4
assert events.collect { it.name } == [
	"INITIALISATION",
	"up",
	"down",
	"randomCrazyJump"
]

final up = events.up
assert up != null
assert up.guards.size() == 1
assert up.actions.size() == 1

final randomCrazyJump = events.randomCrazyJump
assert randomCrazyJump != null
assert randomCrazyJump.parameters.size() == 1
assert randomCrazyJump.parameters[0].name == "prm1"
assert randomCrazyJump.guards.size() == 1
assert randomCrazyJump.actions.size() == 1

final variant = lift0.variant
assert variant == null

assert levels in lift0.sees

assert l == s.totalNumberOfErrors

"When an EventB file is loaded (Lift example), the model elements are accessible."
