MACHINE beverage_vending_machine

VARIABLES coins, sodas

INVARIANT
  coins : NATURAL &  sodas : NATURAL

INITIALISATION coins := 0 || sodas := 3

OPERATIONS
  insert_coin = BEGIN coins := coins + 1 || 
                sodas := sodas END;
  get_soda = PRE coins > 0 & sodas > 0 
             THEN 
                 coins := coins - 1 || 
                 sodas := sodas - 1 
             END
END     