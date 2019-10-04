SUMMARY = "Partition the target with the original safemode partioning scheme"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

ALLOW_EMPTY_${PN}-dbg = "0"
ALLOW_EMPTY_${PN}-dev = "0"

SRC_URI += "\
    file://nilrt-gateway-install \
"

DEPENDS = "safemode-image"
RDEPENDS_${PN} += "bash"
do_install[depends] = "safemode-restore-image:do_image_complete"

do_install_x64() {
    install -d ${D}/usr/share/nilrt
    install -m 0755 ${WORKDIR}/nilrt-gateway-install ${D}/usr/share/nilrt/nilrt-install
    install -m 0755 ${DEPLOY_DIR_IMAGE}/safemode-restore-image-${MACHINE}.wic ${D}/usr/share/nilrt/safemode-restore-image-${MACHINE}.iso
}

python do_package_prepend() {
    found = False
    svi = "%s/safemode_version_info" % d.getVar('STAGING_DIR_HOST')
    with open(svi) as fp:
        line = fp.readline()
        while line:
             x = line.split("=", 1)
             if x[0].rstrip() == "SAFEMODE_MAJOR_VERSION":
                 bb.debug(2, "Found safemode version: %s" % x[1].rstrip())
                 d.setVar('PKGV', x[1].rstrip())
                 found = True
             line = fp.readline()
    if found != True:
        bb.fatal("Safemode version not found (check safemode-image recipe) !!!")
}

FILES_${PN} = "\
    /usr/share/nilrt/safemode-restore-image-${MACHINE}.iso \
    /usr/share/nilrt/nilrt-install \
"
