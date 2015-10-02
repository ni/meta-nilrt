SUMMARY = "SysV nilrt init scripts"
DESCRIPTION = "nilrt distro-specific initscripts to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
LICENSE = "MIT"
SECTION = "base"

DEPENDS += "niacctbase"

RDEPENDS_${PN} += "niacctbase"

SRC_URI = "file://populateconfig \
	   file://nisetbootmode \
	   file://nisetprimarymac \
	   file://firewall \
	   file://mountdebugfs \
	   file://nicleanstalelinks \
	   file://nicreatecpusets \
	   file://nicreatecpuacctgroups \
	   file://nisetupkernelconfig \
	   file://nisetcommitratio \
	   file://nivalidatesystem \
	   file://wirelesssetdomain \
	   file://iso3166-translation.txt \
	   file://nisetupirqpriority \
"

SRC_URI_append_x64 = "file://nidisablecstates \
                      file://nicheckbiosconfig \
"

S = "${WORKDIR}"

group = "${LVRT_GROUP}"

do_install () {
     install -d ${D}${sysconfdir}/init.d/
     install -m 0755    ${S}/populateconfig     ${D}${sysconfdir}/init.d
     install -m 0550   ${S}/nisetbootmode       ${D}${sysconfdir}/init.d
     chown 0:${group} ${D}${sysconfdir}/init.d/nisetbootmode
     install -m 0755    ${WORKDIR}/nisetprimarymac    ${D}${sysconfdir}/init.d
     install -m 0755    ${WORKDIR}/firewall    ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/mountdebugfs         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nicleanstalelinks         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nicreatecpusets         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nicreatecpuacctgroups         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nisetupkernelconfig         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nisetcommitratio         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nivalidatesystem         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/wirelesssetdomain         ${D}${sysconfdir}/init.d
     install -m 0755   ${S}/nisetupirqpriority         ${D}${sysconfdir}/init.d

     install -d ${D}${sysconfdir}/natinst
     install -m 0644   ${WORKDIR}/iso3166-translation.txt         ${D}${sysconfdir}/natinst

     update-rc.d -r ${D} populateconfig start 36 S .
     update-rc.d -r ${D} nisetbootmode start 80 S . stop 0 0 6 .
     update-rc.d -r ${D} nisetprimarymac start 4 5 .
     update-rc.d -r ${D} firewall start 39 S .
     update-rc.d -r ${D} mountdebugfs start 82 S .
     update-rc.d -r ${D} nicleanstalelinks start 5 S .
     update-rc.d -r ${D} nicreatecpusets start 1 5 .
     update-rc.d -r ${D} nicreatecpuacctgroups start 2 5 .
     update-rc.d -r ${D} nisetupkernelconfig start 3 5 .
     update-rc.d -r ${D} nisetcommitratio start 99 S .
     update-rc.d -r ${D} nivalidatesystem start 40 S .
     update-rc.d -r ${D} wirelesssetdomain start 36 S .

     # CAR 450019: Remove this code (and associated script) and migrate responsibility for
     # setting IRQ thread priority to RIO nisetupirqpriority script that sets up kernel
     # config parameters
     if [ "${TARGET_ARCH}" = "arm" ]; then
          update-rc.d -r ${D} nisetupirqpriority start 4 5 .
     else
          update-rc.d -r ${D} nisetupirqpriority start 30 5 .
     fi

     # CAR 450019: Remove this code (and associated script) and migrate responsibility for
     # setting IRQ thread priority to RIO nisetupirqpriority script that sets up kernel
     # config parameters
     if [ "${TARGET_ARCH}" = "arm" ]; then
          update-rc.d -r ${D} nisetupirqpriority start 4 5 .
     else
          update-rc.d -r ${D} nisetupirqpriority start 30 5 .
     fi
}

do_install_append_x64 () {
     install -m 0755   ${WORKDIR}/nidisablecstates      ${D}${sysconfdir}/init.d
     update-rc.d -r ${D} nidisablecstates start 2 3 4 5 S .
     install -m 0755   ${WORKDIR}/nicheckbiosconfig      ${D}${sysconfdir}/init.d
     update-rc.d -r ${D} nicheckbiosconfig start 99 5 .
}
