SRC_URI = "git://github.com/ni/ni-refpolicy.git;branch=nilrt/15.0/dizzy \
           file://customizable_types \
           file://setrans-mls.conf \
           file://setrans-mcs.conf \
"

SRCREV = "dd5ac755a8681b0db0a6ffd4e64f6265e35b4bf0"

S = "${WORKDIR}/git"
