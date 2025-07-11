import java.nio.file.Paths

import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.FormulaExpand
import de.prob.statespace.Trace

final s = api.b_load(Paths.get(dir, "machines", "MultipleExample.mch").toString(), ["MAX_DISPLAY_SET": "1"])
def t = (s as Trace).$initialise_machine()
t = t.Crazy2("p1 = 5", "p2 = {2,4,6,8}")
final trans = t.currentTransition
assert trans.evaluate(FormulaExpand.TRUNCATE).rep == "4,{(5|->#4:{2,...,8})},6 <-- Crazy2(5,#4:{2,...,8})"
assert trans.evaluate(FormulaExpand.EXPAND).rep == "4,{(5|->{2,4,6,8})},6 <-- Crazy2(5,{2,4,6,8})"
assert trans.evaluate(FormulaExpand.TRUNCATE).rep == "4,{(5|->{2,4,6,8})},6 <-- Crazy2(5,{2,4,6,8})"

final truncated = new ClassicalB("{2,4,6,8,10}", FormulaExpand.TRUNCATE)
assert t.evalCurrent(truncated).value == "{2,...}"
final expanded = new ClassicalB("{2,4,6,8,10}", FormulaExpand.EXPAND)
assert t.evalCurrent(expanded).value == "{2,4,6,8,10}"

"Expanding and truncating a formula works correctly"
