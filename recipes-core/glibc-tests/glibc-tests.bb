SUMMARY = "NI-LinuxRT-specifc glibc tests"
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

S = "${WORKDIR}"


inherit ptest

do_compile_ptest() {
	cd ${S}
	${CC} -o test_floating_point test_floating_point.cpp ${LDFLAGS}
	${CC} -o test_oom_handling test_oom_handling.cpp ${LDFLAGS} -lpthread
	${CC} -o test_shmem test_shmem.cpp ${LDFLAGS} -lpthread
	${CC} -o test_stack_touch test_stack_touch.cpp ${LDFLAGS} -lpthread
}

do_install_ptest() {
	install -m 0755 ${S}/test_floating_point      ${D}${PTEST_PATH}
	install -m 0755 ${S}/test_oom_handling        ${D}${PTEST_PATH}
	install -m 0755 ${S}/test_overcomit_memory.sh ${D}${PTEST_PATH}
	install -m 0755 ${S}/test_overcomit_ratio.sh  ${D}${PTEST_PATH}
	install -m 0755 ${S}/test_shmem               ${D}${PTEST_PATH}
	install -m 0755 ${S}/test_stack_touch         ${D}${PTEST_PATH}
}

ALLOW_EMPTY_${PN} = "1"

RDEPENDS_${PN}-ptest = "bash"
