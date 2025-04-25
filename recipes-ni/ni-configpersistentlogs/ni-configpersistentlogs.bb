SUMMARY = "NI persistent logs configuration utility"
DESCRIPTION = "Installs the ni-configpersistentlogs utility"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
SECTION = "base"

PV = "1.0"

SRC_URI = "\
	file://ni-configpersistentlogs \
	file://ni-configpersistentlogs.service \
"

S = "${WORKDIR}"

PATH_VOLATILES_SYSV="/etc/default/volatiles/00_core"
PERSISTENT_SYSV="d root root 0755 /var/log none"
VOLATILE_SYSV="l root root 0755 /var/log /var/volatile/log"
PATH_VOLATILES_SYSD="/usr/lib/tmpfiles.d/00-create-volatile.conf"
PERSISTENT_SYSD="d		/var/log		-	-	-	-"
VOLATILE_SYSD="L		/var/log		-	-	-	-	/var/volatile/log"

inherit update-rc.d systemd
INITSCRIPT_NAME = "ni-configpersistentlogs"
INITSCRIPT_PARAMS = "start 2 S ."
SYSTEMD_SERVICE:${PN} = "ni-configpersistentlogs.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install () {
	install -d ${D}${script_location}
	install -m 0755 ${S}/ni-configpersistentlogs ${D}${script_location}

	if ${@bb.utils.contains('INIT_MANAGER','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${WORKDIR}/ni-configpersistentlogs.service ${D}${systemd_system_unitdir}/
		sed -i "s|<PATH_VOLATILES>|${PATH_VOLATILES_SYSD}|g" ${D}${script_location}/ni-configpersistentlogs
		sed -i "s|<PERSISTENT_LINE>|${PERSISTENT_SYSD}|g" ${D}${script_location}/ni-configpersistentlogs
		sed -i "s|<VOLATILE_LINE>|${VOLATILE_SYSD}|g" ${D}${script_location}/ni-configpersistentlogs
	else
		sed -i "s|<PATH_VOLATILES>|${PATH_VOLATILES_SYSV}|g" ${D}${script_location}/ni-configpersistentlogs
		sed -i "s|<PERSISTENT_LINE>|${PERSISTENT_SYSV}|g" ${D}${script_location}/ni-configpersistentlogs
		sed -i "s|<VOLATILE_LINE>|${VOLATILE_SYSV}|g" ${D}${script_location}/ni-configpersistentlogs
	fi
}

script_location = "${@bb.utils.contains('INIT_MANAGER','sysvinit','${sysconfdir}/init.d','${systemd_unitdir}/scripts',d)}"
FILES:${PN} += "\
	${script_location}/ni-configpersistentlogs \
	${@bb.utils.contains('INIT_MANAGER','systemd','${systemd_system_unitdir}/ni-configpersistentlogs.service','',d)} \
"

RDEPENDS:${PN} += "\
	bash \
	${@bb.utils.contains('INIT_MANAGER','sysvinit',' initscripts',' systemd',d)} \
"
