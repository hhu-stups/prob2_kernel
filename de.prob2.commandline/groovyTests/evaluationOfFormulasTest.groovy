import java.nio.file.Paths

import de.hhu.stups.prob.translator.BAtom
import de.hhu.stups.prob.translator.BNumber
import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.EvalResult
import de.prob.animator.domainobjects.TranslatedEvalResult
import de.prob.statespace.Trace

final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString())
def h = new Trace(s)
h = h.add(0)
h = h.add(3)
assert h.currentState.id == "2"
assert s[3].eval("2-1" as ClassicalB).toString() == '1'
res = s[0].eval("waiting" as ClassicalB).toString()
assert res == '{}' || res == '\u2205'
assert s[2].eval("waiting" as ClassicalB).toString() == '{PID2}'

final formula = "x : waiting & x = PID2 & y : NAT & y = 1" as ClassicalB
final res = s[2].eval(formula)
assert res instanceof EvalResult
assert res.value == "TRUE"
assert res.solutions.containsKey("x")
assert res.solutions.containsKey("y")
assert res.x == "PID2"
assert res.y == "1"

final t = res.translate()
assert t != null && t instanceof TranslatedEvalResult
assert t.value.value == true
assert t.solutions.containsKey("x")
assert t.solutions.containsKey("y")
assert t.x == new BAtom("PID2")
assert t.y == BNumber.of(1)

"Evaluation of formulas works (scheduler.mch)"
