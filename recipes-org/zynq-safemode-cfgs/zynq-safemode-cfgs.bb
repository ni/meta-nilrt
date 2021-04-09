SUMMARY = "Packages containing older nilrd ARM .cfg files + a script to install them"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "file://niinstallsafemode"

inherit build-services

EXPORTS_TO_FETCH = "\
    NI-RIO/controller/bsp/zynq/boot_image/export/5.0/5.0.0f0/safemode/release/Firmware \
    Measurements/FlexRIO/SWCode/bsp/zynq/boot_image/export/5.0/5.0.0f0/safemode/release/Firmware \
"

python () {
    dev_codes = d.getVar('NILRT_ARM_DEVICE_CODES', True) or ""
    pn = d.getVar("PN", True)

    for devc in dev_codes.split(" "):
        sdevc = devc.strip()
        if sdevc:
           d.prependVar("PACKAGES", "{0}-{1} ".format(pn, sdevc.lower()))
           d.setVar("FILES_{0}-{1}".format(pn, sdevc.lower()), "/{0}/*".format(sdevc))
           d.setVar("RDEPENDS_{0}-{1}".format(pn, sdevc.lower()), "bash")
}

do_install() {
    exitCode=0
    for devid in ${NILRT_ARM_DEVICE_CODES}
    do
        fpath="$(ls ${BS_EXPORT_DATA}/Firmware/*/$devid/*.cfg || true)"
        if [ -n "$fpath" ]; then
            mkdir ${D}/$devid
            install -m 0644 "$fpath"                        ${D}/$devid
            install -m 0755 ${WORKDIR}/niinstallsafemode    ${D}/$devid
        else
            bberror "${PN}: No firmware file found for device $devid"
            exitCode=1
        fi
    done
    return "$exitCode"
}
