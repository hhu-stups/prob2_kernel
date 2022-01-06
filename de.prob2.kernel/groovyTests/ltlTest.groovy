import java.nio.file.Paths

import de.prob.animator.domainobjects.LTL
import de.prob.check.LTLChecker
import de.prob.check.LTLCounterExample
import de.prob.check.LTLError
import de.prob.check.LTLOk

final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString())

final res1 = new LTLChecker(s, new LTL("G({ TRUE = TRUE })")).call()
assert res1 instanceof LTLOk
final res2 = new LTLChecker(s, new LTL("G({ card(active) = 0 })")).call()
assert res2 instanceof LTLCounterExample
def t = res2.getTrace(s)
assert t.transitionList.size() == 3 
// used to be == 8, but with safety mc you just need three steps: init,new,ready

final res3 = new LTLChecker(s, new LTL("G({ active < 7 })")).call()
assert res3 instanceof LTLError

"ltl formulas can be checked"
