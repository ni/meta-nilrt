SUMMARY = "SysV nilrt init scripts"
DESCRIPTION = "nilrt distro-specific initscripts to provide basic system functionality."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

inherit ptest

DEPENDS += "shadow-native pseudo-native update-rc.d-native niacctbase"

RDEPENDS_${PN} += "bash niacctbase update-rc.d"
RDEPENDS_${PN}-ptest += "bash"

DEPENDS_append_x64 = " nilrtdiskcrypt "
RDEPENDS_${PN}_append_x64 = " nilrtdiskcrypt "

SRC_URI = "file://nisetbootmode \
	   file://firewall \
	   file://mountdebugfs \
	   file://nicleanstalelinks \
	   file://nicreatecpusets \
	   file://nicreatecpuacctgroups \
	   file://nisetupkernelconfig \
	   file://nisetcommitratio \
	   file://test-nisetcommitratio-common.sh \
	   file://test-nisetcommitratio-system \
	   file://test-nisetcommitratio-unit \
	   file://nisetled \
	   file://wirelesssetdomain \
	   file://iso3166-translation.txt \
	   file://nisetupirqpriority \
	   file://nipopulateconfigdir \
	   file://run-ptest \
"

SRC_URI_append_x64 = "file://nidisablecstates \
                      file://nicheckbiosconfig \
                      file://niopendisks \
                      file://niclosedisks \
                      file://test-niopendisks-init \
                      file://test-niclosedisks-init \
"

S = "${WORKDIR}"

do_install () {
     install -d ${D}${sysconfdir}/init.d/
     install -m 0550   ${S}/nisetbootmode       ${D}${sysconfdir}/init.d
     chown 0:${LVRT_GROUP} ${D}${sysconfdir}/init.d/nisetbootmode
     install -m 0755    ${WORKDIR}/firewall    ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/mountdebugfs         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nicleanstalelinks         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nicreatecpusets         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nicreatecpuacctgroups         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nipopulateconfigdir         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nisetupkernelconfig         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/nisetcommitratio         ${D}${sysconfdir}/init.d
     install -m 0755   ${WORKDIR}/wirelesssetdomain         ${D}${sysconfdir}/init.d
     install -m 0755   ${S}/nisetupirqpriority         ${D}${sysconfdir}/init.d

     install -d ${D}${sysconfdir}/natinst
     install -m 0644   ${WORKDIR}/iso3166-translation.txt         ${D}${sysconfdir}/natinst

     update-rc.d -r ${D} nisetbootmode start 80 S . stop 0 0 6 .
     update-rc.d -r ${D} firewall start 39 S .
     update-rc.d -r ${D} mountdebugfs start 82 S .
     update-rc.d -r ${D} nicleanstalelinks start 5 S .
     update-rc.d -r ${D} nicreatecpusets start 1 5 .
     update-rc.d -r ${D} nicreatecpuacctgroups start 2 5 .
     update-rc.d -r ${D} nipopulateconfigdir start 36 S .
     update-rc.d -r ${D} nisetupkernelconfig start 3 5 .
     update-rc.d -r ${D} nisetcommitratio start 99 S .
     update-rc.d -r ${D} wirelesssetdomain start 36 S .

     # only for nilrt-nxg, on older nilrt it's installed via p4
     if ${@oe.utils.conditional('DISTRO', 'nilrt-nxg', 'true', 'false', d)}; then
          install -m 0755   ${WORKDIR}/nisetled         ${D}${sysconfdir}/init.d
          update-rc.d -r ${D} nisetled start 40 S .
     fi

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

pkg_postinst_${PN} () {
    if [ -n "$D" ]; then
        # Error in off-line install so this will run on the real target where we can query the target class
        exit 1
    else
        # Make sure /boot is mounted so that fw_printenv is usable
        if /sbin/fw_printenv TargetClass > /dev/null 2>&1; then
            mountstate=1
        else
            mountstate=0
            mount /boot
        fi

        class="`/sbin/fw_printenv -n TargetClass`"

        # Use persistent names on PXI, not on any other targets
        if [ "$class" != "PXI" ]; then
            touch /etc/udev/rules.d/80-net-name-slot.rules

            # Since the network is already brought up on the first boot, reload the network to get the new rules
            if ${@oe.utils.conditional('DISTRO', 'nilrt-nxg', 'true', 'false', d)}; then
                /etc/init.d/connman stop
                /etc/init.d/udev stop
                modprobe -r igb
                modprobe igb
                /etc/init.d/udev start
                /etc/init.d/connman start
            fi
        fi

        # Restore the original state of /boot
        [ $mountstate == 0 ] && umount /boot || true
    fi
}

do_install_append_x64 () {
     install -m 0755   ${WORKDIR}/nidisablecstates      ${D}${sysconfdir}/init.d
     update-rc.d -r ${D} nidisablecstates start 2 3 4 5 S .
     install -m 0755   ${WORKDIR}/nicheckbiosconfig      ${D}${sysconfdir}/init.d
     update-rc.d -r ${D} nicheckbiosconfig start 99 5 .

     install -m 0755   ${WORKDIR}/niopendisks   ${D}${sysconfdir}/init.d
     update-rc.d -r ${D} niopendisks start 00 S .

     install -m 0755   ${WORKDIR}/niclosedisks  ${D}${sysconfdir}/init.d
     update-rc.d -r ${D} niclosedisks start 41 0 . start 41 6 .
}

do_install_ptest () {
	cp ${WORKDIR}/test-nisetcommitratio-* ${D}${PTEST_PATH}/
}

do_install_ptest_append_x64 () {
	cp ${WORKDIR}/test-niopendisks-init  ${D}${PTEST_PATH}/
	cp ${WORKDIR}/test-niclosedisks-init  ${D}${PTEST_PATH}/
}
