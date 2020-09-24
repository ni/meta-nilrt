SUMMARY = "NILRT transconf hooks needed for first boot into minimal image"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "transconf"

RDEPENDS_${PN} += " \
	openssh-transconf \
	shadow-transconf \
"
