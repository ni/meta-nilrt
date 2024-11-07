SUMMARY = "Package feed of potentially useful, but unsupported, packages for NI Linux RT"
LICENSE = "MIT"

inherit packagegroup

# These packages are only built for the nilrt-xfce fork of the older NILRT distro
# as opposed to nilrt-nxg which always enables x11 support. Also only for x64
# because NILRT ARM does not have a GUI.
RDEPENDS:${PN}:append:x64 = "\
	${@bb.utils.contains('DISTRO_FEATURES', 'x11', '\
		packagegroup-self-hosted \
		packagegroup-xfce-extended \
		consolekit \
		geany \
		gimp \
		gnuplot \
		gnuradio \
		gtk+3 \
		iceauth \
		fltk \
		libvncserver \
		libwmf \
		lxdm \
		mesa-demos \
		modemmanager \
		networkmanager \
		numlockx \
		openbox \
		sessreg \
		setxkbmap \
		tk \
		twm \
		upower \
		vte \
		x11vnc \
		xbitmaps \
		xclock \
		xcursorgen \
		xdotool \
		xfontsel \
		xlsfonts \
		xmag \
		xrdb \
		xterm \
		xwd \
	', '', d)} \
"

RDEPENDS:${PN} = "\
	packagegroup-core-buildessential \
	packagegroup-core-sdk \
	packagegroup-core-security \
	packagegroup-core-standalone-sdk-target \
	packagegroup-core-tools-debug \
	packagegroup-fonts-truetype \
	packagegroup-ni-debug-kernel \
	packagegroup-ni-desirable \
	packagegroup-ni-selinux \
"

# kernel regression tests
RDEPENDS:${PN} += "\
	kernel-test-fbomb \
"

RDEPENDS:${PN} += "\
	avahi \
	bind \
	cifs-utils \
	ofono \
	ppp \
	ppp-dialin \
	python3-salttesting \
	resolvconf \
	salt \
"

RDEPENDS:${PN} += "\
	psplash \
	sysfsutils \
"

# openembedded-core/meta/recipes-core
RDEPENDS:${PN} += "\
	libcgroup \
"

# openembedded-core/meta/recipes-bsp
RDEPENDS:${PN} += "\
	alsa-state \
	keymaps \
	lrzsz \
	setserial \
	usbutils \
"

# openembedded-core/meta/recipes-connectivity
RDEPENDS:${PN} += "\
	bluez5 \
"

# openembedded-core/meta/recipes-devtools
RDEPENDS:${PN} += "\
	apt \
	bison \
	bootchart2 \
	btrfs-tools \
	cmake \
	distcc \
	dosfstools \
	dpkg \
	expect \
	file \
	flex \
	gnu-config \
	i2c-tools \
	mmc-utils \
	mtd-utils \
	patchelf \
	pax-utils \
	python3-dbus \
	python3-imaging \
	python3-setuptools \
	python3-docutils \
	python3-mako \
	python3-nose \
	python3-numpy \
	python3-pyiface \
	python3-setuptools \
	rpm \
	rsync \
	unfs3 \
	vala \
	valgrind \
	xmlto \
"

# openembedded-core/meta/recipes-extended
RDEPENDS:${PN} += "\
	at \
	augeas \
	bc \
	blktool \
	byacc \
	cups \
	ghostscript \
	iputils \
	libarchive \
	lighttpd \
	ltp \
	msmtp \
	net-tools \
	pigz \
	libconvert-asn1-perl \
	libtimedate-perl \
	libxml-namespacesupport-perl \
	libxml-sax-perl \
	procps \
	psmisc \
	screen \
	shadow \
	slang \
	sysklogd \
	tcp-wrappers \
	time \
"

# openembedded-core/meta/recipes-rt
RDEPENDS:${PN} += "\
	rt-tests \
"

# openembedded-core/meta/recipes-support
RDEPENDS:${PN} += "\
	apr \
	aspell \
	attr \
	acl \
	boost \
	ca-certificates \
	createrepo-c \
	debianutils \
	enchant2 \
	gnupg \
	gnutls \
	gpgme \
	lz4 \
	lzop \
	nspr \
	nss \
	nss-myhostname \
	pinentry \
	ptest-runner \
"

