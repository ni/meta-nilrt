SUMMARY = "Non-critical, but desirable packages. These packages are not \
permitted to fail during build."
LICENSE = "MIT"

inherit packagegroup

# essential packagegroups
RDEPENDS_${PN} += "\
	packagegroup-core-tools-debug \
	packagegroup-ni-debug-kernel \
	packagegroup-ni-ptest \
	packagegroup-ni-selinux \
"

RDEPENDS_${PN}_append_x64 = "\
	packagegroup-ni-mono-extra \
	florence \
"

RDEPENDS_${PN} += "\
	cgdb \
	cifs-utils \
	elfutils \
	file \
	gdb \
	ldd \
	ltrace \
	mysql-python \
	strace \
	valgrind \
"
