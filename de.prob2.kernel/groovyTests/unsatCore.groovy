import java.nio.file.Paths

import de.prob.animator.command.UnsatCoreCommand
import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.FormulaExpand

final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString()) // machine is not needed...

final pred = new ClassicalB("x > 7 & 1=4")
final c = new UnsatCoreCommand(pred, [])

s.execute(c)
assert c.core.prettyPrint == new ClassicalB("1=4", FormulaExpand.EXPAND).prettyPrint

" The UnsatCoreCommand and MinimumUnsatCoreCommand work as expected."
