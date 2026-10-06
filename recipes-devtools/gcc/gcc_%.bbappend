
# set gcc default settings to match TUNE_CCARGS
python () {
    ccargs = d.getVar("TUNE_CCARGS", True).split()
    for a in ccargs:
        arg = a.split('=')
        if len(arg) < 2:
            continue
        if arg[0] == '-march':
            d.appendVar("EXTRA_OECONF:append", " --with-arch=" + arg[1])
        elif arg[0] == '-mtune':
            d.appendVar("EXTRA_OECONF:append", " --with-tune=" + arg[1])
        elif arg[0] == '-mfpu':
            d.appendVar("EXTRA_OECONF:append", " --with-fpu=" + arg[1])
        elif arg[0] == '-mfloat-abi':
            d.appendVar("EXTRA_OECONF:append", " --with-float=" + arg[1])
}

PACKAGES =+ "${PN}-lto-dump ${PN}-lto"

SUMMARY:${PN}-lto = "GNU C compiler link-time optimization tools"
RDEPENDS:${PN}-lto = "${PN} (= ${EXTENDPKGV})"

FILES:${PN}-lto = "\
    ${libexecdir}/gcc/${TARGET_SYS}/${BINV}/lto* \
"

SUMMARY:${PN}-lto-dump = "GNU C compiler link-time optimization dump tool"
RDEPENDS:${PN}-lto-dump = "${PN} (= ${EXTENDPKGV})"

FILES:${PN}-lto-dump = "${bindir}/${TARGET_PREFIX}lto-dump"
