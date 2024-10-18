SUMMARY = "Yocto ptest helper utilities"
DESCRIPTION = "Libraries, scripts, and source files which help developers to write and emit valid yocto ptests."
AUTHOR = "NI RTOS <rtos@emerson.com>"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
BUGTRACKER = "https://github.com/ni/meta-nilrt/issues"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

PV = "1.4"

SRC_URI = "\
	file://ptest-format.sh \
"

S = "${WORKDIR}"


do_install () {
	install -d ${D}${libdir}/${PN}/bash
	install --mode=0644 ${S}/ptest-format.sh ${D}${libdir}/${PN}/bash/ptest-format.sh
}


## subpackages
PACKAGE_BEFORE_PN = "${PN}-bash"

# -bash : helper files for bash ptests
SUMMARY:${PN}-bash = "${SUMMARY} - bash ptests"
FILES:${PN}-bash = "${libdir}/${PN}/bash/*"
RDEPENDS:${PN}-bash += " bash"
