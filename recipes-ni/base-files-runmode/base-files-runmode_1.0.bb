SUMMARY = "Miscellaneous files for runmode"
DESCRIPTION = "nilrt distro-specific miscellaneous files for runmode."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = "base"

SRC_URI = "file://README_File_Paths.txt \
	   file://README_File_Transfer.txt \
	   file://${MACHINE}/issue.template \
	   file://LICENSES \
	   file://natinst_libs.conf \
	   file://local_libs.conf \
	   file://ulimit.sh \
	   file://nisetlocale.sh \
	   file://sysctl.conf \
"

# Move main package before ${PN}-doc to make it  pick
# the LICENSES file (otherwise, it goes into ${PN}-doc
PACKAGES = "${PN}-dbg ${PN}-staticdev ${PN}-dev ${PN} ${PN}-doc ${PN}-locale"

FILES_${PN} += "README_File_Paths.txt \
		README_File_Transfer.txt \
		/usr/share/doc/LICENSES \
"

S = "${WORKDIR}"

do_install () {
	install -d -m 0755 ${D}${sysconfdir}/natinst/share/
	
	# README's
	install -m 0644 ${WORKDIR}/README_File_Paths.txt ${D}
	install -m 0644 ${WORKDIR}/README_File_Transfer.txt ${D}

	# /etc/issue
	install -d ${D}${sysconfdir}/
	install -m 0755 ${WORKDIR}/${MACHINE}/issue.template ${D}${sysconfdir}/

	# license information
	install -d ${D}/usr/share/doc/
	install -m 0644 ${WORKDIR}/LICENSES ${D}/usr/share/doc/

	# ld.so.conf includes the directory /etc/ld.so.conf.d, a standard
	# practice in linux distros, adding extra files to map our directories
	install -d ${D}${sysconfdir}/ld.so.conf.d/
	install -m 0644 ${WORKDIR}/natinst_libs.conf ${D}${sysconfdir}/ld.so.conf.d/
	install -m 0644 ${WORKDIR}/local_libs.conf ${D}${sysconfdir}/ld.so.conf.d/

	install -d ${D}${sysconfdir}/profile.d/

	# script for limiting stack sizes when a user logs in
	install -m 0755 ${WORKDIR}/ulimit.sh ${D}${sysconfdir}/profile.d/

	# script for setting locale when a user logs in
	install -m 0755 ${WORKDIR}/nisetlocale.sh ${D}${sysconfdir}/profile.d/

	# add sysctl.conf file to adjust system configuration parameters
	install -m 0644 ${WORKDIR}/sysctl.conf ${D}${sysconfdir}/

	# Certstore on config partition
	install -d -m 0775 ${D}/var/local/natinst/
	ln -s ${sysconfdir}/natinst/share/certstore ${D}/var/local/natinst/certstore
}
