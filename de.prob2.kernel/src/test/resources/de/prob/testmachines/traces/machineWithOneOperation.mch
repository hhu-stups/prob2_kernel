MACHINE machineWithOneOperation

VARIABLES  floors

INVARIANT  floors : 0..100 /* NAT */

INITIALISATION floors := 0

OPERATIONS

	inccc = PRE floors<2 THEN floors := floors + 1 END


END
