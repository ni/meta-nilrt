SRCREV = "b82e3b764e60df337ca695e8f8642e7bf42b0cca"
SRC_URI = "git://github.com/xkbcommon/libxkbcommon.git;protocol=https"

unset SRC_URI[md5sum]
unset SRC_URI[sha256sum]
unset UPSTREAM_CHECK_URI

S = "${WORKDIR}/git"
