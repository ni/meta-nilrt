SUMMARY = "NILRT transconf hooks needed for first boot into minimal image"
LICENSE = "MIT"


inherit packagegroup


RDEPENDS:${PN} = " \
	initscripts-transconf \
	openssh-transconf \
	shadow-transconf \
	transconf \
"