# openembedded-core/meta/recipes-kernel
RDEPENDS:${PN} += "\
	blktrace \
	dkms \
	dtc \
	kexec-tools \
	kmod \
	linux-firmware \
	makedumpfile \
	oprofile \
	powertop \
	systemtap \
"

# openembedded-core/meta/recipes-multimedia
RDEPENDS:${PN} += "\
	alsa-tools \
	alsa-utils \
	alsa-utils-scripts \
	flac \
	pulseaudio \
	speex \
"

# meta-openembedded/meta-oe/recipes-benchmark
RDEPENDS:${PN} += "\
	bonnie++ \
	dbench \
	fio \
	iozone3 \
	iperf2 \
	iperf3 \
	lmbench \
	nbench-byte \
	phoronix-test-suite \
	tiobench \
"

# meta-openembedded/meta-oe/recipes-connectivity
RDEPENDS:${PN} += "\
	gammu \
	hostapd \
	iw \
	krb5 \
	linuxptp \
	lirc \
	mosh \
	rarpd \
	rfkill \
	samba \
	ser2net \
	tiptop \
	usbmuxd \
	zeromq \
"

# meta-openembedded/meta-oe/recipes-core
RDEPENDS:${PN} += "\
	usleep \
"

# meta-openembedded/meta-oe/recipes-devtools
RDEPENDS:${PN} += "\
	breakpad \
	cgdb \
	gdb \
	cppunit \
	ctags \
	cunit \
	dejagnu \
	i2c-tools \
	icon-slicer \
	jq \
	lemon \
	libedit \
	log4cplus \
	ltrace \
	lua \
	luajit \
	mcpp \
	memstat \
	nodejs \
	libio-pty-perl \
	php \
	pm-qa \
	protobuf \
	python3-pyopenssl \
	python3-simplejson \
	python3-tornado \
	swig \
	tclap \
	uw-imap \
	yajl \
	yasm \
"

# meta-openembedded/meta-oe/recipes-devtools
RDEPENDS:${PN}:append:x64 = "\
	concurrencykit \
	msr-tools \
"

# meta-openembedded/meta-oe/recipes-extended
RDEPENDS:${PN} += "\
	acpica \
	collectd \
	corosync \
	dash \
	ddrescue \
	dialog \
	efivar \
	flashrom \
	haveged \
	hexedit \
	hplip \
	indent \
	iotop \
	jansson \
	leveldb \
	libcec \
	libconfig \
	liblockfile \
	liblognorm \
	libmodbus \
	lockfile-progs \
	logcheck \
	logwatch \
	md5deep \
	nana \
	net-snmp \
	nicstat \
	p7zip \
	redis \
	rrdtool \
	rsyslog \
	smartmontools \
	can-utils \
	tcsh \
	tipcutils \
	tmux \
	zram \
"

# meta-openembedded/meta-oe/recipes-graphics
# meta-oe/recipes-graphics
RDEPENDS:${PN}:append:x64 = "\
	jasper \
	packagegroup-fonts-truetype \
	terminus-font \
	ttf-gentium \
	ttf-inconsolata \
	ttf-mplus \
	ttf-pt-sans \
	xorg-sgml-doctools \
"


# meta-openembedded/meta-oe/recipes-multimedia
RDEPENDS:${PN} += "\
	audiofile \
	jack \
	media-ctl \
	opus-tools \
	v4l-utils \
	wavpack \
	libwebp \
"
# vlc, mplayer requires libmad which is on a non-whitelisted license

