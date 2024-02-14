import java.nio.file.Paths

import de.prob.animator.domainobjects.ClassicalB
import de.prob.model.classicalb.ClassicalBModel
import de.prob.model.eventb.EventBModel
import de.prob.model.representation.AbstractModel
import de.prob.statespace.StateSpace
import de.prob.statespace.Trace

final f = "1 + 2" as ClassicalB
final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString())

final m = s as ClassicalBModel
assert m.is(s as AbstractModel)

var thrown1 = false
try {
	s as EventBModel
} catch (ClassCastException ignored) {
	thrown1 = true
}
if (!thrown1) {
	throw new AssertionError("Classical B StateSpace was cast to EventBModel - this isn't supposed to be possible!")
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

def t = s as Trace
assert s.is(t.stateSpace)
assert s.is(t as StateSpace)
assert t.model.is(t as AbstractModel)
assert t.model.is(t as ClassicalBModel)

var thrown3 = false
try {
	t as EventBModel
} catch (ClassCastException ignored) {
	thrown3 = true
}
if (!thrown3) {
	throw new AssertionError("Classical B Trace was cast to EventBModel - this isn't supposed to be possible!")
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

s.subscribe(m, f)
t.currentState.explore()

// States can be accessed by integer ID via the element access operator
assert s.getState(0).is(s[0])

def h = t.add 0

final idAt0 = h.currentState
h = h.add 3
assert h.currentState == s.getState("2")
h = h.add 8

final idAt8 = h.currentState
assert idAt0 == idAt8
def h2 = new Trace(s)
h2 = h2.add 0
h2 = h2.add 3
h2 = h2.add 9

s.subscribe(m, m.scheduler.variables.collect {it.formula})
final varsAt6 = s[6].explore().values
res = varsAt6[m.scheduler.variables.waiting.formula].value
assert res == "{}" || res == "\u2205"
assert varsAt6[m.scheduler.variables.active.formula].value == "{PID2}"
res2 = varsAt6[m.scheduler.variables.ready.formula].value
assert res2 == "{}" || res2 == "\u2205"
final f1 = "1+1=2" as ClassicalB
s.subscribe(m, f1)
assert h2.currentState.values.containsKey(f1)
assert h2.currentState.values[f1].value == "TRUE"

final root = s.root
assert s.isValidOperation(s[0],"new", "pp = PID1")
assert !s.isValidOperation(s[0],"blah", "TRUE = TRUE")
assert s.isValidOperation(root,"\$initialise_machine", "TRUE = TRUE")
assert !s.isValidOperation(root,"\$setup_constants", "TRUE = TRUE")

t = s as Trace
assert t.canExecuteEvent("\$initialise_machine", [])
// for invoking method, names can be escaped with a $ if it is needed
final t2 = t.$$initialise_machine("TRUE = TRUE")
t = t.$initialise_machine("TRUE = TRUE")
assert t.currentState == t2.currentState
assert t.canExecuteEvent("new", ["pp = PID1"])
t = t.new("pp = PID1")
assert !t.canExecuteEvent("blah", [])

"Some attributes of the scheduler model were tested"
