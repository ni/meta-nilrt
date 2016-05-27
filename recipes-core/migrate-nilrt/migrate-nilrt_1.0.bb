SUMMARY = "Scripts for migrating NI Targets from LV2015 to LVNext and back"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "file://ni_migrate_target "

inherit allarch

RDEPENDS_${PN} += " bash "

S = "${WORKDIR}"

do_install() {
        install -d ${D}${base_sbindir}/
	sed -e "s#NEXT_FEED_URI=\"\"#NEXT_FEED_URI=\"${NILRT_FEED_URI}\"#" \
            -e "s/NILRT_ARM_MIGRATION_SUPPORTED_DEVICES=\"\"/NILRT_ARM_MIGRATION_SUPPORTED_DEVICES=\'${NILRT_ARM_MIGRATION_SUPPORTED_DEVICES}\'/" \
	     ${S}/ni_migrate_target > ${D}${base_sbindir}/ni_migrate_target
	chmod +x ${D}${base_sbindir}/ni_migrate_target
}

FILES_${PN} += "${base_sbindir}/ni_migrate_target "
