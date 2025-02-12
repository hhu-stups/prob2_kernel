MACHINE LargeProperties
CONSTANTS a, b
PROPERTIES a <: 1..8 & b <: 1..2
VARIABLES v1
INVARIANT v1 : 1..3
INITIALISATION v1 := 1
OPERATIONS
  op1 = PRE v1 < 3 THEN v1 := succ(v1) END;
  op2 = PRE v1 > 1 THEN v1 := pred(v1) END
END
