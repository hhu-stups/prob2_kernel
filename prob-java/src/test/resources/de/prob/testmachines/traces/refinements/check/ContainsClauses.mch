MACHINE ContainsClauses

SETS
  DIRECTIONS = {left_blink,right_blink,neutral_blink}
  
CONSTANTS
    g
PROPERTIES
    g = NAT

VARIABLES
    x
INVARIANT
    x : NAT
INITIALISATION
    x := 14

OPERATIONS
op = x := 1


END