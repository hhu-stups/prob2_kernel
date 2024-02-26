import de.prob.statespace.Trace

import java.nio.file.Paths

final s = api.z_load(Paths.get(dir, "machines", "comsets.tex").toString())
final t = new Trace(s)
final t2 = t.$initialise_machine().Op1().Check1().Op2().Check2()
assert t2.currentState.eval(s.model.parseFormula("test")).value == "3"

"A Z file can be loaded and animated"
