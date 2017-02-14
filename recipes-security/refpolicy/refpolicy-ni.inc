SRC_URI = "${NILRT_GIT}/ni-refpolicy.git;branch=nilrt/17.0/krogoth \
           file://customizable_types \
           file://setrans-mls.conf \
           file://setrans-mcs.conf \
"

SRCREV = "722173ba7c1224b1f718ac89b5dcbc3e0795b48a"

S = "${WORKDIR}/git"
