EXTRA_OECONF = "--libdir=${base_libdir} --disable-lynx --enable-legacy"

DEPENDS = ""
RDEPENDS_${PN} = ""

PRINC := "${@int(PRINC) + 1}"
