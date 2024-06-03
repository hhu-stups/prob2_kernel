import java.nio.file.Paths

import de.prob.animator.domainobjects.EvalResult
import de.prob.animator.domainobjects.WDError
import de.prob.statespace.Trace

final path = Paths.get(dir, "machines", "TheoryWDTest", "TestTheoryWD.buc")
final s = api.eventb_load(path.toString())
assert s.model.allFiles.size() == 3
assert s.model.allFiles[0] == path.resolveSibling("TestTheoryWD.bcc")
assert s.model.allFiles[1] == path.resolveSibling("TheoryPath.tcl")
assert s.model.allFiles[2] == path.resolveSibling("SimpleWDTheory.dtf")

def t = s as Trace
t = t.$setup_constants()

final res1 = t.evalCurrent("mu({123})")
assert res1 instanceof EvalResult
assert res1.value == "123"

final res2 = t.evalCurrent("mu({123, 456})")
assert res2 instanceof WDError

"Theories in a Rodin project can be loaded and used in formulas"
