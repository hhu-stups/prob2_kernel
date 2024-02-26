import java.nio.file.Paths

import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.EnumerationWarning
import de.prob.statespace.Trace

final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString())
def t = new Trace(s)
t = t.$initialise_machine()

final x = "{w | w : NATURAL & w mod 3 = 0} /\\ {v | v : NATURAL & v mod 2 = 1} = {1}" as ClassicalB
// this now produces a time-out rather than virtual-timeout aka enumeration warning:
//final x = "{w | w : NATURAL & w mod 2 = 0} /\\ {v | v : NATURAL & v mod 2 = 1} = {}" as ClassicalB
// since prob_prolog commit: ff67eda7 delay modulo CLP(FD) propagation until WF0 set
final res = t.evalCurrent(x)
assert res instanceof EnumerationWarning

"enumeration warnings are handled"
