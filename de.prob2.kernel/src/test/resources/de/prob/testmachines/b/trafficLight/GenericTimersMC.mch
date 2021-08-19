MACHINE GenericTimersMC(TIMERS)
// A variation of GenericTimers for model checking
VARIABLES
  curDeadlines

INVARIANT
  curDeadlines : TIMERS +-> NATURAL


INITIALISATION
  curDeadlines := {}

OPERATIONS
  
  AbsoluteSetDeadline(timer,deadline) = PRE timer:TIMERS & deadline:NATURAL THEN
     curDeadlines(timer) := deadline
  END;
  
  AddDeadline(timer,deadline) = PRE timer:TIMERS & deadline:NATURAL THEN
     curDeadlines(timer) := deadline
  END;

  TimeoutDeadline(timer, deadline) = PRE timer:TIMERS & deadline:NATURAL THEN
     curDeadlines := curDeadlines \ {timer |-> deadline}
  END;
  
  // Increase time without exceeding a deadline
  IncreaseTime(delta) = SELECT delta:NATURAL & 
      (curDeadlines/={} => delta <= min(ran(curDeadlines))) THEN
     curDeadlines := %x.(x:dom(curDeadlines)|curDeadlines(x)-delta)
  END;
  
  IncreaseTimeUntilDeadline(timer,delta) =
   SELECT delta:NATURAL & 
          timer : dom(curDeadlines) &
          delta = min(ran(curDeadlines)) & 
          delta = curDeadlines(timer) THEN
     curDeadlines := %x.(x:dom(curDeadlines) \ {timer}|curDeadlines(x)-delta)
  END

END
  
  