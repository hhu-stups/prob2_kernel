MACHINE ExampleMachine
DEFINITIONS
  SET_PREF_MAX_OPERATIONS == 100;
  MAX_OPERATIONS_dec == 0;
  MAX_OPERATIONS_dec_many == 0;
  MAX_OPERATIONS_set2 == 0;
  MAX_OPERATIONS_set_nondet2 == 0
VARIABLES
  x
INVARIANT
  x : 1..10
INITIALISATION
  x := 5
OPERATIONS
  inc = PRE x < 10 THEN x := x + 1 END;
  dec = PRE x > 0 THEN x := x - 1 END;
  inc_many(dx) = SELECT dx > 0 & x + dx <= 10 THEN x := x + dx END;
  dec_many(dx) = SELECT dx > 0 & x - dx >= 1 THEN x := x - dx END;
  set(p) = SELECT p : 1..10 & x = 4 THEN x := p END;
  set2(p) = SELECT p : 1..10 & x = 4 THEN x := p END;
  set_nondet = BEGIN x :: 1..100 END;
  set_nondet2 = BEGIN x :: 1..100 END
END
