MACHINE Lift

VARIABLES  floors

INVARIANT  floors : 0..100 /* NAT */

INITIALISATION floors := 0

OPERATIONS

	inc(x) = PRE x:BOOL & floors<100 THEN floors := floors + 1 END ;
	dec = PRE floors>0 THEN floors := floors - 1 END;
	out <-- getfloors = BEGIN out := floors END

END
