MACHINE Regex
CONSTANTS c
PROPERTIES c = REGEX_REPLACE("12345","2", "1") 
DEFINITIONS
EXTERNAL_FUNCTION_REGEX_REPLACE == (STRING*STRING*STRING --> STRING);
REGEX_REPLACE(re_str,re_pat,re_subst) == re_str;
END
