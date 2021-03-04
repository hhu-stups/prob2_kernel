import java.nio.file.Paths

import de.prob.animator.domainobjects.EventB
import de.prob.animator.domainobjects.FormulaExpand
import de.prob.check.CBCDeadlockChecker
import de.prob.check.CBCDeadlockFound
import de.prob.check.CBCInvariantChecker
import de.prob.check.CBCInvariantViolationFound
import de.prob.check.IModelCheckJob
import de.prob.check.ModelCheckOk
import de.prob.check.ModelChecker

final modelCheck = {IModelCheckJob job ->
	final checker = new ModelChecker(job)
	checker.start()
	checker.result
}


final s2 = api.eventb_load(Paths.get(dir, "Time", "clock.bcm").toString())

final res5 = modelCheck(new CBCInvariantChecker(s2))
assert res5 instanceof CBCInvariantViolationFound
final tInvViolation2 = res5.getTrace(s2)
final ops5 = tInvViolation2.getTransitionList(true, FormulaExpand.EXPAND)
assert ops5.size() == 2
assert ops5[0].name == "invariant_check_tock"
assert ops5[1].name == "tock"
assert ops5[1].parameterValues[0].toInteger() >= 0

"constraint based deadlock and invariant checking works correctly"
