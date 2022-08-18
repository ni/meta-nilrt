SUMMARY = "NILRT transconf hooks needed for first boot into minimal image"
LICENSE = "MIT"


inherit packagegroup


RDEPENDS:${PN} = "transconf"

RDEPENDS:${PN} += " \
	openssh-transconf \
	shadow-transconf \
"
