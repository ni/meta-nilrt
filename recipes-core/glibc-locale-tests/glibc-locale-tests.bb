SUMMARY = "NI-LinuxRT-specific glibc locale tests"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
BUGTRACKER = "https://github.com/ni/meta-nilrt/issues"
SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "glibc"


SRC_URI = "\
	file://run-ptest \
	file://test_locale_aliases.sh \
"

S = "${WORKDIR}"


inherit ptest


do_install_ptest() {
	install -m 0755 ${S}/run-ptest                ${D}${PTEST_PATH}

	install -m 0755 ${S}/test_locale_aliases.sh   ${D}${PTEST_PATH}
}


ALLOW_EMPTY:${PN} = "1"

# -ptest : ptest wrappers
RDEPENDS:${PN}-ptest += " \
	bash \
	locale-base-en-us.iso-8859-1 \
	locale-base-ja-jp.windows-31j \
	locale-base-zh-cn.cp936 \
	localedef \
	ni-locale-alias \
	ptest-utils-bash \
"