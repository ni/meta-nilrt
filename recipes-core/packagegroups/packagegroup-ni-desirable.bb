SUMMARY = "Non-critical, but desirable packages. These packages are not \
permitted to fail during build."
LICENSE = "MIT"

inherit packagegroup

# essential packagegroups
RDEPENDS:${PN} += "\
	packagegroup-core-buildessential \
	packagegroup-core-tools-debug \
	packagegroup-ni-debug-kernel \
	packagegroup-ni-selinux \
"

RDEPENDS:${PN}:append:x64 = "\
	packagegroup-ni-next-kernel \
"

RDEPENDS:${PN} += "\
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
	ldd \
	ltrace \
	ntp \
	ntpdate \
	nvme-cli \
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
	trace-cmd \
	valgrind \
	vim \
"

# Testing packages
RDEPENDS:${PN} += "\
	kernel-performance-tests \
	kernel-containerized-performance-tests \
	ni-base-system-image-tests \
"
RDEPENDS:${PN}:append:x64 = "\
	kernel-test-nohz \
"
