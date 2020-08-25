# Remove Debian's pic.patch from NILRT's build of OpenSSL. The
# changes are not properly scoped to Linux only and break
# Windows/msvc builds which consume NILRT/OE source packages. The
# modified perl scripts are also not called during either the x64 or
# xilinx-zynqhf builds of NILRT, so there's no real value in
# improving the change.

SRC_URI_remove = "file://debian/pic.patch"
