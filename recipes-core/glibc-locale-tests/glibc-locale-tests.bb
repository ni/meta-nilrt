SUMMARY = "NI-LinuxRT-specific glibc locale tests"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
BUGTRACKER = "https://github.com/ni/meta-nilrt/issues"
SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "glibc"


SRC_URI = "\
	file://run-ptest \
	file://test_locale_aliases.c \
	file://test_locale_aliases.sh \
	file://CP932.txt \
	file://CP936.txt \
	file://L1.txt \
"

S = "${WORKDIR}"


inherit ptest

CC += " ${LDFLAGS}"
debugsrcdir = "/usr/src/debug/${BPN}"

do_compile() {
	cd ${S}
	${CC} -o test_locale_aliases test_locale_aliases.c
}

do_install() {
	# source files
	install -d ${D}${debugsrcdir}
	install -m 0644 ${S}/*.c ${D}${debugsrcdir}/
}

do_install_ptest() {
	install -m 0755 ${S}/run-ptest                ${D}${PTEST_PATH}

	install -m 0755 ${S}/test_locale_aliases.sh   ${D}${PTEST_PATH}
	install -m 0755 ${S}/test_locale_aliases      ${D}${PTEST_PATH}
	install -m 0644 ${S}/CP932.txt                ${D}${PTEST_PATH}
	install -m 0644 ${S}/CP936.txt                ${D}${PTEST_PATH}
	install -m 0644 ${S}/L1.txt                   ${D}${PTEST_PATH}
}


ALLOW_EMPTY:${PN} = "1"


## subpackages
# -src : Source files
INSANE_SKIP:${PN}-src = "dev-deps"
RDEPENDS:${PN}-src = "\
	binutils \
	gcc-symlinks \
"

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