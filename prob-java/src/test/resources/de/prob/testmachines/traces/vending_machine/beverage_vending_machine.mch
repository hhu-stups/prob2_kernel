MACHINE beverage_vending_machine

VARIABLES coins, soda

INVARIANT
  coins : NATURAL & soda : NATURAL

INITIALISATION coins := 0 || soda := 3

OPERATIONS
  insert_coin = BEGIN coins := coins + 1|| 
                      soda := soda END;
  get_soda = PRE coins > 0 & soda > 0 
             THEN 
                 coins := coins - 1|| 
                 soda := soda - 1 
             END
END     