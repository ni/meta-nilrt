SRC_URI = "${NILRT_GIT}/python-salttesting.git;protocol=git;branch=nilrt/cardassia/develop \
           file://0001-Add-ptest-output-option-to-test-suite.patch \
           "

SRCREV = "${AUTOREV}"
PV = "2016.7.22+git${SRCPV}"
S = "${WORKDIR}/git"
