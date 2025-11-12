FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "\
    file://kthread_test_priority.sh \
    file://ptest-format.sh \
    file://irq_test_affinity.sh \
    file://kernel_test_preempt_rt_presence.sh \
    file://kernel_test_rt_throttling_disabled.sh \
    file://rcu_nocbs_test.sh \
"

RDEPENDS:${PN}-ptest += "bash"

do_install_ptest:append() {
    cp ${UNPACKDIR}/kthread_test_priority.sh ${D}${PTEST_PATH}
    cp ${UNPACKDIR}/ptest-format.sh ${D}${PTEST_PATH}
    cp ${UNPACKDIR}/irq_test_affinity.sh ${D}${PTEST_PATH}
    cp ${UNPACKDIR}/kernel_test_preempt_rt_presence.sh ${D}${PTEST_PATH}
    cp ${UNPACKDIR}/kernel_test_rt_throttling_disabled.sh ${D}${PTEST_PATH}
    cp ${UNPACKDIR}/rcu_nocbs_test.sh ${D}${PTEST_PATH}
}
