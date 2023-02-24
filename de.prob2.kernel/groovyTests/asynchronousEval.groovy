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
assert s.registeredFormulas.containsKey(f)
assert s.registeredFormulas.containsKey(f2)
final rf = s.registeredFormulas[f]
final rf2 = s.registeredFormulas[f2]

final results = t.currentState.evalFormulas([rf, rf2])
assert results[rf].value == "2"
res = results[rf2].value
assert res == "{}" || res == "\u2205"

final results2 = s.root.evalFormulas([rf, rf2])
assert results2[rf].value == "2"
assert results2[rf2] instanceof EvaluationErrorResult

// Can unregister either via the original IEvalElement or the RegisteredFormula
s.unregisterFormulas([f, rf2])
assert !s.registeredFormulas.containsKey(f)
assert !s.registeredFormulas.containsKey(f2)

"It is possible to register formulas and asynchronously evaluate them later"
