SUMMARY = "Miscellaneous files for nilrt"
DESCRIPTION = "nilrt distro-specific miscellaneous files."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

SRC_URI = "file://README_File_Paths.txt \
	   file://README_File_Transfer.txt \
	   file://issue.template \
	   file://LICENSES \
	   file://natinst_libs.conf \
	   file://local_libs.conf \
	   file://ulimit.sh \
	   file://nisetlocale.sh \
	   file://sysctl.conf \
	   file://machine-info \
"

# Move main package before ${PN}-doc to make it  pick
# the LICENSES file (otherwise, it goes into ${PN}-doc
PACKAGES = "${PN}-dbg ${PN}-staticdev ${PN}-dev ${PN} ${PN}-doc ${PN}-locale"

FILES_${PN} += "README_File_Paths.txt \
		README_File_Transfer.txt \
		/usr/share/doc/LICENSES \
		/usr/lib/${TARGET_ARCH}-linux-gnu${ARCH_ABI_EXT} \
		/etc/ld.so.conf.d/multiarch_libs.conf \
		/usr/local/natinst/bin \
		/usr/local/natinst/lib \
"

S = "${WORKDIR}"

ARCH_ABI_EXT="${ABIEXTENSION}${@bb.utils.contains('TUNE_FEATURES','callconvention-hard','hf','',d)}"

DEPENDS += "niacctbase"

do_install () {
	install -d -m 0755 -o ${LVRT_USER} -g ${LVRT_GROUP} ${D}${sysconfdir}/natinst/share/

	# boot loader/init configuration directory
	install -d -m 0755 ${D}${sysconfdir}/niboot/

	# README's
	install -m 0644 ${WORKDIR}/README_File_Paths.txt ${D}
	install -m 0644 ${WORKDIR}/README_File_Transfer.txt ${D}

	# /etc/issue
	install -d ${D}${sysconfdir}/
	install -m 0644 ${WORKDIR}/issue.template ${D}${sysconfdir}/

	# /usr/natinst/local directories
	install -d -m 0755 ${D}/usr/local/natinst
	install -d -m 0755 ${D}/usr/local/natinst/bin
	install -d -m 0755 ${D}/usr/local/natinst/lib

	# log dir
	install -d -m 0775 -o ${LVRT_USER} -g ${LVRT_GROUP} ${D}/var/local/natinst
	install -d -m 0775 -o ${LVRT_USER} -g ${LVRT_GROUP} ${D}/var/local/natinst/log

	# license information
	install -d ${D}/usr/share/doc/
	install -m 0644 ${WORKDIR}/LICENSES ${D}/usr/share/doc/

	# Create multiarch installation directory and write proper path to
	# multiarch.conf
	install -d ${D}/usr/lib/${TARGET_ARCH}-linux-gnu${ARCH_ABI_EXT}

	# ld.so.conf includes the directory /etc/ld.so.conf.d, a standard
	# practice in linux distros, adding extra files to map our directories
	install -d ${D}${sysconfdir}/ld.so.conf.d/
	install -m 0644 ${WORKDIR}/natinst_libs.conf ${D}${sysconfdir}/ld.so.conf.d/
	install -m 0644 ${WORKDIR}/local_libs.conf ${D}${sysconfdir}/ld.so.conf.d/
	echo /usr/lib/${TARGET_ARCH}-linux-gnu${ARCH_ABI_EXT} > ${D}${sysconfdir}/ld.so.conf.d/multiarch_libs.conf

	install -d ${D}${sysconfdir}/profile.d/

	# script for limiting stack sizes when a user logs in
	install -m 0644 ${WORKDIR}/ulimit.sh ${D}${sysconfdir}/profile.d/

	# script for setting locale when a user logs in
	install -m 0644 ${WORKDIR}/nisetlocale.sh ${D}${sysconfdir}/profile.d/

	# add sysctl.conf file to adjust system configuration parameters
	install -m 0644 ${WORKDIR}/sysctl.conf ${D}${sysconfdir}/

	# add machine-info and allow System Web Server to modify it
	install -m 0664 -g ${LVRT_GROUP} ${WORKDIR}/machine-info ${D}${sysconfdir}/
}
