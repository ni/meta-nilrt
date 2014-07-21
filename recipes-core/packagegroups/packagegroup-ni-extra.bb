SUMMARY = "Bag of goodies for NI Linux Realtime distribution"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

RDEPENDS_${PN} = "\
	packagegroup-core-sdk \
	packagegroup-core-standalone-sdk-target \
	packagegroup-core-buildessential \
	packagegroup-core-basic \
	packagegroup-core-tools-debug \
"

# packagegroup-self-hosted-graphics - x11


# packagegroup-self-hosted-host-tools - x11
RDEPENDS_${PN} += "\
	connman \
	connman-plugin-ethernet \
	dhcp-client \
	e2fsprogs \
	e2fsprogs-e2fsck \
	e2fsprogs-mke2fs \
	e2fsprogs-tune2fs \
	genext2fs \
	hdparm \
	iptables \
	lsb \
	mc \
	parted \
	pseudo \
	screen \
${@base_contains("DISTRO_FEATURES", "x11", " \
	vte \
", "", d)} \
"

# packagegroup-self-hosted-debug - x11
RDEPENDS_${PN} += "\
	gdb \
	gdbserver \
	rsync \
	strace \
	tcf-agent \
"
# packagegroup-self-hosted-sdk - x11
RDEPENDS_${PN} += "\
	autoconf \
	automake \
	binutils \
	binutils-symlinks \
	ccache \
	coreutils \
	cpp \
	cpp-symlinks \
	distcc \
	eglibc-utils \
	eglibc-gconv-ibm850 \
	file \
	findutils \
	g++ \
	g++-symlinks \
	gcc \
	gcc-symlinks \
	intltool \
	ldd \
	less \
	libssp \
	libssp-dev \
	libssp-staticdev \
	libstdc++ \
	libstdc++-dev \
	libtool \
	make \
	mktemp \
	perl-module-re \
	perl-module-text-wrap \
	pkgconfig \
	quilt \
	sed \
"

# packagegroup-self-hosted-extended - x11
RDEPENDS_${PN} += "\
	bzip2 \
	chkconfig \
	chrpath \
	cpio \
	curl \
	diffstat \
	diffutils \
	elfutils \
	expat \
	gamin \
	gawk \
	gdbm \
	gettext \
	gettext-runtime \
	git \
	grep \
	groff \
	gzip \
	hicolor-icon-theme \
	libaio \
	libusb1 \
	libxml2 \
	lrzsz \
	lsof \
	lzo \
	man \
	man-pages \
	mdadm \
	minicom \
	mtools \
	ncurses \
	neon \
	nfs-utils \
	nfs-utils-client \
	openssl \
	openssh-sftp-server \
	opkg \
	opkg-utils \
	patch \
	perl \
	perl-dev \
	perl-modules \
	perl-pod \
	python \
	python-compile \
	python-compiler \
	python-compression \
	python-core \
	python-curses \
	python-datetime \
	python-distutils \
	python-elementtree \
	python-email \
	python-fcntl \
	python-logging \
	python-misc \
	python-multiprocessing \
	python-netclient \
	python-netserver \
	python-pickle \
	python-pkgutil \
	python-re \
	python-rpm \
	python-shell \
	python-sqlite3 \
	python-subprocess \
	python-textutils \
	python-unixadmin \
	python-xmlrpc \
	quota \
	readline \
	rpm \
	setserial \
	socat \
	subversion \
	sudo \
	sysstat \
	tar \
	tcl \
	texi2html \
	texinfo \
	unzip \
	usbutils \
	watchdog \
	wget \
	which \
	xinetd \
	zip \
	zlib \
"

#    ${PTH} 

RDEPENDS_${PN} += "\
	bind \
	connman \
	dhcp \
	libpcap \
	nfs-utils \
	ppp \
	socat \
"

RDEPENDS_${PN} += "\
	bison \
	btrfs-tools \
	dosfstools \
	flex \
	mtools \
	perl \
	libxml-sax-perl \
	libxml-namespacesupport-perl \
	libtimedate-perl \
	libconvert-asn1-perl \
	prelink \
	python \
	rsync \
	valgrind \
	trace-cmd \
${@base_contains("DISTRO_FEATURES", "x11", " \
qemu \
", "", d)} \
"
# nasm - zynq

RDEPENDS_${PN} += "\
	augeas \
	blktool \
	cups \
	foomatic-filters \
	gamin \
	ghostscript \
	groff \
	hdparm \
	iputils \
	lsof \
	mailx \
	man \
	man-pages \
	minicom \
	msmtp \
	parted \
	psmisc \
	screen \
	sed \
	stat \
	texi2html \
	texinfo \
	time \
	wget \
	which \
	xinetd \
	xz \
	zip \
"

