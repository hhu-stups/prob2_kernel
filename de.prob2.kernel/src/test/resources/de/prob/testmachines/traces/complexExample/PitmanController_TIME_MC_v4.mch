MACHINE PitmanController_TIME_MC_v4
// v2: uses GenericTimer machine
SETS
  PTIMERS = {blink_deadline, tip_deadline}
INCLUDES PitmanController_v6, 
         GenericTimersMC(PTIMERS)
         // or GenericTimersMC(PTIMERS) for model checking
PROMOTES
  ENV_Turn_EngineOn, ENV_Turn_EngineOff,
  ENV_Pitman_DirectionBlinking,
  ENV_Pitman_Reset_to_Neutral,
  ENV_Hazard_blinking
DEFINITIONS
 
  ASSERT_LTL1 == "G not(deadlock(ENV_Turn_EngineOn,ENV_Turn_EngineOff))";
  ASSERT_LTL2 == "G not(deadlock(RTIME_BlinkerOn, RTIME_BlinkerOff, RTIME_Nothing,RTIME_Tip_blinking_Timeout))";
  ASSERT_LTL3 == "G controller(ENV_Turn_EngineOn, ENV_Turn_EngineOff)";
  ASSERT_LTL4 == "G deterministic(RTIME_BlinkerOn, RTIME_BlinkerOff, RTIME_Nothing)";
  ASSERT_LTL5 == "G e(ENV_Hazard_blinking)";
  ASSERT_LTL6 == "G e(ENV_Pitman_Tip_blinking_start)";
  ASSERT_LTL7 == "G e(ENV_Pitman_DirectionBlinking)"
INITIALISATION
  AbsoluteSetDeadline(blink_deadline,500)
OPERATIONS
  ENV_Pitman_Tip_blinking_start(newPos) = 
  SELECT newPos : PITMAN_TIP_BLINKING &
     newPos /= pitmanArmUpDown THEN
     ENV_Pitman_Tip_blinking_short(newPos) ||
     AddDeadline(tip_deadline,500)
  END;
  

  RTIME_Tip_blinking_Timeout(delta) =
   SELECT
     /*@label "grdTip" */ delta ∈ 0‥500
   THEN
     IF pitmanArmUpDown : PITMAN_TIP_BLINKING & remaining_blinks > 1 &
        active_blinkers = {pitman_direction(pitmanArmUpDown)} THEN
       TIME_Tip_blinking_Timeout 
     END ||
     IncreaseTimeUntilDeadline(tip_deadline,delta)
  END;
  
  RTIME_BlinkerOn(delta) =
   SELECT
     /*@label "grdTip" */ delta ∈ 0‥500
   THEN
     TIME_BlinkerOn ||
     IncreaseTimeUntilCyclicDeadline(blink_deadline,delta,500)
  END;
  
  RTIME_BlinkerOff(delta) =
   SELECT
     /*@label "grdTip" */ delta ∈ 0‥500
   THEN
     TIME_BlinkerOff ||
     IncreaseTimeUntilCyclicDeadline(blink_deadline,delta,500)
  END;
  
  RTIME_Nothing(delta,newOnCycle) =
   SELECT
     /*@label "grdDelta" */ delta ∈ 0‥500 &
     newOnCycle : BOOL
   THEN
     TIME_Nothing(newOnCycle) ||
     IncreaseTimeUntilCyclicDeadline(blink_deadline,delta,100)
  END;
  
  RTIME_Passes(delta) = SELECT delta : {100}
   THEN
    IncreaseTime(delta)
  END
END