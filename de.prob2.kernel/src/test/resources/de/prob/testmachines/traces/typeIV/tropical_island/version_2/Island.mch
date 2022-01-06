MACHINE Island


CONSTANTS maxPerson
PROPERTIES maxPerson = 20 
VARIABLES currentPersons
INVARIANT currentPersons : 0..maxPerson
INITIALISATION currentPersons := 0
OPERATIONS 
  
  arrive_by_boat(persons) = PRE persons : NAT & currentPersons + persons <= maxPerson 
                            THEN
                                currentPersons := currentPersons + persons
                            END;

  arrive_by_foot = PRE currentPersons + 1 <= maxPerson 
                   THEN
                        currentPersons := currentPersons + 1
                   END;

  leave(persons) = PRE persons = 5 or persons = 1 & currentPersons - persons > 0  
          THEN
             currentPersons := currentPersons -persons 
          END
END//MACHINE