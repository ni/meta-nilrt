SUMMARY = "Salt - remote execution manager"
DESCRIPTION = "\
Salt is a powerful remote execution manager that can be used to administer \
servers in a fast and efficient way. It allows commands to be executed across \
large groups of servers. This means systems can be easily managed, but data \
can also be easily gathered. Quick introspection into running systems becomes \
a reality. Remote execution is usually used to set up a certain state on a \
remote system. Salt addresses this problem as well, the salt state system uses \
salt state files to define the state a server needs to be in.  Between the \
remote execution system, and state management Salt addresses the backbone of \
cloud and data center management."
HOMEPAGE = "http://saltstack.com/"
SECTION = "admin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c996f5a78d858a52c894fa3f4bec68c1"

DEPENDS = "\
	python3-distro-native \
	python3-jinja2 \
	python3-markupsafe \
	python3-msgpack \
	python3-pyyaml \
"

SRC_URI = "\
	git://github.com/ni/salt.git;protocol=https;branch=ni/master/3000.2 \
	file://cloud \
	file://master \
	file://minion \
	file://roster \
	file://run-ptest \
	file://salt-api \
	file://salt-common.bash_completion \
	file://salt-common.logrotate \
	file://salt-master \
	file://salt-minion \
	file://salt-syndic \
"

SRCREV = "${AUTOREV}"


S = "${WORKDIR}/git"

inherit setuptools3 ptest

inherit update-rc.d
INITSCRIPT_PACKAGES = "${PN}-minion ${PN}-api ${PN}-master ${PN}-syndic"

PACKAGECONFIG = "tcp zeromq"
PACKAGECONFIG[tcp] = ",,python3-pycrypto"
PACKAGECONFIG[zeromq] = ",,python3-pycrypto python3-pyzmq"

# Avoid a QA Warning triggered by the test package including a file
# with a .a extension
INSANE_SKIP:${PN}-tests += "staticdev"


do_install:append() {
	install -d ${D}${sysconfdir}/bash_completion.d/
	install -m 0644 ${WORKDIR}/salt-common.bash_completion ${D}${sysconfdir}/bash_completion.d/${PN}-common

	install -d ${D}${sysconfdir}/logrotate.d/
	install -m 0644 ${WORKDIR}/salt-common.logrotate ${D}${sysconfdir}/logrotate.d/${PN}-common

	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${WORKDIR}/salt-minion ${D}${sysconfdir}/init.d/${PN}-minion
	install -m 0755 ${WORKDIR}/salt-api ${D}${sysconfdir}/init.d/${PN}-api
	install -m 0755 ${WORKDIR}/salt-master ${D}${sysconfdir}/init.d/${PN}-master
	install -m 0755 ${WORKDIR}/salt-syndic ${D}${sysconfdir}/init.d/${PN}-syndic

	install -d ${D}${sysconfdir}/${PN}/
	install -m 0644 ${WORKDIR}/minion ${D}${sysconfdir}/${PN}/minion
	install -m 0644 ${WORKDIR}/master ${D}${sysconfdir}/${PN}/master
	install -m 0644 ${WORKDIR}/cloud ${D}${sysconfdir}/${PN}/cloud
	install -m 0644 ${WORKDIR}/roster ${D}${sysconfdir}/${PN}/roster

	install -d ${D}${sysconfdir}/${PN}/cloud.conf.d ${D}${sysconfdir}/${PN}/cloud.profiles.d ${D}${sysconfdir}/${PN}/cloud.providers.d

	install -d ${D}${PYTHON_SITEPACKAGES_DIR}/${PN}-tests/
	cp -r ${S}/tests/ ${D}${PYTHON_SITEPACKAGES_DIR}/${PN}-tests/

	# The Salt SysV scripts require that the process name of the salt
	# components have the form "salt-<component>".
	# The current python shebangs on the salt components scripts spwans
	# processes that are generically named python. Changed shebang so
	# that process names will be identifiable by the init scripts.
	sed -i 's|#!/usr/bin/env python3|#!/usr/bin/python3|' ${D}${bindir}/salt-*
}

# Note ${PN}-tests must be before ${PN}-common in the PACKAGES variable
# in order for ${PN}-tests to own the correct FILES.
PACKAGES += "\
	${PN}-tests \
	${PN}-api \
	${PN}-cloud \
	${PN}-common \
	${PN}-master \
	${PN}-minion \
	${PN}-ssh \
	${PN}-syndic \
	${PN}-bash-completion \
"

