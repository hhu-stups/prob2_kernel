import java.nio.file.Paths

import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.EvaluationErrorResult
import de.prob.statespace.Trace

final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString())

def t = s as Trace
t = t.$initialise_machine()
t = t.new("pp=PID1")
final f = "1+1" as ClassicalB
final f2 = "active" as ClassicalB
final registered = s.registerFormulas([f, f2])
assert (registered as HashSet) == ([f, f2] as HashSet)
assert s.registeredFormulas.contains(f)
assert s.registeredFormulas.contains(f2)

final results = t.currentState.evalFormulas([f, f2])
assert results[f].value == "2"
res = results[f2].value
assert res == "{}" || res == "\u2205"

final results2 = s.root.evalFormulas([f, f2])
assert results2[f].value == "2"
assert results2[f2] instanceof EvaluationErrorResult

s.unregisterFormulas([f, f2])
assert !s.registeredFormulas.contains(f)
assert !s.registeredFormulas.contains(f2)

"It is possible to register formulas and asynchronously evaluate them later"
