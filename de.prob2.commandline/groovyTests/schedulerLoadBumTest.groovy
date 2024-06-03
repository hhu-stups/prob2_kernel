import java.nio.file.Paths

import de.prob.statespace.Trace

final path = Paths.get(dir, "machines", "Scheduler", "Scheduler0.bum")
final s = api.eventb_load(path.toString()) 
assert s.model.allFiles.size() == 1
assert s.model.allFiles[0] == path.resolveSibling("Scheduler0.bcm")

def c = s as Trace
assert c.currentState == s.root
assert c.currentState.toString() == "root"
c = c.anyEvent()
st = c.currentState
assert st != s.root
c = c.anyEvent()
assert c.currentState != st

"A .bum file (Scheduler0.bum) was loaded and some steps were made"
