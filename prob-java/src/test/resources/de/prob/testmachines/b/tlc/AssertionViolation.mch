MACHINE AssertionViolation
ASSERTIONS v1 : 1..9
VARIABLES v1
INVARIANT v1 : 1..10
INITIALISATION v1 := 1
OPERATIONS
  op1 = PRE v1 < 6 THEN v1 := succ(v1) END;
  op2 = PRE v1 > 4 THEN v1 := pred(v1) END;
  op3 = PRE v1 = 6 THEN v1 := 10 END
END
