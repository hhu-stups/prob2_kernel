MACHINE Lift

ABSTRACT_VARIABLES  current_level

INVARIANT  current_level : 0..99 /* NAT */

ASSERTIONS current_level>=0
INITIALISATION current_level := 4

OPERATIONS

	inc = PRE current_level<99 THEN current_level := current_level + 1 END ;
	
	dec = BEGIN current_level := current_level - 1 END;
	/* 
	result  <-- ground = BEGIN result := bool(current_level=0) END
	dec = PRE current_level>0 THEN current_level := current_level - 1 END ;  */
	
	result  <-- ground = 
	            IF current_level=0 THEN
	             result := TRUE
	            ELSE
	             result := FALSE
	            END
	
END




