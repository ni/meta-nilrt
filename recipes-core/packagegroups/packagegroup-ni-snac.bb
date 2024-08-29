SUMMARY = "Open source package dependencies for the NILRT SNAC configuration."
LICENSE = "MIT"


inherit packagegroup


RDEPENDS:${PN} = "\
	cryptsetup \
	firewalld \
	ntp \
	tmux \
	libpwquality \
"
