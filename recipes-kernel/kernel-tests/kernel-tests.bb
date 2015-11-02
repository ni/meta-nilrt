SUMMARY = "Linux kernel-specific tests"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPLv2 & GPLv2+"
LIC_FILES_CHKSUM = "file://run-ptest;md5=35c33505956a0d78042c787033691c59"

inherit ptest

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

DEPENDS = "virtual/kernel libcap"
RDEPENDS_${PN}-ptest += "bash libcap"

ALLOW_EMPTY_${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://test_kernel_mcopy.sh \
    file://test_kernel_mcopy_functionality.c \
    file://test_kernel_mcopy_freed_memory.c \
    file://test_kernel_cache_info.sh \
    file://test_kernel_cap_support.sh \
    file://test_exe_cap_support.c \
    file://test_proc_cap_support.c \
    file://cap_support_exe_to_test.c \
    file://test_kernel_ll_route.sh \
    file://test_kernel_hrtimers.sh \
    file://test_kernel_hrtimers.c \
    file://test_user_stack_size.sh \
    file://test_pthread_stack_size.c \
    file://test_kernel_swap_disabled.sh \
"

LDFLAGS += "-lcap -lpthread"

do_compile_ptest_append() {
    cd ${WORKDIR}
    ${CC} -o test_kernel_mcopy_functionality test_kernel_mcopy_functionality.c
    ${CC} -o test_kernel_mcopy_freed_memory test_kernel_mcopy_freed_memory.c
    ${CC} -o test_exe_cap_support test_exe_cap_support.c ${LDFLAGS}
    ${CC} -o cap_support_exe_to_test cap_support_exe_to_test.c ${LDFLAGS}
    ${CC} -o test_proc_cap_support test_proc_cap_support.c ${LDFLAGS}
    ${CC} -o test_kernel_hrtimers test_kernel_hrtimers.c
    ${CC} -o test_pthread_stack_size test_pthread_stack_size.c ${LDFLAGS}
}

do_install_ptest_append() {
    cp ${WORKDIR}/run-ptest ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_mcopy.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_mcopy_functionality ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_mcopy_freed_memory ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_cache_info.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_cap_support.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_exe_cap_support ${D}${PTEST_PATH}
    cp ${WORKDIR}/cap_support_exe_to_test ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_proc_cap_support ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_ll_route.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_hrtimers.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_hrtimers ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_user_stack_size.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_pthread_stack_size ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_swap_disabled.sh ${D}${PTEST_PATH}
}
