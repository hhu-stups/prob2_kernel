MACHINE Lift4

VARIABLES  floors

INVARIANT  floors : 0..100 /* NAT */

INITIALISATION floors := 0

OPERATIONS

	inc(a) = PRE a:NAT THEN floors := floors + a END ;
	dec = PRE floors>0 THEN floors := floors - 1 END;
	out <-- getfloors = BEGIN out := floors END

END
