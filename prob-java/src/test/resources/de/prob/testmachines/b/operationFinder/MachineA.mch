MACHINE MachineA
CONSTANTS c
PROPERTIES c : 1..10 
VARIABLES x
INVARIANT x : 1..c
INITIALISATION x := 1
OPERATIONS 
  Inc = SELECT x < c THEN x := x + 1 END;
  Inc2 = x := x + 1;
  Inc3 = x := x + 1
END//MACHINE
