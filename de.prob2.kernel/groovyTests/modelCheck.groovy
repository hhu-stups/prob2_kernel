import java.nio.file.Paths

import de.prob.check.ConsistencyChecker
import de.prob.check.ModelCheckErrorUncovered
import de.prob.check.ModelCheckOk
import de.prob.check.ModelCheckingOptions

final s1 = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString())

final checker = new ConsistencyChecker(s1)
checker.call()
final res1 = checker.result
assert res1 instanceof ModelCheckOk

final s2 = api.eventb_load(Paths.get(dir, "machines", "InvalidModel", "createErrors.bcm").toString())

final res2 = new ConsistencyChecker(s2, new ModelCheckingOptions().checkInvariantViolations(true)).call()
assert res2 instanceof ModelCheckErrorUncovered
assert res2.getMessage() == "Invariant violation found."
assert res2.getTrace(s2) != null

final res3 = new ConsistencyChecker(s2, new ModelCheckingOptions().checkDeadlocks(true)).call()
assert res3 instanceof ModelCheckErrorUncovered
assert res3.getMessage() == "Deadlock found."
assert res3.getTrace(s2) != null

"model checking works correctly"
