MACHINE PitmanController_TIME
INCLUDES Blinking_v4
PROMOTES
  ENV_Turn_EngineOn, ENV_Turn_EngineOff,
  ENV_Pitman_DirectionBlinking,
  ENV_Pitman_Reset_to_Neutral,
  ENV_Hazard_blinking
DEFINITIONS
 
  ASSERT_LTL1 == "G not(deadlock(ENV_Turn_EngineOn,ENV_Turn_EngineOff))";
  ASSERT_LTL2 == "G not(deadlock(RTIME_BlinkerOn, RTIME_BlinkerOff, RTIME_Nothing))";
  ASSERT_LTL3 == "G controller(ENV_Turn_EngineOn, ENV_Turn_EngineOff)";
  ASSERT_LTL4 == "G controller(RTIME_BlinkerOn, RTIME_BlinkerOff, RTIME_Nothing)";
  ASSERT_LTL5 == "G e(ENV_Hazard_blinking)";
  ASSERT_LTL6 == "G e(ENV_Pitman_Tip_blinking_start)";
  ASSERT_LTL7 == "G e(ENV_Pitman_DirectionBlinking)"
VARIABLES
  curTime,
  tipStartTime
INVARIANT
  /*@label "invTime" */ curTime ∈ ℕ
  &
  /*@label "invTip1" */ tipStartTime ∈ ℕ
  &
  /*@label "invTip2" */ tipStartTime ≤ curTime
INITIALISATION
  curTime := 0 ||
  tipStartTime := 0
OPERATIONS
  ENV_Pitman_Tip_blinking_start(newPos) = 
  PRE newPos : PITMAN_TIP_BLINKING &
     newPos /= pitmanArmUpDown THEN
     ENV_Pitman_Tip_blinking_short(newPos,pitman_direction(newPos)) ||
     tipStartTime ≔ curTime
  END;
  

  RTIME_Tip_blinking_Timeout =
   SELECT
     /*@label "grdTip" */ curTime ≥ tipStartTime+500
   THEN
     TIME_Tip_blinking_Timeout
  END;
  
  RTIME_BlinkerOn(renamingDelta) =
   SELECT
     /*@label "grdTip" */ renamingDelta ∈ 1‥500 &
     /*@label "grdTm" */  (curTime+renamingDelta) mod 500 = 0
   THEN
     TIME_BlinkerOn ||
     curTime ≔ curTime+renamingDelta
  END;
  
  RTIME_BlinkerOff(renamingDelta) =
   SELECT
     /*@label "grdTip" */ renamingDelta ∈ 1‥500 &
     /*@label "grdTm" */  (curTime+renamingDelta) mod 500 = 0
   THEN
     TIME_BlinkerOff ||
     curTime ≔ curTime+renamingDelta
  END;
  
  RTIME_Nothing(renamingDelta,newOnCycle) =
   SELECT
     /*@label "grdTip" */ renamingDelta ∈ 1‥500 &
     /*@label "grdTm" */  (curTime+renamingDelta) mod 500 = 0 &
    newOnCycle /= onCycle
   THEN
     TIME_Nothing(newOnCycle) ||
     curTime ≔ curTime+renamingDelta
  END;
  
  RTIME_Passes(renamingDelta) =
   SELECT
     /*@label "grdTip" */ renamingDelta ∈ {100} &
     /*@label "grdTm" */  ∀d·(d∈1‥renamingDelta ⇒ (curTime+d) mod 500 ≠ 0)
   THEN
     curTime ≔ curTime+renamingDelta
  END
END
