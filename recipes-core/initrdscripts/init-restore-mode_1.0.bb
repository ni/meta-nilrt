SUMMARY = "Extremely basic live image init script"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SRC_URI = "file://init-restore-mode.sh \
           file://ni_provisioning	   \
           file://grub.cfg"

PR = "r1"

S = "${WORKDIR}"

do_install() {
    install -m 0755 ${S}/init-restore-mode.sh ${D}/init
    install -m 0755 ${S}/ni_provisioning ${D}/ni_provisioning
    install -m 0755 ${S}/grub.cfg ${D}/grub.cfg
}

inherit allarch

FILES_${PN} += " /init /ni_provisioning /grub.cfg"
