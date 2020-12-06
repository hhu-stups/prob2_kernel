MACHINE LiftProto

VARIABLES  floors

INVARIANT  floors : 0..100 /* NAT */

INITIALISATION floors := 0

OPERATIONS

	inc(x,y) = PRE x : NAT & y : NAT & floors<100 
			THEN 
			floors := floors + x + y 
			END ;
	dec = PRE floors>0 THEN floors := floors - 1 END;
	out <-- getfloors = BEGIN out := floors END

END