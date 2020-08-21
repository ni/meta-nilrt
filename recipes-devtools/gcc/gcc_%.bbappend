
# set gcc default settings to match TUNE_CCARGS
python () {
    ccargs = d.getVar("TUNE_CCARGS", True).split()
    for a in ccargs:
        arg = a.split('=')
        if len(arg) < 2:
            continue
        if arg[0] == '-march':
            d.appendVar("EXTRA_OECONF_append", " --with-arch=" + arg[1])
        elif arg[0] == '-mtune':
            d.appendVar("EXTRA_OECONF_append", " --with-tune=" + arg[1])
        elif arg[0] == '-mfpu':
            d.appendVar("EXTRA_OECONF_append", " --with-fpu=" + arg[1])
        elif arg[0] == '-mfloat-abi':
            d.appendVar("EXTRA_OECONF_append", " --with-float=" + arg[1])
}
