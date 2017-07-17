SRC_URI = "${NILRT_GIT}/ni-refpolicy.git;branch=nilrt/17.0/krogoth \
           file://customizable_types \
           file://setrans-mls.conf \
           file://setrans-mcs.conf \
"

PV = "2.20140311+git${SRCPV}"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

DEFAULT_PREFERENCE = "1"
