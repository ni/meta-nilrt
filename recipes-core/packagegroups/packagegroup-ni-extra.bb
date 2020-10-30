SUMMARY = "Bag of goodies for NI Linux Realtime distribution"
LICENSE = "MIT"

inherit packagegroup

# These packages are only built for the nilrt-xfce fork of the older NILRT distro
# as opposed to nilrt-nxg which always enables x11 support. Also only for x64
# because NILRT ARM does not have a GUI.
RDEPENDS_${PN}_append_x64 = "\
	${@bb.utils.contains('DISTRO_FEATURES', 'x11', '\
		packagegroup-self-hosted \
		packagegroup-xfce-extended \
		packagegroup-ni-mono-extra \
		consolekit \
		geany \
		gimp \
		gnuplot \
		gnuradio \
		gtk+3 \
		iceauth \
		florence \
		fltk \
		libvncserver \
		libwmf \
		lxdm \
		mesa-demos \
		modemmanager \
		networkmanager \
		numlockx \
		openjdk-7-jre \
		openbox \
		onboard \
		polkit \
		sessreg \
		setxkbmap \
		sip \
		t1lib \
		toscoterm \
		tk \
		twm \
		udisks \
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

RDEPENDS_${PN} = "\
	packagegroup-core-sdk \
	packagegroup-core-standalone-sdk-target \
	packagegroup-core-basic \
	packagegroup-core-device-devel \
	packagegroup-core-buildessential \
	packagegroup-core-tools-debug \
	packagegroup-core-security \
	packagegroup-tpm2 \
	packagegroup-fonts-truetype \
"

# meta-nilrt
RDEPENDS_${PN} += "\
	packagegroup-ni-debug-kernel \
	packagegroup-ni-selinux \
"

RDEPENDS_${PN} += "\
	avahi \
	bind \
	cifs-utils \
	dhcp-server \
	dhcp-client \
	dhcp-omshell \
	libpcap \
	ofono \
	ppp \
	ppp-dialin \
	python3-salttesting \
	resolvconf \
	salt \
"

RDEPENDS_${PN} += "\
	psplash \
	sysfsutils \
"

# openembedded-core/meta/recipes-core
RDEPENDS_${PN} += "\
	libcgroup \
"

# openembedded-core/meta/recipes-bsp
RDEPENDS_${PN} += "\
	alsa-state \
	keymaps \
	lrzsz \
	setserial \
	usbutils \
"

# openembedded-core/meta/recipes-connectivity
RDEPENDS_${PN} += "\
	bluez5 \
"

# openembedded-core/meta/recipes-devtools
RDEPENDS_${PN} += "\
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
	ossp-uuid \
	patchelf \
	pax-utils \
	prelink \
	python3-distribute \
	python3-imaging \
	python3-docutils \
	python3-mako \
	python3-nose \
	python3-numpy \
	rpm \
	rsync \
	unfs3 \
	vala \
	valgrind \
	xmlto \
"

# openembedded-core/meta/recipes-extended
RDEPENDS_${PN} += "\
	at \
	augeas \
	bc \
	blktool \
	byacc \
	cups \
	foomatic-filters \
	ghostscript \
	iputils \
	libarchive \
	lighttpd \
	ltp \
	mailx \
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
RDEPENDS_${PN} += "\
	rt-tests \
"

# openembedded-core/meta/recipes-support
RDEPENDS_${PN} += "\
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
	sqlite \
"

# openembedded-core/meta/recipes-kernel
RDEPENDS_${PN} += "\
	blktrace \
	dkms \
	dtc \
	kexec-tools \
	kmod \
	linux-firmware \
	makedumpfile \
	oprofile \
	perf \
	powertop \
	sysprof \
	systemtap \
	trace-cmd \
"

# openembedded-gore/meta/recipes-multimedia
RDEPENDS_${PN} += "\
	alsa-tools \
	alsa-utils \
	alsa-utils-scripts \
	flac \
	pulseaudio \
	speex \
"

