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
	binutils \
	cgdb \
	cifs-utils \
	elfutils \
	file \
	g++ \
	gcc \
	gdb \
	git \
	htop \
	ldd \
	ltrace \
	mysql-python \
	ntpdate \
	openssl-dev \
	python-modules \
	python-nose \
	python-pip \
	python-psutil \
	python-setuptools \
	python3-dev \
	python3-misc \
	python3-pip \
	rsync \
	sshpass \
	strace \
	valgrind \
	vim \
"
