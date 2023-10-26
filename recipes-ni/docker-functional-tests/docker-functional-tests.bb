SUMMARY = "Functional test of docker container capabilities on NILRT"
HOMEPAGE = "https://github.com/ni/nilrt"
SECTION = "tests"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = ""

SRC_URI = "\
    file://run-ptest \
    file://test_daemon.sh \
    file://test_parallel.sh \
    file://test_parallel.Dockerfile \
    file://test_parallel_test_file.txt \
"
S = "${WORKDIR}"

inherit ptest

ALLOW_EMPTY:${PN} = "1"

do_install_ptest:append() {
    install -m 0755 ${S}/run-ptest ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_daemon.sh ${D}${PTEST_PATH}
    install -m 0755 ${S}/test_parallel.sh ${D}${PTEST_PATH}
    mkdir -p ${D}${PTEST_PATH}/test_parallel_container
    install -m 0755 ${S}/test_parallel.Dockerfile \
        ${D}${PTEST_PATH}/test_parallel_container/Dockerfile
    install -m 0664 ${S}/test_parallel_test_file.txt \
        ${D}${PTEST_PATH}/test_parallel_container/test_file.txt
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
RDEPENDS:${PN}-ptest += "bash docker-ce"

