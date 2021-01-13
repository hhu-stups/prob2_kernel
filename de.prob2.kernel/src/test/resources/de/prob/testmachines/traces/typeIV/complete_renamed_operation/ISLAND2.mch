MACHINE ISLAND2

CONSTANTS
    maxCars
PROPERTIES
    maxCars = 5

VARIABLES
    cars
INVARIANT
    cars : NAT
INITIALISATION
    cars := 0

OPERATIONS
    drive_on = PRE cars < maxCars THEN cars := cars + 1 + 0 END;

    off = PRE cars > 0 THEN cars := cars - 1 END

END