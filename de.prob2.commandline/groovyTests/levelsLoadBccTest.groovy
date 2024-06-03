import java.nio.file.Paths

import de.prob.statespace.Trace

final path = Paths.get(dir, "machines", "Lift", "levels.bcc")
final s = api.eventb_load(path.toString())
assert s.model.allFiles.size() == 1
assert s.model.allFiles[0] == path

def c = s as Trace
assert c.currentState == s.root
assert c.currentState.toString() == "root"
c = c.anyEvent()
final st = c.currentState
assert st != s.root
c = c.anyEvent()
assert c.currentState != st

"A .bcc file can be loaded and does not result in an empty machine"
