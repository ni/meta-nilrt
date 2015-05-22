PACKAGES =+ "${PN}-gdisk ${PN}-cgdisk ${PN}-sgdisk ${PN}-fixparts"
FILES_${PN}-gdisk = "${sbindir}/gdisk"
FILES_${PN}-cgdisk = "${sbindir}/cgdisk"
FILES_${PN}-sgdisk = "${sbindir}/sgdisk"
FILES_${PN}-fixparts = "${sbindir}/fixparts"

RDEPENDS_${PN} += "\
       ${PN}-gdisk \
       ${PN}-cgdisk \
       ${PN}-sgdisk \
       ${PN}-fixparts \
"

ALLOW_EMPTY_${PN} = "1"
