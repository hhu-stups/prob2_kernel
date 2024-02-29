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
  
  // Increase time without exceeding a deadline
  IncreaseTime(renamingDelta) = SELECT renamingDelta:NATURAL &
      (curDeadlines/={} => renamingDelta <= min(ran(curDeadlines))) THEN
     curDeadlines := %x.(x:dom(curDeadlines)|curDeadlines(x)-renamingDelta)
  END;
  
  IncreaseTimeUntilDeadline(timer,renamingDelta) =
   SELECT renamingDelta:NATURAL &
          timer : dom(curDeadlines) &
          renamingDelta = min(ran(curDeadlines)) &
          renamingDelta = curDeadlines(timer) THEN
     curDeadlines := %x.(x:dom(curDeadlines) \ {timer}|curDeadlines(x)-renamingDelta)
  END;
  
  IncreaseTimeUntilCyclicDeadline(timer,renamingDelta,newDelta) =
   SELECT renamingDelta:NATURAL &
          timer : dom(curDeadlines) & 
          renamingDelta = curDeadlines(timer)  &
          renamingDelta = min(ran(curDeadlines)) &
          newDelta : NATURAL
  THEN
     curDeadlines := %x.(x:dom(curDeadlines) \ {timer}|curDeadlines(x)-renamingDelta) \/ {timer |-> newDelta}
  END

END
  
