import java.nio.file.Paths

import de.prob.statespace.Trace

final path = Paths.get(dir, "machines", "LoadStdLibTest.mch")
final s = api.b_load(path.toString())
assert s.model.allFiles[0] == path
assert s.model.allFiles.any {it.fileName.toString() == "LibraryMeta.def"}
final t = new Trace(s)

"the standard library is present"
