DESCRIPTION = "Add grub.cfg to NI Linux RT x86 64bit boot mechanism"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                     file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PR = "r1"

SECTION = "base"

SRC_URI = "file://grub.cfg \
"

FILES_${PN} += "/boot/* \
"

do_install () {
	install -d ${D}/boot/
	install -m 0755 ${WORKDIR}/grub.cfg ${D}/boot/
}

