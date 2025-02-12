MACHINE Deadlock
VARIABLES v1,v2
INVARIANT v1 : 1..4 & v2 : 5..8
INITIALISATION v1 := 1 || v2 := 5
OPERATIONS
  op1 = PRE v1 < 4 THEN v1 := succ(v1) END;
  op2 = PRE v2 < 8 THEN v2 := succ(v2) END
END
