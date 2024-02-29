MACHINE Island


CONSTANTS maxPerson
PROPERTIES maxPerson = 20 
VARIABLES currentPersons
INVARIANT currentPersons : 0..maxPerson
INITIALISATION currentPersons := 0
OPERATIONS 
  
  arrive_by_boat(persons) = PRE currentPersons + persons <= maxPerson 
                            THEN
                                currentPersons := currentPersons + persons
                            END;

  arrive_by_foot = PRE currentPersons + 1 <= maxPerson 
                   THEN
                        currentPersons := currentPersons + 1
                   END;

  leave = PRE currentPersons - 1 > 0 
          THEN
             currentPersons := currentPersons -1 
          END
END//MACHINE