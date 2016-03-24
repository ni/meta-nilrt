
DESCRIPTION = "Tools for testing memory technology devices."
HOMEPAGE = "http://www.linux-mtd.infradead.org/"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://../../COPYING;md5=0636e73ff0215e8d672dc4c32c317bb3 \
                    file://../../include/common.h;beginline=1;endline=17;md5=ba05b07912a44ea2bf81ce409380049c"

SRC_URI = "git://git.infradead.org/mtd-utils.git;protocol=git;tag=ca39eb1d98e736109c64ff9c1aa2a6ecca222d8f"


S = "${WORKDIR}/git/tests/fs-tests"

FILES_${PN}-dbg = "${datadir}/mtd-utils/tests/utils/.debug"
FILES_${PN}-dbg = "${datadir}/mtd-utils/tests/integrity/.debug"
FILES_${PN}-dbg = "${datadir}/mtd-utils/tests/stress/atoms/.debug"
FILES_${PN}-dbg = "${datadir}/mtd-utils/tests/simple/.debug"

FILES_${PN} = "${datadir}/mtd-utils/tests/integrity/integck"
FILES_${PN} = "${datadir}/mtd-utils/tests/simple/ftrunc"
FILES_${PN} = "${datadir}/mtd-utils/tests/simple/test_1"
FILES_${PN} = "${datadir}/mtd-utils/tests/simple/test_2"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/fwrite00"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/gcd_hupper"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/pdfrun"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/rmdir00"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/rndrm00"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/rndrm99"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/rndwrite00"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/stress_1"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/stress_2"
FILES_${PN} = "${datadir}/mtd-utils/tests/stress/atoms/stress_3"
FILES_${PN} = "${datadir}/mtd-utils/tests/utils/free_space"
FILES_${PN} = "${datadir}/mtd-utils/tests/utils/fstest_monitor"

EXTRA_OEMAKE = "'CC=${CC}' 'RANLIB=${RANLIB}' 'AR=${AR}' 'CFLAGS=${CFLAGS} -I${S}/lib -I${WORKDIR}/git/ubi-utils/include -I${WORKDIR}/git/include -DWITHOUT_XATTR' 'BUILDDIR=${S}'"

PACKAGE_STRIP = "no"

mtd_utils_tests = " \
	help_all.sh \
	run_all.sh \
	integrity/integck \
	simple/ftrunc \
	simple/test_1 \
	simple/test_2 \
	stress/stress00.sh \
	stress/stress01.sh \
	stress/atoms/fwrite00 \
	stress/atoms/gcd_hupper \
	stress/atoms/pdfrun \
	stress/atoms/rmdir00 \
	stress/atoms/rndrm00 \
	stress/atoms/rndrm99 \
	stress/atoms/rndwrite00 \
	stress/atoms/stress_1 \
	stress/atoms/stress_2 \
	stress/atoms/stress_3 \
	utils/free_space \
	utils/fstest_monitor \
	"

do_configure_append () {
	# This is a hack; the main mtd-utils makefile usually generates this header
	# but since we want to avoid making mtd-utils generate it here instead
	echo "#define VERSION ${PV}" > ${WORKDIR}/git/include/version.h
}

do_install () {
	install -d ${D}${datadir}/mtd-utils/tests
	install -d ${D}${datadir}/mtd-utils/tests/integrity
	install -d ${D}${datadir}/mtd-utils/tests/simple
	install -d ${D}${datadir}/mtd-utils/tests/stress
	install -d ${D}${datadir}/mtd-utils/tests/stress/atoms
	install -d ${D}${datadir}/mtd-utils/tests/utils
	for app in ${mtd_utils_tests}; do
		install -m 0755 $app ${D}${datadir}/mtd-utils/tests/$app
	done
}

