import java.nio.file.Paths

import de.prob.check.CBCInvariantChecker
import de.prob.check.CBCInvariantViolationFound

final s2 = api.eventb_load(Paths.get(dir, "Time", "clock.bcm").toString())

final res5 = new CBCInvariantChecker(s2).call()
assert res5 instanceof CBCInvariantViolationFound
final tInvViolation2 = res5.getTrace(s2)
final ops5 = tInvViolation2.transitionList
assert ops5.size() == 2
assert ops5[0].name == "invariant_check_tock"
assert ops5[1].name == "tock"
assert ops5[1].parameterValues[0].toInteger() >= 0

"constraint based deadlock and invariant checking works correctly"
