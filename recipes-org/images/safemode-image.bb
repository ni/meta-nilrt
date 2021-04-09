SUMMARY = "Safemode image for older nilrt"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "\
	file://grubenv_non_ni_target \
	file://unicode.pf2 \
"

RDEPENDS_${PN} += "grub-efi grub"
COMPATIBLE_MACHINE = "x64"

inherit build-services
# optional env var with path to local safemode tar.gz
export SAFEMODE_PAYLOAD_PATH
EXPORTS_TO_FETCH = "$EXPORTS_TO_FETCH"

do_fetch_prepend() {
        mkdir -p ${BS_EXPORT_DATA}

        if [ -z "$SAFEMODE_PAYLOAD_PATH" ]; then
                EXPORTS_TO_FETCH=$(bs_get_latest_export "nilinux/os-common/export/8.0")"/standard_x64_safemode.tar.gz"
                SAFEMODE_PAYLOAD=$EXPORTS_TO_FETCH
        else
                SAFEMODE_PAYLOAD="${SAFEMODE_PAYLOAD_PATH}/standard_x64_safemode.tar.gz"
                cp -f "$SAFEMODE_PAYLOAD" ${BS_EXPORT_DATA}
        fi

        echo SAFEMODE_PAYLOAD=$SAFEMODE_PAYLOAD >${BS_EXPORT_DATA}/safemode_version_info
        echo SAFEMODE_MAJOR_VERSION=`echo $SAFEMODE_PAYLOAD |egrep -o "\/[0-9]+\.[0-9]+\/" |tr -d "/"` >>${BS_EXPORT_DATA}/safemode_version_info
        echo SAFEMODE_MINOR_VERSION=`echo $SAFEMODE_PAYLOAD |egrep -o "\/[0-9]+\.[0-9]+.[0-9]+[abdf][0-9]+\/" |tr -d "/"` >>${BS_EXPORT_DATA}/safemode_version_info
}

do_install() {
	mkdir -p ${D}/payload/fonts

	tar -xf "${BS_EXPORT_DATA}/standard_x64_safemode.tar.gz" -C ${D}/payload

	cp ${WORKDIR}/grubenv_non_ni_target	${D}/payload
	cp ${WORKDIR}/unicode.pf2		${D}/payload/fonts

	GRUB_VERSION=$(echo ${GRUB_BRANCH} | cut -d "/" -f 2)

	echo "BUILD_IDENTIFIER=${BUILD_IDENTIFIER}" > ${D}/payload/imageinfo
	echo "GRUB_VERSION=${GRUB_VERSION}.0" >> ${D}/payload/imageinfo
}

sysroot_stage_all_append() {
        install -m 0755 ${BS_EXPORT_DATA}/safemode_version_info ${SYSROOT_DESTDIR}
}

# always invalidate the sstate-cache for do_install and do_fetch as we depend on an external
# file (standard_x64_safemode.tar.gz) which is not cached as part of sstate
do_fetch[nostamp] = "1"
do_install[nostamp] = "1"

FILES_${PN} = "\
	/payload \
"
