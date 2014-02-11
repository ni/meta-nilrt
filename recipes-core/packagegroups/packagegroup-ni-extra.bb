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
	vte \
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
"
# nasm - zynq
# qemu - x11

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
"
# consolekit - x11

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
"

# networkmanager - x11

RDEPENDS_${PN} += "\
	bootchart \
	cppunit \
	lua5.1 \
"

# php - fail
# nodejs - fail
# packagekit - x11

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
"
# python-matplotlib - conflicts with several other pkgs


# python-numpy - fail
# python-mako - fail
# python-sip - x11
# python-pyqt - x11

RDEPENDS_${PN} += "\
	hexedit \
	hplip \
	iotop \
	redis \
	rsyslog \
	tcsh \
	tmux \
	zram \
	zsh \
"
# gnuplot - x11
# polkit - x11


RDEPENDS_${PN} += "\
	ca-certificates \
	ckermit \
	cramfs \
	debianutils \
	eject \
	fftw \
	fuse \
	gd \
	i2c-tools \
	imagemagick \
	mysql5 \
	nano \
	nmon \
	openldap \
	picocom \
	postgresql \
	rng-tools \
	sqlite \
	start-stop-daemon \
	strongswan \
	tcpdump \
	vim \
	xmlto \
	yasm \
"

# lzip - fail
# emacs - fail
# poppler - x11
# lzma - conflicts with xz?
