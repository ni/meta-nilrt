SRC_URI = "git://github.com/ni/ni-refpolicy.git;branch=nilrt/16.0/dizzy \
           file://customizable_types \
           file://setrans-mls.conf \
           file://setrans-mcs.conf \
"

SRCREV = "722173ba7c1224b1f718ac89b5dcbc3e0795b48a"

S = "${WORKDIR}/git"
