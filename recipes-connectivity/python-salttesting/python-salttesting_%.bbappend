SRC_URI = "git://github.com/saltstack/salt-testing;protocol=git;branch=develop \
           file://0001-Add-ptest-output-option-to-test-suite.patch \
           "

SRCREV = "095e9020b033c63b21fceba5bae9fa013dc52789"
PV = "2016.7.22+git${SRCPV}"
S = "${WORKDIR}/git"
