MACHINE OneTypeIIICandidateCounterPart

VARIABLES  levels

INVARIANT  levels : 0..100 /* NAT */

INITIALISATION levels := 0

OPERATIONS

	inc(a,b,c) = PRE a < 100 - levels & levels<100  & b : NAT & c : NAT THEN levels := levels + 1*(a+b+c) END ;
	dec = PRE levels>0 THEN levels := levels - 1 END;
	out <-- getlevels = BEGIN out := levels END

END
