SUMMARY = "NI-LinuxRT-specific glibc tests"
HOMEPAGE = "https://github.com/ni/meta-nilrt"
BUGTRACKER = "https://github.com/ni/meta-nilrt/issues"
SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "glibc"

SRC_URI = "\
	file://run-ptest \
	file://test_floating_point.cpp \
	file://test_oom_handling.cpp \
	file://test_overcomit_memory.sh \
	file://test_overcomit_ratio.sh \
	file://test_shmem.cpp \
	file://test_stack_touch.cpp \
"

S = "${UNPACKDIR}"

inherit ptest

CC += " ${LDFLAGS}"
debugsrcdir = "/usr/src/debug/${BPN}"

do_compile() {
	cd ${S}
	${CC} ${CFLAGS} ${LDFLAGS} -o test_floating_point ${UNPACKDIR}/test_floating_point.cpp

	${CC} ${CFLAGS} ${LDFLAGS} -o test_oom_handling ${UNPACKDIR}/test_oom_handling.cpp -lpthread
	${CC} ${CFLAGS} ${LDFLAGS} -o test_shmem        ${UNPACKDIR}/test_shmem.cpp        -lpthread
	${CC} ${CFLAGS} ${LDFLAGS} -o test_stack_touch  ${UNPACKDIR}/test_stack_touch.cpp  -lpthread
}

do_install() {
	# source files
	install -d ${D}${debugsrcdir}
	install -m 0644 ${UNPACKDIR}/*.cpp ${D}${debugsrcdir}/
}

do_install_ptest() {
	install -m 0755 ${UNPACKDIR}/run-ptest                ${D}${PTEST_PATH}

	install -m 0755 test_floating_point      ${D}${PTEST_PATH}
	install -m 0755 test_oom_handling        ${D}${PTEST_PATH}
	install -m 0755 ${UNPACKDIR}/test_overcomit_memory.sh ${D}${PTEST_PATH}
	install -m 0755 ${UNPACKDIR}/test_overcomit_ratio.sh  ${D}${PTEST_PATH}
	install -m 0755 test_shmem               ${D}${PTEST_PATH}
	install -m 0755 test_stack_touch         ${D}${PTEST_PATH}
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
RDEPENDS:${PN}-ptest += " \
	bash \
	ptest-utils-bash \
"
