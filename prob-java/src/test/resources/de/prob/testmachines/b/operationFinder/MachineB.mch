MACHINE MachineB

INCLUDES MachineA

PROMOTES Inc

VARIABLES y

INVARIANT y : NAT

INITIALISATION y := 10

OPERATIONS 
 Foo = BEGIN Inc2 || y := 10 END 
END//MACHINE