ALLOW_EMPTY:${PN} = "1"
FILES:${PN} = ""


## SUBPACKAGES #
# salt-api
SUMMARY:${PN}-api = "${SUMMARY}; generic, modular network access system"
DESCRIPTION:${PN}-api = "a modular interface on top of Salt that can provide a variety of entry points into a \
running Salt system. It can start and manage multiple interfaces allowing a REST API to coexist with XMLRPC or \
even a Websocket API. The Salt API system is used to expose the fundamental aspects of Salt control to external\
 sources. salt-api acts as the bridge between Salt itself and REST, Websockets, etc. Documentation is available\
 on Read the Docs: http://salt-api.readthedocs.org/"
RDEPENDS:${PN}-api = "${PN}-master python3-core"
RSUGGESTS:${PN}-api = "python3-cherrypy"
CONFFILES:${PN}-api = "${sysconfdir}/init.d/${PN}-api"
FILES:${PN}-api = "${bindir}/${PN}-api ${CONFFILES:${PN}-api}"
INITSCRIPT_NAME:${PN}-api = "${PN}-api"
INITSCRIPT_PARAMS:${PN}-api = "defaults"

# salt-bash-completion
FILES:${PN}-bash-completion = "${sysconfdir}/bash_completion.d/${PN}-common"

# salt-cloud
SUMMARY:${PN}-cloud = "${SUMMARY}; public cloud VM management system"
DESCRIPTION:${PN}-cloud = "${DESCRIPTION}. Provision virtual machines on various public clouds via a cleanly controlled profile and mapping system."
RDEPENDS:${PN}-cloud = "${PN}-common (= ${EXTENDPKGV}) python3-core"
RSUGGESTS:${PN}-cloud = "python3-netaddr python3-botocore"
CONFFILES:${PN}-cloud = "${sysconfdir}/${PN}/cloud"
FILES:${PN}-cloud = "\
	${bindir}/${PN}-cloud \
	${sysconfdir}/${PN}/cloud.conf.d/ \
	${sysconfdir}/${PN}/cloud.profiles.d/ \
	${sysconfdir}/${PN}/cloud.providers.d/ \
	${CONFFILES:${PN}-cloud} \
"

# salt-common
SUMMARY:${PN}-common = "${SUMMARY}; shared libraries that salt requires for all packages"
DESCRIPTION:${PN}-common ="${DESCRIPTION} This particular package provides shared libraries that \
salt-master, salt-minion, and salt-syndic require to function."
RDEPENDS:${PN}-common = " \
	python3-configparser \
	python3-core \
	python3-dateutil \
	python3-dateutil \
	python3-difflib \
	python3-distutils \
	python3-fcntl \
	python3-jinja2 \
	python3-misc \
	python3-multiprocessing \
	python3-profile \
	python3-pyiface \
	python3-pyyaml \
	python3-requests (>= 1.0.0) \
	python3-resource \
	python3-terminal \
	python3-tornado (>= 4.2.1) \
	python3-unixadmin \
	python3-xmlrpc \
"
RRECOMMENDS:${PN}-common = "lsb-release"
RSUGGESTS:${PN}-common = "python3-mako python3-git"
RCONFLICTS:${PN}-common = "python3-mako (< 0.7.0)"
CONFFILES:${PN}-common="${sysconfdir}/logrotate.d/${PN}-common"
FILES:${PN}-common = "${bindir}/${PN}-call ${PYTHON_SITEPACKAGES_DIR} ${CONFFILES:${PN}-common}"

# salt-master
SUMMARY:${PN}-master = "${SUMMARY}; remote manager to administer servers via salt"
DESCRIPTION:${PN}-master ="${DESCRIPTION} This particular package provides the salt controller."
RDEPENDS:${PN}-master = "\
	${@bb.utils.contains('PACKAGECONFIG', 'tcp', 'python3-pycrypto', '',d)} \
	${@bb.utils.contains('PACKAGECONFIG', 'zeromq', 'python3-pycrypto python3-pyzmq (>= 13.1.0)', '',d)} \
	${PN}-common (= ${EXTENDPKGV}) \
	python3-core \
	python3-msgpack \
"
CONFFILES:${PN}-master="${sysconfdir}/init.d/${PN}-master  ${sysconfdir}/${PN}/master"
RSUGGESTS:${PN}-master = "python3-git"
FILES:${PN}-master = "${bindir}/${PN} ${bindir}/${PN}-cp ${bindir}/${PN}-key ${bindir}/${PN}-master ${bindir}/${PN}-run ${bindir}/${PN}-unity ${bindir}/spm ${CONFFILES:${PN}-master}"
INITSCRIPT_NAME:${PN}-master = "${PN}-master"
INITSCRIPT_PARAMS:${PN}-master = "defaults"

