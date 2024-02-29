MACHINE GenericTimers(TIMERS)
VARIABLES
  curTime,
  curDeadlines

INVARIANT
  /*@label "invTime" */ curTime ∈ ℕ
  &
  curDeadlines : TIMERS +-> NATURAL


INITIALISATION
  curTime := 0 ||
  curDeadlines := {}

OPERATIONS
  
  AbsoluteSetDeadline(timer,deadline) = PRE timer:TIMERS & deadline:NATURAL THEN
     curDeadlines(timer) := deadline // for INITIALISATION; avoid dependency on curTime
  END;
  
  AddDeadline(timer,deadline) = PRE timer:TIMERS & deadline:NATURAL THEN
     curDeadlines(timer) := curTime+deadline
  END;
  
  // Increase time without exceeding a deadline
  IncreaseTime(renamingDelta) = SELECT renamingDelta:NATURAL &
      (curDeadlines/={} => curTime+renamingDelta <= min(ran(curDeadlines))) THEN
     curTime := curTime + renamingDelta
  END;
  
  IncreaseTimeUntilDeadline(timer,renamingDelta) =
   SELECT renamingDelta:NATURAL &
          timer : dom(curDeadlines) &
          curTime + renamingDelta = min(ran(curDeadlines)) &
          curTime + renamingDelta = curDeadlines(timer) THEN
     curTime := curTime + renamingDelta ||
     curDeadlines :=  {timer} <<| curDeadlines
  END;
  
  IncreaseTimeUntilCyclicDeadline(timer,renamingDelta,newDelta) =
   SELECT renamingDelta:NATURAL &
          timer : dom(curDeadlines) & 
          curTime+renamingDelta = curDeadlines(timer)  &
          curTime+renamingDelta = min(ran(curDeadlines)) &
          newDelta : NATURAL
  THEN
     curTime := curTime + renamingDelta ||
     curDeadlines(timer) :=  curTime+renamingDelta+newDelta
  END

END
  