# meta-openembedded/meta-oe/recipes-support
RDEPENDS:${PN} += "\
	bash-completion \
	ccid \
	ckermit \
	cpufrequtils \
	cryptsetup \
	daemonize \
	daemontools \
	dfu-util \
	eject \
	enca \
	espeak \
	fbset \
	fbset-modes \
	fftw \
	gd \
	glog \
	gperftools \
	gradm \
	gsl \
	gsoap \
	hddtemp \
	htop \
	imagemagick \
	libinih \
	iso-codes \
	joe \
	lcov \
	libcap-ng \
	libftdi \
	libraw1394 \
	libutempter \
	links \
	lmsensors \
	lockdev \
	log4c \
	log4cpp \
	lzip \
	lvm2 \
	mailcap \
	mbuffer \
	mg \
	mime-support \
	mariadb \
	multipath-tools \
	nano \
	nmon \
	onig \
	openct \
	opencv \
	openldap \
	opensc \
	p11-kit \
	p910nd \
	pam-passwdqc \
	pcsc-lite \
	picocom \
	pkcs11-helper \
	portaudio-v19 \
	postgresql \
	pps-tools \
	procmail \
	pv \
	read-edid \
	rng-tools \
	rsnapshot \
	sdparm \
	serial-forward \
	sg3-utils \
	sharutils \
	smem \
	soci \
	spitools \
	strongswan \
	system-config-keyboard \
	libtalloc \
	tree \
	usb-modeswitch \
	vim \
"

RDEPENDS:${PN}:append:x64 = "\
	edac-utils \
	lcms \
	mcelog \
	numactl \
	pidgin \
	poppler \
	xdelta3 \
	xmlstarlet \
	zile \
"

# meta-openembedded/meta-oe/recipes-kernel
RDEPENDS:${PN} += "\
	crash \
"

# meta-openembedded/meta-filesystems
RDEPENDS:${PN} += "\
	ntfs-3g-ntfsprogs \
	smbnetfs \
	sshfs-fuse \
	unionfs-fuse \
	fuse \
	physfs \
	aufs-util \
	xfsprogs \
"

# meta-openembedded/meta-networking
RDEPENDS:${PN} += "\
	inetutils \
	polarssl \
	ufw \
	vlan \
	vpnc \
	vsftpd \
	atftp \
	autofs \
	cyrus-sasl \
	iscsi-initiator-utils \
	lldpd \
	ncftp \
	opensaf \
	postfix \
	radvd \
	tftp-hpa \
	conntrack-tools \
	ebtables \
	net-snmp \
	openflow \
	openl2tp \
	pptp-linux \
	radiusclient-ng \
	rp-pppoe \
	xl2tpd \
	zeroconf \
	arptables \
	bridge-utils \
	dnsmasq \
	dovecot \
	drbd-utils \
	esmtp \
	fetchmail \
	fping \
	geoip \
	ifenslave \
	iftop \
	macchanger \
	memcached \
	mtr \
	ndisc6 \
	netcat \
	netcat-openbsd \
	netcf \
	ypbind-mt \
	yp-tools \
	nuttcp \
	pimd \
	proftpd \
	smcroute \
	sshpass \
	ssmping \
	strongswan \
	stunnel \
	tcpdump \
	tnftp \
	traceroute \
	tunctl \
	wireshark \
	ptpd \
	snort \
"

# meta-oe/recipes-devtools/python (most now in meta-oe/meta-python)
RDEPENDS:${PN} += "\
	python3-future \
	python3-gsocketpool \
	python3-requests \
	python3-pyzmq \
	python3-m2crypto \
	python3-cheetah \
	python3-configparser \
	python3-lxml \
	python3-matplotlib \
	python3-pyudev \
	python3-pyalsaaudio \
	python3-pyusb \
	python3-markupsafe \
	python3-jinja2 \
	python3-pexpect \
	python3-dateutil \
	python3-ldap \
	python3-gevent \
	python3-cython \
	python3-smbus \
	python3-psutil \
	python3-msgpack \
	python3-twisted \
	python3-pyserial \
	python3-slip-dbus \
	pyrtm \
	python3-sqlalchemy \
	python3-pyyaml \
	python3-decorator \
	python3-greenlet \
	python3-pytz \
	python3-zopeinterface \
	python3-pyparted \
	python3-gevent \
	python3-greenlet \
	python3-gsocketpool \
"
# python3-matplotlib: hang on do_install

# meta-openembedded/meta-webserver
RDEPENDS:${PN} += "\
	fcgi \
	spawn-fcgi \
	hiawatha \
	monkey \
	nginx \
	phpmyadmin \
	sthttpd \
"

