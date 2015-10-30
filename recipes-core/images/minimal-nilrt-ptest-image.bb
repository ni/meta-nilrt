DESCRIPTION = "Minimal ptest image for NI Linux Real-Time x64 and ARM targets"
LICENSE = "MIT"

require minimal-nilrt-image.bb

IMAGE_FEATURES+=" ptest-pkgs"

IMAGE_INSTALL += "\
	ptest-runner \
	rt-tests \
"
