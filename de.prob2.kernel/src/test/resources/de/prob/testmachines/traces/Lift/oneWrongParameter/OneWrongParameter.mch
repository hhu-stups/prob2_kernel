MACHINE OneWrongParameter

VARIABLES  levels

INVARIANT  levels : 0..100 /* NAT */

INITIALISATION levels := 0

OPERATIONS

	inc(x, y, z) = PRE x < 100 - levels & y >= 0 & z = TRUE & levels<100 THEN levels := levels + x+y END ;
	dec = PRE levels>0 THEN levels := levels - 1 END;
	out <-- getlevels = BEGIN out := levels END

END
