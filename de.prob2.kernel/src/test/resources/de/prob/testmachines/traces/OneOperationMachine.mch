MACHINE OneOperationMachine


VARIABLES x
INVARIANT x : NAT
INITIALISATION x := 1
OPERATIONS 
  out <-- Inc(y, z) = PRE y : NAT & z : BOOL 
  THEN
      out := x 
  END
END//MACHINE
