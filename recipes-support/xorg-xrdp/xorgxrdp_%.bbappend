FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-xorg.conf-Make-sure-the-glamor-module-is-loaded.patch"

DEPENDS += "libdrm"

FILES:${PN}:remove = "${libdir}/xorg/modules/*"
FILES:${PN} += "${libdir}/xorg/modules/*.so"
FILES:${PN} += "${libdir}/xorg/modules/*/*.so"
FILES:${PN}-staticdev += "${libdir}/xorg/modules/*.a"
FILES:${PN}-staticdev += "${libdir}/xorg/modules/*/*.a"

CFLAGS:prepend = "-I${STAGING_INCDIR}/libdrm "
EXTRA_OECONF =+ "--enable-glamor"

