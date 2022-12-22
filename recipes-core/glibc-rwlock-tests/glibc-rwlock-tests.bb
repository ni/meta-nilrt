SUMMARY = "NI-LinuxRT-specific glibc rwlock tests"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
BUGTRACKER = "https://github.com/ni/meta-nilrt/issues"
SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "glibc"

SRC_URI = "\
	file://run-ptest \
	file://rwlockbomb.c \
	file://test_rwlockbomb.sh \
"

S = "${WORKDIR}"


inherit ptest

CC += " ${LDFLAGS}"
debugsrcdir = "/usr/src/debug/${BPN}"

do_compile() {
	cd ${S}

	${CC} -o rwlockbomb rwlockbomb.c -lpthread
}

do_install() {
	# source files
	install -d ${D}${debugsrcdir}
	install -m 0644 ${S}/*.c ${D}${debugsrcdir}/
}

do_install_ptest() {
	install -m 0755 ${S}/run-ptest                ${D}${PTEST_PATH}

	install -m 0744 ${S}/rwlockbomb               ${D}${PTEST_PATH}
	install -m 0744 ${S}/test_rwlockbomb.sh       ${D}${PTEST_PATH}
}


ALLOW_EMPTY:${PN} = "1"

## subpackages
# -src : Source files
INSANE_SKIP:${PN}-src = "dev-deps"
RDEPENDS:${PN}-src = "\
	binutils \
	gcc-symlinks \
	kernel-dev \
"

# -ptest : ptest wrappers
# coreutils: for `nproc` in test_rwlockbomb.sh
RDEPENDS:${PN}-ptest += " \
	bash \
	coreutils \
	ptest-utils-bash \
"