# meta-nilrt/recipes-devtools
RDEPENDS:${PN} += " \
	debootstrap \
"

# meta-cloud-services/meta-openstack/recipes-devtools
RDEPENDS:${PN} += "\
	erlang \
	libconfig-general-perl \
	python3-amqp \
	python3-amqplib \
	python3-beautifulsoup4 \
	python3-boto \
	python3-cephclient \
	python3-cliff \
	python3-cmd2 \
	python3-colorama \
	python3-coverage \
	python3-croniter \
	python3-cssselect \
	python3-decorator \
	python3-django \
	python3-django-appconf \
	python3-django-compressor \
	python3-django-nose \
	python3-dnspython \
	python3-eventlet \
	python3-extras \
	python3-falcon \
	python3-fixtures \
	python3-flake8 \
	python3-greenlet \
	python3-html5lib \
	python3-httplib2 \
	python3-httpretty \
	python3-ipaddr \
	python3-iso8601 \
	python3-jinja2 \
	python3-jsonpatch \
	python3-jsonpath-rw \
	python3-jsonpointer \
	python3-jsonschema \
	python3-keyring \
	python3-kombu \
	python3-lesscpy \
	python3-lockfile \
	python3-logutils \
	python3-lxml \
	python3-mako \
	python3-markupsafe \
	python3-mccabe \
	python3-memcache \
	python3-memcached \
	python3-mimeparse \
	python3-mock \
	python3-msgpack \
	python3-netaddr \
	python3-netifaces \
	python3-networkx \
	python3-requests \
	python3-oauthlib \
	python3-pam \
	python3-paramiko \
	python3-passlib \
	python3-paste \
	python3-pastedeploy \
	python3-pbr \
	python3-pep8 \
	python3-ply \
	python3-posix-ipc \
	python3-prettytable \
	python3-py \
	python3-pyasn1 \
	python3-pyflakes \
	python3-pymongo \
	python3-pyparsing \
	python3-pyscss \
	python3-pytest \
	python3-pytz \
	python3-pyudev \
	python3-requests \
	python3-rfc3986 \
	python3-rtslib-fb \
	python3-setuptools-git \
	python3-simplegeneric \
	python3-singledispatch \
	python3-six \
	python3-sqlalchemy \
	python3-stevedore \
	python3-subunit \
	python3-sysv-ipc \
	python3-taskflow \
	python3-tempita \
	python3-termcolor \
	python3-testrepository \
	python3-testresources \
	python3-testscenarios \
	python3-testtools \
	python3-tooz \
	python3-tox \
	python3-virtualenv \
	python3-waitress \
	python3-warlock \
	python3-websockify \
	python3-webtest \
	python3-wsme \
	python3-xattr \
"

# meta-cloud-services/meta-openstack/recipes-devtools
RDEPENDS:${PN} += "\
	uwsgi \
"

# meta-nilrt/recipes-devtools
RDEPENDS:${PN} += "\
	libubox \
"

# meta-nilrt/recipes-connectivity
RDEPENDS:${PN} += "\
	relayd \
"

# meta-cloud-services/meta-openstack/recipes-support
RDEPENDS:${PN} += "\
	chef \
	hiera \
	mod-wsgi \
	nss-pam-ldapd \
	tgt \
"

RDEPENDS:${PN}:append:x64 = "\
	chef \
	puppet \
"

# meta-cloud-services/meta-openstack/recipes-extended
RDEPENDS:${PN} += "\
	qpid \
"

# meta-virtualization
RDEPENDS:${PN} += "\
	cgroup-lite \
	lxc \
	multipath-tools \
	openvswitch \
"

RDEPENDS:${PN}:append:x64 = "\
	ipxe \
"

# Shared libraries needed to run FireFox
RDEPENDS:${PN} += "\
	alsa-lib \
"

# meta-security
RDEPENDS:${PN} += "\
	clamav \
	ecryptfs-utils \
	libhtp \
	samhain-server \
"

# meta-nilrt/recipes-ni
RDEPENDS:${PN} += "\
	nirtcfg-tests \
	sysconfig-settings \
"

# meta-qt5
RDEPENDS:${PN} += "\
	qtvirtualkeyboard \
"