# meta-openembedded/meta-oe/recipes-benchmark
RDEPENDS_${PN} += "\
	bonnie++ \
	dbench \
	fio \
	iozone3 \
	iperf2 \
	iperf3 \
	libhugetlbfs \
	lmbench \
	memtester \
	nbench-byte \
	phoronix-test-suite \
	tiobench \
"


# meta-openembedded/meta-oe/recipes-bsp
RDEPENDS_${PN} += "\
	tbtadm \
"
# meta-openembedded/meta-oe/recipes-connectivity
RDEPENDS_${PN} += "\
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
	ti-wifi-utils \
	tiptop \
	umip \
	usbmuxd \
	zeromq \
	wireless-tools \
"

# meta-openembedded/meta-oe/recipes-core
RDEPENDS_${PN} += "\
	usleep \
"

# meta-openembedded/meta-oe/recipes-devtools
RDEPENDS_${PN} += "\
	bootchart \
	breakpad \
	cgdb \
	gdb \
	cppunit \
	ctags \
	cunit \
	dejagnu \
	i2c-tools \
	icon-slicer \
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
	python3-futures \
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
RDEPENDS_${PN}_append_x64 += "\
	concurrencykit \
	msr-tools \
"

# meta-openembedded/meta-oe/recipes-extended
RDEPENDS_${PN} += "\
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
RDEPENDS_${PN}_append_x64 += "\
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
RDEPENDS_${PN} += "\
	alsa-oss \
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
RDEPENDS_${PN} += "\
	bash-completion \
	ccid \
	ckermit \
	cpufrequtils \
	cryptsetup \
	daemonize \
	daemontools \
	dstat \
	dfu-util \
	eject \
	enca \
	engine-pkcs11 \
	espeak \
	eventlog \
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
	mysql-python \
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
	start-stop-daemon \
	strongswan \
	system-config-keyboard \
	libtalloc \
	tbb \
	tree \
	usb-modeswitch \
	vim \
"

RDEPENDS_${PN}_append_x64 += "\
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
RDEPENDS_${PN} += "\
	crash \
"

# meta-openembedded/meta-initramfs/recipes-devtools
RDEPENDS_${PN} += "\
	mkinitcpio \
"

# meta-openembedded/meta-filesystems
RDEPENDS_${PN} += "\
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
RDEPENDS_${PN} += "\
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
	netkit-ftp \
	netkit-rpc \
	netkit-rsh-client \
	netkit-rsh-server \
	netkit-rwho-server \
	netkit-tftp-client \
	netkit-tftp-server \
	netkit-telnet \
	net-snmp \
	openflow \
	openl2tp \
	pptp-linux \
	quagga \
	radiusclient-ng \
	rp-pppoe \
	xl2tpd \
	zeroconf \
	arptables \
	bridge-utils \
	dnsmasq \
	dnssec-conf \
	dovecot \
	drbd-utils \
	esmtp \
	fetchmail \
	fping \
	geoip \
	ifenslave \
	iftop \
	ipsec-tools \
	macchanger \
	memcached \
	mtr \
	ndisc6 \
	ntp \
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
RDEPENDS_${PN} += "\
	python3-future \
	python3-gsocketpool \
	python3-mprpc \
	python3-feedparser \
	python3-pyzmq \
	python3-m2crypto \
	python3-cheetah \
	python3-lxml \
	python3-numeric \
	python3-matplotlib \
	python3-pyudev \
	python3-pyalsaaudio \
	python3-pyusb \
	python3-pexpect \
	python3-webdav \
	python3-ldap \
	python3-epydoc \
	python3-gevent \
	python3-cython \
	python3-smbus \
	python3-snakefood \
	python3-vobject \
	python3-twisted \
	python3-pyserial \
	python3-slip-dbus \
	python3-gdata \
	pyrtm \
	python3-sqlalchemy \
	python3-decorator \
	python3-greenlet \
	python3-pytz \
	python3-zopeinterface \
	python3-pyparted \
	python3-pyconnman \
	python3-gevent \
	python3-greenlet \
	python3-gsocketpool \
"
# python3-matplotlib: hang on do_install

