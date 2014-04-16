#These are two escape sequences that set blank timeout and
#video monitor powerdown interval to 0.
#Extract from man console_codes:
#ESC [ 9 ; n ]       Set screen blank timeout to n minutes.
#ESC [ 14 ; n ]      Set the VESA powerdown interval in minutes.
printf '\033[9;0]\033[14;0]'
