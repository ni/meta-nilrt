SUMMARY = "Linux kernel-specific tests"
HOMEPAGE = "https://kernel.org"
SECTION = "tests"
LICENSE = "GPLv2 & GPLv2+"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

inherit ptest

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-files:"

S = "${WORKDIR}"

DEPENDS = "virtual/kernel libcap"
RDEPENDS:${PN}-ptest += "bash libcap kmod dmidecode"

ALLOW_EMPTY:${PN} = "1"

SRC_URI += "\
    file://run-ptest \
    file://required_kernel_modules.arm \
    file://required_kernel_modules.x64 \
    file://ptest-format.sh \
    file://test_kernel_mcopy.sh \
    file://test_kernel_mcopy_functionality.c \
    file://test_kernel_mcopy_freed_memory.c \
    file://test_kernel_cache_info.sh \
    file://test_kernel_cap_support.sh \
    file://test_kernel_modules.sh \
    file://test_kernel_security.sh \
    file://test_exe_cap_support.c \
    file://test_proc_cap_support.c \
    file://cap_support_exe_to_test.c \
    file://test_kernel_ll_route.sh \
    file://test_kernel_hrtimers.sh \
    file://test_kernel_hrtimers.c \
    file://test_user_stack_size.sh \
    file://test_pthread_stack_size.c \
    file://test_kernel_swap_disabled.sh \
    file://test_i915_firmware.sh \
"

LDFLAGS += "-lcap -lpthread"

do_compile_ptest:append() {
    cd ${WORKDIR}
    ${CC} ${CFLAGS} -o test_kernel_mcopy_functionality test_kernel_mcopy_functionality.c ${LDFLAGS}
    ${CC} ${CFLAGS} -o test_kernel_mcopy_freed_memory test_kernel_mcopy_freed_memory.c ${LDFLAGS}
    ${CC} ${CFLAGS} -o test_exe_cap_support test_exe_cap_support.c ${LDFLAGS}
    ${CC} ${CFLAGS} -o cap_support_exe_to_test cap_support_exe_to_test.c ${LDFLAGS}
    ${CC} ${CFLAGS} -o test_proc_cap_support test_proc_cap_support.c ${LDFLAGS}
    ${CC} ${CFLAGS} -o test_kernel_hrtimers test_kernel_hrtimers.c ${LDFLAGS}
    ${CC} ${CFLAGS} -o test_pthread_stack_size test_pthread_stack_size.c ${LDFLAGS}
}

do_install_ptest:append() {
    cp ${WORKDIR}/run-ptest ${D}${PTEST_PATH}
    cp ${WORKDIR}/ptest-format.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_mcopy.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_mcopy_functionality ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_mcopy_freed_memory ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_cache_info.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_cap_support.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_modules.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_security.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_exe_cap_support ${D}${PTEST_PATH}
    cp ${WORKDIR}/cap_support_exe_to_test ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_proc_cap_support ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_ll_route.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_hrtimers.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_hrtimers ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_user_stack_size.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_pthread_stack_size ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_kernel_swap_disabled.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_i915_firmware.sh ${D}${PTEST_PATH}
}

do_install_ptest:append:x64() {
    cp ${WORKDIR}/required_kernel_modules.x64 ${D}${PTEST_PATH}/required_kernel_modules
}

do_install_ptest:append:xilinx-zynqhf() {
    cp ${WORKDIR}/required_kernel_modules.arm ${D}${PTEST_PATH}/required_kernel_modules
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
