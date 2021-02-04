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
  IncreaseTime(delta) = SELECT delta:NATURAL & 
      (curDeadlines/={} => curTime+delta <= min(ran(curDeadlines))) THEN
     curTime := curTime + delta
  END;
  
  IncreaseTimeUntilDeadline(timer,delta) =
   SELECT delta:NATURAL & 
          timer : dom(curDeadlines) &
          curTime + delta = min(ran(curDeadlines)) & 
          curTime + delta = curDeadlines(timer) THEN
     curTime := curTime + delta ||
     curDeadlines :=  {timer} <<| curDeadlines
  END;
  
  IncreaseTimeUntilCyclicDeadline(timer,delta,newDelta) =
   SELECT delta:NATURAL & 
          timer : dom(curDeadlines) & 
          curTime+delta = curDeadlines(timer)  &
          curTime+delta = min(ran(curDeadlines)) &
          newDelta : NATURAL
  THEN
     curTime := curTime + delta ||
     curDeadlines(timer) :=  curTime+delta+newDelta
  END

END
  
  