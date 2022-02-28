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
	packagegroup-ni-next-kernel \
	kernel-test-nohz \
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
	gperf \
	htop \
	iperf2 \
	iperf3 \
	kernel-performance-tests \
	ldd \
	ltrace \
	ntpdate \
	openssl-dev \
	perf \
	python3-nose \
	python3-pip \
	python3-psutil \
	python3-setuptools \
	python3-dev \
	python3-misc \
	python3-pip \
	rsync \
	sshpass \
	strace \
	sysconfig-settings-ui \
	valgrind \
	vim \
"
