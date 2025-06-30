import java.nio.file.Paths

import de.prob.statespace.Trace

final path = Paths.get(dir, "machines", "counter", "machine.bum.bum")
final s = api.eventb_load(path.toString())
assert s.model.allFiles.size() == 3
assert s.model.allFiles[0] == path.resolveSibling("machine.bum.bcm")
assert s.model.allFiles[1] == path.resolveSibling("machine.bum.bpo")
assert s.model.allFiles[2] == path.resolveSibling("machine.bum.bps")

def c = s as Trace
assert c.currentState == s.root
assert c.currentState.toString() == "root"
c = c.anyEvent()
final st = c.currentState
assert st != s.root
c = c.anyEvent()
assert c.currentState != st

"A .bum.bum file can be loaded and does not result in an empty machine"
