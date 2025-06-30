import java.nio.file.Paths

import de.prob.statespace.Trace

final path = Paths.get(dir, "machines", "Scheduler", "Scheduler0.bcm")
final s = api.eventb_load(path.toString())
assert s.model.allFiles.size() == 5
assert s.model.allFiles[0] == path
assert s.model.allFiles[1] == path.resolveSibling("Processes.bpo")
assert s.model.allFiles[2] == path.resolveSibling("Processes.bps")
assert s.model.allFiles[3] == path.resolveSibling("Scheduler0.bpo")
assert s.model.allFiles[4] == path.resolveSibling("Scheduler0.bps")

def c = s as Trace
assert c.currentState == s.root
assert c.currentState.toString() == "root"
c = c.anyEvent()
st = c.currentState
assert st != s.root
c = c.anyEvent()
assert c.currentState != st

"A .bcm file (Scheduler0.bcm) was loaded and some steps were made"
