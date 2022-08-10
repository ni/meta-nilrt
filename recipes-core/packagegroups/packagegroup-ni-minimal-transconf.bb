SUMMARY = "NILRT transconf hooks needed for first boot into minimal image"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = " \
	initscripts-transconf \
	openssh-transconf \
	shadow-transconf \
	transconf \
"
