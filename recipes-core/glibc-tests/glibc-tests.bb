FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SUMMARY = "glibc related test suite, meant for functional testing of the NI Linux RT distribution"
SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit ptest

SRC_URI = "file://test_shmem.cpp \
           file://test_stack_touch.cpp \
           file://run-ptest \
           file://test_oom_handling.cpp \
           file://test_floating_point.cpp \
           file://test_overcomit_memory.sh \
           file://test_overcomit_ratio.sh \
          "
DEPENDS = "glibc"
RDEPENDS_${PN}-ptest = "bash"

ALLOW_EMPTY_${PN} = "1"

do_compile_ptest() {
    cd ${WORKDIR}
    ${CC} -o test_shmem test_shmem.cpp ${LDFLAGS} -lpthread
    ${CC} -o test_stack_touch test_stack_touch.cpp ${LDFLAGS} -lpthread
    ${CC} -o test_oom_handling test_oom_handling.cpp ${LDFLAGS} -lpthread
    ${CC} -o test_floating_point test_floating_point.cpp ${LDFLAGS}
}

do_install_ptest() {
    install -m 0755 ${WORKDIR}/test_shmem ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test_stack_touch ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test_oom_handling ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test_floating_point ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test_overcomit_memory.sh ${D}${PTEST_PATH}
    install -m 0755 ${WORKDIR}/test_overcomit_ratio.sh ${D}${PTEST_PATH}
}
