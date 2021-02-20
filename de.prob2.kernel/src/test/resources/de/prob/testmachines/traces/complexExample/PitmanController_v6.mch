MACHINE PitmanController_v6
INCLUDES BlinkLamps_v3, Sensors
/*
 The BlinkLamps machine takes care of flashing the lights and the
 remaining blinks (for tip blinking).
 The main machine only has to worry about setting active_blinkers and
 for setting the blinkers to continuous or tip-blinking
 v5 uses v3 BlinkLamps
*/
PROMOTES TIME_BlinkerOn, TIME_BlinkerOff, TIME_Nothing
CONSTANTS
  pitman_direction /*@desc "Convert Pitman position into blink direction" */
PROPERTIES
  pitman_direction = {Neutral |-> neutral_blink, Downward5 |-> left_blink, Downward7 |-> left_blink,
                      Upward5 |-> right_blink, Upward7 |-> right_blink}
ASSERTIONS
 /*@label "thm1" */ pitman_direction : PITMAN_POSITION --> DIRECTIONS
  
DEFINITIONS
  blinkersOff == (blinkLeft=lamp_off & blinkRight=lamp_off);
  continuousBlink == -1;
  continuousBlinking == (remaining_blinks = continuousBlink);

  ASSERT_LTL1 == "G not(deadlock(ENV_Turn_EngineOn,ENV_Turn_EngineOff))";
  ASSERT_LTL2 == "G not(deadlock(TIME_BlinkerOn, TIME_BlinkerOff, TIME_Nothing))";
  ASSERT_LTL3 == "G controller(ENV_Turn_EngineOn, ENV_Turn_EngineOff)";
  ASSERT_LTL4 == "G controller(TIME_BlinkerOn, TIME_BlinkerOff, TIME_Nothing)";
  ASSERT_LTL5 == "G e(ENV_Hazard_blinking)";
  ASSERT_LTL6 == "G e(ENV_Pitman_Tip_blinking_short)";
  ASSERT_LTL7 == "G e(ENV_Pitman_DirectionBlinking)"
INVARIANT
  /*@label "SAF-H1" */ (hazardWarningSwitchOn = switch_on => active_blinkers=BLINK_DIRECTION) 
  /*@desc "ELS-8: As long as the hazard warning light switch is pressed (active), all
           direction indicators flash synchronously. " */ &
  
   /*@label "SAF-H2" */ (hazardWarningSwitchOn = switch_off & continuousBlinking
    => active_blinkers = {pitman_direction(pitmanArmUpDown)} ) &

   /*@label "SAF-H3" */ (pitmanArmUpDown:PITMAN_DIRECTION_BLINKING & engineOn=TRUE 
    => {pitman_direction(pitmanArmUpDown)} <: active_blinkers) &
    
     /*@label "SAF-H4" */ (engineOn=FALSE & hazardWarningSwitchOn = switch_off => active_blinkers={})
    &
 // new invariants required for Rodin Proof:
 
  /*@label "SAF-H0" */ (hazardWarningSwitchOn = switch_on ⇒ remaining_blinks = continuousBlink) &
  
  /*@label "SAF-H3b"*/ (pitmanArmUpDown∈PITMAN_DIRECTION_BLINKING ∧ engineOn=TRUE
         ⇒ remaining_blinks = continuousBlink)

OPERATIONS
  
 ENV_Turn_EngineOn =
 BEGIN
   SET_EngineOn ||
   IF pitmanArmUpDown :PITMAN_DIRECTION_BLINKING &
      hazardWarningSwitchOn = switch_off THEN
	  SET_BlinkersOn(pitman_direction(pitmanArmUpDown),continuousBlink)
   END
 END;
 
 ENV_Turn_EngineOff = 
 BEGIN
   SET_EngineOff ||
   IF hazardWarningSwitchOn = switch_off
    /*@desc "ELS-8 As long as the hazard warning light switch is pressed (active), 
             all direction indicators flash synchronously." */
             // TO DO: pluse ratio 1:2 if ignition key is in lock
   THEN
		SET_AllBlinkersOff
   END
 END;
 
 ENV_Pitman_DirectionBlinking (newPos) = 
 // corresponds to pitmanArmUpDown = 2 or 4 (Upward/Downward7)
 // ELS-1, ELS-5
 PRE newPos /= pitmanArmUpDown THEN
    IF hazardWarningSwitchOn = switch_off & engineOn = TRUE THEN
		SET_BlinkersOn(pitman_direction(newPos),continuousBlink)
    END
    ||
    SET_Pitman_DirectionBlinking(newPos)
 END;
 
 ENV_Pitman_Reset_to_Neutral = 
 // ELS-1, ELS-5
 BEGIN
    SET_Pitman_Reset_to_Neutral
    ||
    IF hazardWarningSwitchOn = switch_off & continuousBlinking THEN
		SET_AllBlinkersOff
    END
 END;
 
 ENV_Pitman_Tip_blinking_short(newPos) = 
 // ELS-2, ELS-5
 PRE newPos : PITMAN_TIP_BLINKING &
     newPos /= pitmanArmUpDown THEN
       SET_Pitman_Tip_blinking_short(newPos)
        ||
	   IF hazardWarningSwitchOn = switch_off 
	      /*@desc "ELS-13: If the warning light is activated,
	               any tip-blinking will be ignored ..." */
	      & engineOn = TRUE 
		 THEN
			SET_BlinkersOn(pitman_direction(newPos),3)
		END
 END;
 
 TIME_Tip_blinking_Timeout = 
 // after 0.5 seconds a Tip blinking is cancelled and replaced by a continuous blinking
 // ELS-4: If the driver holds the pitman arm for more than 0.5 seconds in position "tip-blinking left", 
 // flashing cycles are initiated for all direction
 // indicators on the left (see Req. ELS-1) until the pitman arm leaves the position "tip-blinking left".
 SELECT pitmanArmUpDown : PITMAN_TIP_BLINKING & remaining_blinks > 1 &
        active_blinkers = {pitman_direction(pitmanArmUpDown)} THEN
		SET_RemainingBlinks(continuousBlink)
 END;
 
 
 
 ENV_Hazard_blinking(newSwitchPos) = SELECT newSwitchPos /= hazardWarningSwitchOn THEN
 // ELS-1, ELS-5
	 SELECT newSwitchPos = switch_on // hazardWarningSwitchOn = switch_off 
	 THEN
		SET_AllBlinkersOn
	  WHEN newSwitchPos = switch_off // hazardWarningSwitchOn = switch_on
	  THEN
		IF pitmanArmUpDown = Neutral or engineOn = FALSE THEN
		   SET_AllBlinkersOff
		ELSIF  pitmanArmUpDown /: PITMAN_DIRECTION_BLINKING THEN
		   // ELS-12 : When hazard warning is deactivated again, the pitman arm is in
           //  position \direction blinking left" or \direction blinking right" ignition is On, 
           //  the direction blinking cycle should be started (see Req. ELS-1).
		   SET_AllBlinkersOff
		ELSE
		   SET_BlinkersOn(pitman_direction(pitmanArmUpDown),remaining_blinks) // remaining_blinks must be ≠ 0
		END
	 END ||
		SET_Hazard_blinking(newSwitchPos)
 END
 

 
END

