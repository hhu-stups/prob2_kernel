MACHINE TrafficLight

VARIABLES
cars_go,
peds_go

INVARIANT
cars_go : BOOL & peds_go : BOOL & not(cars_go = TRUE & peds_go = TRUE)

INITIALISATION
cars_go := FALSE || peds_go := FALSE

OPERATIONS
set_peds_go = PRE cars_go = FALSE
                THEN
                    peds_go := TRUE
                END;

set_peds_stop = peds_go := FALSE;


set_cars = ANY new_value WHERE  new_value : BOOL & new_value = TRUE => peds_go = FALSE
                        THEN
                            cars_go := new_value
                        END
END
