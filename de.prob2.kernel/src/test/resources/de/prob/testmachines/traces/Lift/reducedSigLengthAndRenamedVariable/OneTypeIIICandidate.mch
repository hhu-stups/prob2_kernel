MACHINE OneTypeIIICandidate

VARIABLES  levels

INVARIANT  levels : 0..100 /* NAT */

INITIALISATION levels := 0

OPERATIONS

	inc(x) = PRE x < 100 - levels & levels<100 THEN levels := levels + 1 END ;
	dec = PRE levels>0 THEN levels := levels - 1 END;
	out <-- getlevels = BEGIN out := levels END

END
