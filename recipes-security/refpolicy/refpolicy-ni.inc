SRC_URI = "git://git.amer.corp.natinst.com/ni-refpolicy.git;branch=dev/nilrt/15.0/dizzy \
           file://customizable_types \
           file://setrans-mls.conf \
           file://setrans-mcs.conf \
          "

SRCREV = "44eef3679b4b05f70ca0c3783893b42cb5cc97ce"

S = "${WORKDIR}/git"
