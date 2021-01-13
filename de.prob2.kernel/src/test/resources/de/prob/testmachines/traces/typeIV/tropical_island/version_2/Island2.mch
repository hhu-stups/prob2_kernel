MACHINE Island2


CONSTANTS maxPerson
PROPERTIES maxPerson = 20 
VARIABLES currentPersons
INVARIANT currentPersons : 0..maxPerson
INITIALISATION currentPersons := 0
OPERATIONS 
  
  arrive_by_boat(foot_or_bike, car) = PRE car : NAT & foot_or_bike : NAT &
                                 currentPersons + foot_or_bike*1 + car*5 <= maxPerson 
                            THEN
                                currentPersons := currentPersons*1 + foot_or_bike + car*5
                            END;

  arrive_by_foot = PRE currentPersons + 1 <= maxPerson 
                   THEN
                        currentPersons := currentPersons + 1
                   END;

  leave_by_foot = PRE currentPersons - 1>= 0 
          THEN
             currentPersons := currentPersons -1 
          END;

   leave_with_car =  PRE currentPersons - 5 >= 0 
          THEN
             currentPersons := currentPersons - 5
          END

END//MACHINE