RDEPENDS_${PN} += "\
	libdumpvalue-perl \
	libenv-perl \
	libfile-checktree-perl \
	libi18n-collate-perl \
	libpod-plainer-perl \
	rt-tests \
	aspell \
	boost \
	createrepo \
	curl \
	enchant \
	gnupg \
	lzo \
	lzop \
${@base_contains("DISTRO_FEATURES", "x11", " \
consolekit \
", "", d)} \
"

RDEPENDS_${PN} += "\
	bonnie++ \
	iozone3 \
	iperf \
	lmbench \
	memtester \
	iw \
	krb5 \
	rfkill \
	samba \
	smbnetfs \
	sshfs-fuse \
${@base_contains("DISTRO_FEATURES", "x11", " \
networkmanager \
\
", "", d)} \
"

# meta-oe/recipes-devtools
RDEPENDS_${PN} += "\
	bootchart \
	cppunit \
	lemon \
	ltrace \
	lua5.1 \
${@base_contains("DISTRO_FEATURES", "x11", " \
packagekit \
tk \
", "", d)} \
"

# php - fail
# nodejs - fail

# meta-oe/recipes-devtools/python
RDEPENDS_${PN} += "\
	pyrtm \
	python-cheetah \
	python-cython \
	python-dateutil \
	python-docutils \
	python-epydoc \
	python-feedparser \
	python-gdata \
	python-gevent \
	python-greenlet \
	python-ldap \
	python-lxml \
	python-m2crypto \
	python-nose \
	python-numeric \
	python-pexpect \
	python-psutil \
	python-pyalsaaudio \
	python-pyopenssl \
	python-pyserial \
	python-pyudev \
	python-pyusb \
	python-pyyaml \
	python-simplejson \
	python-smbus \
	python-sqlalchemy \
	python-tornado \
	python-twisted \
	python-vobject \
	python-webdav \
	python-zopeinterface \
${@base_contains("DISTRO_FEATURES", "x11", " \
python-sip python-pyqt \
", "", d)} \
"
# python-matplotlib - conflicts with several other pkgs

# python-numpy - fail
# python-mako - fail

# meta-oe/recipes-extended
RDEPENDS_${PN} += "\
	dialog \
	hexedit \
	hplip \
	iotop \
	redis \
	rsyslog \
	tcsh \
	tmux \
	zram \
	zsh \
${@base_contains("DISTRO_FEATURES", "x11", " \
gnuplot polkit \
", "", d)} \
"
# net-snmp - build fails


# meta-oe/recipes-graphics
RDEPENDS_${PN} += "\
${@base_contains("DISTRO_FEATURES", "x11", " \
	gimp \
	jasper \
	openbox \
	ttf-dejavu \
	ttf-droid \
	ttf-gentium \
	ttf-inconsolata \
	ttf-liberation \
	ttf-ubuntu-font-family \
	iceauth \
	sessreg \
	setxkbmap \
	twm \
	xterm \
", "", d)} \
"

# meta-oe/recipes-multimedia
RDEPENDS_${PN} += "\
${@base_contains("DISTRO_FEATURES", "x11", " \
	audiofile \
	media-ctl \
	v4l-utils \
", "", d)} \
"
# vlc, mplayer requires libmad which is on a non-whitelisted license

# meta-oe/recipes-networking
RDEPENDS_${PN} += "\
	ntp \
	netcat \
	bridge-utils \
	cifs \
	traceroute \
	tunctl \
"

# meta-oe/recipes-support
RDEPENDS_${PN} += "\
	ca-certificates \
	ckermit \
	cpufrequtils \
	cramfs \
	cryptsetup \
	debianutils \
	daemonize \
	devmem2 \
	eject \
	fftw \
	fuse \
	gd \
	i2c-tools \
	links \
	lockdev \
	lvm2 \
	mbuffer \
	mg \
	multipath-tools \
	mysql5 \
	nano \
	nmon \
	ntfs-3g-ntfsprogs \
	openldap \
	picocom \
	postgresql \
	serial-forward \
	rng-tools \
	sqlite \
	start-stop-daemon \
	strongswan \
	tcpdump \
	usbpath \
	vim \
	xmlto \
	yasm \
${@base_contains("DISTRO_FEATURES", "x11", " \
lcms \
pidgin \
poppler \
imagemagick \
", "", d)} \
"
# ccid -- requires pcsc, whose tarball is 0 size
# opencv opencv-samples - requires python-numpy
# lzip - fail
# emacs - fail
# lzma - conflicts with xz?
# openmotif - mirror down


RDEPENDS_${PN} += "${@base_contains("DISTRO_FEATURES", "x11", " florence ", "", d)}"
