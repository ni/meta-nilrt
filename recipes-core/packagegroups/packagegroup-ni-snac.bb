SUMMARY = "Open source package dependencies for the NILRT SNAC configuration."
LICENSE = "MIT"


inherit packagegroup


RDEPENDS:${PN} = "\
	cryptsetup \
	firewalld \
	libpwquality \
	nilrt-snac \
	ntp \
	tmux \
	wireguard-tools \
"
