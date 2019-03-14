SRC_URI = "${NILRT_GIT}/ni-refpolicy.git;branch=nilrt/17.0/krogoth \
           file://customizable_types \
           file://setrans-mls.conf \
           file://setrans-mcs.conf \
"

PV = "2.20140311+git${SRCPV}"
SRCREV = "43d39eb64adb1d634f6f2712c460b21a8e565385"

S = "${WORKDIR}/git"

DEFAULT_PREFERENCE = "1"
