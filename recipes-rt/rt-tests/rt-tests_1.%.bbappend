FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "\
    file://kthread_test_priority.sh \
    file://ptest-format.sh \
    file://irq_test_priority.sh \
    file://irq_test_affinity.sh \
    file://kernel_test_preempt_rt_presence.sh \
    file://kernel_test_rt_throttling_disabled.sh \
    file://test_throttling_jitter.c \
"

RDEPENDS_${PN}-ptest += "bash"

LDFLAGS += "-lm"

do_compile_ptest_append() {
    cd ${WORKDIR}
    ${CC} -o test_throttling_jitter test_throttling_jitter.c ${LDFLAGS}
}

do_install_ptest_append() {
    cp ${WORKDIR}/kthread_test_priority.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/ptest-format.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/irq_test_priority.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/irq_test_affinity.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/kernel_test_preempt_rt_presence.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/kernel_test_rt_throttling_disabled.sh ${D}${PTEST_PATH}
    cp ${WORKDIR}/test_throttling_jitter ${D}${PTEST_PATH}
}