# salt-minion
SUMMARY:${PN}-minion = "${SUMMARY}; client package for salt, the distributed remote execution system"
DESCRIPTION:${PN}-minion = "${DESCRIPTION} This particular package provides the worker agent for salt."
RDEPENDS:${PN}-minion = "\
	${@bb.utils.contains('PACKAGECONFIG', 'zeromq', 'python3-pycrypto python3-pyzmq (>= 13.1.0)', '',d)} \
	${PN}-common (= ${EXTENDPKGV}) \
	python3-aiodns \
	python3-aiohttp \
	python3-avahi \
	python3-core \
	python3-distro \
	python3-mmap \
	python3-msgpack \
	python3-pika \
	python3-psutil \
	python3-pyinotify \
	python3-pyroute2 \
"
RRECOMMENDS:${PN}-minion:append:x64 = "dmidecode"
RSUGGESTS:${PN}-minion = "python3-augeas"
CONFFILES:${PN}-minion = "${sysconfdir}/${PN}/minion ${sysconfdir}/init.d/${PN}-minion"
FILES:${PN}-minion = "${bindir}/${PN}-minion ${sysconfdir}/${PN}/minion.d/ ${CONFFILES:${PN}-minion} ${bindir}/${PN}-proxy"
INITSCRIPT_NAME:${PN}-minion = "${PN}-minion"
INITSCRIPT_PARAMS:${PN}-minion = "defaults 93 7"

# salt-ptest
RDEPENDS:${PN}-ptest += "salt-tests python3-distro python3-mock"

# salt-ssh
SUMMARY:${PN}-ssh = "${SUMMARY}; remote manager to administer servers via salt"
DESCRIPTION:${PN}-ssh = "${DESCRIPTION} This particular package provides the salt ssh controller. It \
is able to run salt modules and states on remote hosts via ssh. No minion or other salt specific software needs\
 to be installed on the remote host."
RDEPENDS:${PN}-ssh = "${PN}-common (= ${EXTENDPKGV}) python3-core python3-msgpack"
CONFFILES:${PN}-ssh="${sysconfdir}/${PN}/roster"
FILES:${PN}-ssh = "${bindir}/${PN}-ssh ${CONFFILES:${PN}-ssh}"

# salt-syndic
SUMMARY:${PN}-syndic = "${SUMMARY}; master-of-masters for salt"
DESCRIPTION:${PN}-syndic = "${DESCRIPTION} This particular package provides the master of masters for \
salt; it enables the management of multiple masters at a time."
RDEPENDS:${PN}-syndic = "${PN}-master (= ${EXTENDPKGV}) python3-core"
CONFFILES:${PN}-syndic="${sysconfdir}/init.d/${PN}-syndic"
FILES:${PN}-syndic = "${bindir}/${PN}-syndic ${CONFFILES:${PN}-syndic}"
INITSCRIPT_NAME:${PN}-syndic = "${PN}-syndic"
INITSCRIPT_PARAMS:${PN}-syndic = "defaults"

# salt-tests
SUMMARY:${PN}-tests = "${SUMMARY}; test suite"
DESCRIPTION:${PN}-tests ="${DESCRIPTION} This particular package provides the salt unit test suite."
RDEPENDS:${PN}-tests = "${PN}-common python3-pytest-salt python3-pyzmq python3-six python3-tests python3-image bash"
FILES:${PN}-tests = "${PYTHON_SITEPACKAGES_DIR}/salt-tests/tests/"

# salt-transconf
inherit transconf-hook
SRC_URI =+ "file://transconf-hooks/"
RDEPENDS:${PN}-transconf += "bash"
TRANSCONF_HOOKS:${PN} = "transconf-hooks/salt"
