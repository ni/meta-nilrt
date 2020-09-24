DESCRIPTION ?= "NI LinuxRT ramfs for booting NI targets."
LICENSE = "MIT"

PACKAGE_INSTALL = "init-nilrt-ramfs"

BAD_RECOMMENDATIONS += "shared-mime-info ca-certificates"

require nilrt-image-common.inc
