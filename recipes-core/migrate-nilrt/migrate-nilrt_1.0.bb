SUMMARY = "Scripts for migrating NI Targets from LV2015 to LVNext and back"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://ni_migrate_target "

inherit allarch

RDEPENDS_${PN} += " bash "

S = "${WORKDIR}"

PACKAGES = "${PN}"

do_install() {
        install -d ${D}${base_sbindir}/
	sed -e "s#NEXT_FEED_URI=\"\"#NEXT_FEED_URI=\"${NILRT_FEEDS_URI}/${NILRT_RELEASE_NAME}\"#" \
            -e "s/NILRT_ARM_MIGRATION_SUPPORTED_DEVICES=\"\"/NILRT_ARM_MIGRATION_SUPPORTED_DEVICES=\'${NILRT_ARM_DEVICE_CODES}\'/" \
	     ${S}/ni_migrate_target > ${D}${base_sbindir}/ni_migrate_target
	chmod +x ${D}${base_sbindir}/ni_migrate_target
}

FILES_${PN} += "${base_sbindir}/ni_migrate_target "
