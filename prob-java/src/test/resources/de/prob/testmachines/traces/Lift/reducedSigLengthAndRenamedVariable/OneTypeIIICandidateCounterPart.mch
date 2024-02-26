MACHINE OneTypeIIICandidateCounterPart

VARIABLES  floors

INVARIANT  floors : 0..100 /* NAT */

INITIALISATION floors := 0

OPERATIONS

	inc(a,b,c) = PRE a < 100 - floors & floors<100  & b : NAT & c : NAT THEN floors := floors + 1*(a+b+c) END ;
	dec = PRE floors>0 THEN floors := floors - 1 END;
	out <-- getfloors = BEGIN out := floors END

END
