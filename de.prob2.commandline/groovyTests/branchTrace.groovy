import java.nio.file.Paths

import de.prob.animator.domainobjects.FormulaExpand
import de.prob.statespace.Trace

final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString())
def t = new Trace(s)
t = t.$initialise_machine()
t = t.new('pp = PID1')
s.evaluateTransitions(t.transitionList, FormulaExpand.EXPAND)

assert t.transitionList.collect {it.rep} == ["\$initialise_machine()", "new(PID1)"]

t = t.back()
t = t.new('pp = PID2')
s.evaluateTransitions(t.transitionList, FormulaExpand.EXPAND)
assert t.transitionList.collect {it.rep} == ["\$initialise_machine()", "new(PID2)"]

// list underneath doesn't change
t = s as Trace
final t1 = t.$initialise_machine()
final t2 = t.$initialise_machine()
s.evaluateTransitions(t1.transitionList + t2.transitionList, FormulaExpand.EXPAND)

assert t1.transitionList.collect {it.rep} == ["\$initialise_machine()"]
assert t2.transitionList.collect {it.rep} == ["\$initialise_machine()"]

"performing some animations, going back, different step results in a correct trace"