# meta-openembedded/meta-webserver
RDEPENDS_${PN} += "\
	apache2 \
	cherokee \
	fcgi \
	spawn-fcgi \
	hiawatha \
	monkey \
	nginx \
	nostromo \
	phpmyadmin \
	sthttpd \
"

# meta-nilrt/recipes-devtools
RDEPENDS_${PN} += " \
	debootstrap \
"

# meta-cloud-services/meta-openstack/recipes-devtools
RDEPENDS_${PN} += "\
	erlang \
	libconfig-general-perl \
	python3-amqp \
	python3-amqplib \
	python3-anyjson \
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
	python3-futures \
	python3-greenlet \
	python3-html5lib \
	python3-httplib2 \
	python3-httpretty \
	python3-ipaddr \
	python3-iso8601 \
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
	python3-mccabe \
	python3-memcache \
	python3-memcached \
	python3-mimeparse \
	python3-mock \
	python3-mox \
	python3-mox3 \
	python3-netaddr \
	python3-netifaces \
	python3-networkx \
	python3-oauth2 \
	python3-oauthlib \
	python3-pam \
	python3-paramiko \
	python3-passlib \
	python3-paste \
	python3-pastedeploy \
	python3-pbr \
	python3-pep8 \
	python3-posix-ipc \
	python3-prettytable \
	python3-psycopg2 \
	python3-py \
	python3-pyflakes \
	python3-pymongo \
	python3-pyparsing \
	python3-pyscss \
	python3-pysnmp \
	python3-pysqlite \
	python3-pytest \
	python3-pytz \
	python3-pyudev \
	python3-rfc3986 \
	python3-rtslib-fb \
	python3-setuptools-git \
	python3-simplegeneric \
	python3-singledispatch \
	python3-sqlalchemy \
	python3-stevedore \
	python3-subunit \
	python3-suds \
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
	python3-wsgiref \
	python3-wsme \
	python3-xattr \
"

# meta-cloud-services/meta-openstack/recipes-devtools
RDEPENDS_${PN} += "\
	uwsgi \
"

# meta-nilrt/recipes-devtools
RDEPENDS_${PN} += "\
	libubox \
"

# meta-nilrt/recipes-connectivity
RDEPENDS_${PN} += "\
	relayd \
"

# meta-cloud-services/meta-openstack/recipes-support
RDEPENDS_${PN} += "\
	chef \
	hiera \
	mod-wsgi \
	nss-pam-ldapd \
	tgt \
"

RDEPENDS_${PN}_append_x64 += "\
	chef \
	puppet \
"

# meta-cloud-services/meta-openstack/recipes-extended
RDEPENDS_${PN} += "\
	qpid \
"

# meta-measured
RDEPENDS_${PN} += "\
	trousers \
"

# meta-virtualization
RDEPENDS_${PN} += "\
	cgroup-lite \
	docker \
	lxc \
	multipath-tools \
	openvswitch \
"

RDEPENDS_${PN}_append_x64 += "\
	ipxe \
"

# Shared libraries needed to run FireFox
RDEPENDS_${PN} += "\
    alsa-lib \
"

# meta-security
RDEPENDS_${PN} += "\
	clamav \
	libhtp \
	samhain-server \
"

# meta-nilrt/recipes-ni
RDEPENDS_${PN} += "\
	nirtcfg-tests \
"

# meta-qt5
RDEPENDS_${PN} += "\
	qtvirtualkeyboard \
"

# meta-ivi
RDEPENDS_${PN} += "\
	common-api-c++ \
	common-api-c++-dbus \
	common-api-c++-someip \
	dlt-daemon \
	ecryptfs-utils \
	keyutils \
	liboil \
	persistence-common-object \
	vsomeip \
"
