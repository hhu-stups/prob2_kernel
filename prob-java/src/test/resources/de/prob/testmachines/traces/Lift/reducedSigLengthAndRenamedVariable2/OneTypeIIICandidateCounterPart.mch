MACHINE OneTypeIIICandidateCounterPart

VARIABLES  levels

INVARIANT  levels : 0..100 /* NAT */

INITIALISATION levels := 0

OPERATIONS

	inc(x,y,z) = PRE x < 100 - levels & levels<100  & y : NAT & z : NAT THEN levels := levels + 1*(x+y+z) END ;
	dec = PRE levels>0 THEN levels := levels - 1 END;
	out <-- getlevels = BEGIN out := levels END

END
