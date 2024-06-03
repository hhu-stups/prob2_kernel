import java.nio.file.Paths

import de.prob.statespace.Trace

final path = Paths.get(dir, "machines", "Lift", "levels.buc")
final s = api.eventb_load(path.toString()) 
assert s.model.allFiles.size() == 1
assert s.model.allFiles[0] == path.resolveSibling("levels.bcc")

def c = s as Trace
assert c.currentState == s.root
assert c.currentState.toString() == "root"
c = c.$setup_constants()
final st = c.currentState
assert st != s.root
c = c.$initialise_machine()
assert c.currentState != st

"A .buc file can be loaded and does not result in an empty machine"
