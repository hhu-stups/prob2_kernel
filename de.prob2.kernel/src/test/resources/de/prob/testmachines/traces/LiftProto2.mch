MACHINE LiftProto2

VARIABLES  levels

INVARIANT  levels : 0..100 /* NAT */

INITIALISATION levels := 0

OPERATIONS

	inc(x,y,z) = PRE x : NAT & y : NAT & levels<100 
			THEN 
			levels := levels + x + y + z
			END ;
            
	dec1 = PRE levels>0 THEN levels := levels - 1 END;

    	dec2 = PRE levels>0 THEN levels := levels - 1 END;

	out <-- getlevels = BEGIN out := levels END

END