HOMEPAGE = "http://saltstack.com/"
SECTION = "admin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c996f5a78d858a52c894fa3f4bec68c1"
DEPENDS = "\
    python3-msgpack \
    python3-pyyaml \
    python3-jinja2 \
    python3-markupsafe \
"

DEPENDS += "\
    python3-distro-native \
"

PACKAGECONFIG = "tcp zeromq"
PACKAGECONFIG[tcp] = ",,python3-pycryptodome"
PACKAGECONFIG[zeromq] = ",,python3-pycryptodome python3-pyzmq"

SRC_URI = "\
    git://github.com/ni/salt.git;protocol=https;branch=ni/skyline-25.0/3000.2 \
    file://minion \
    file://salt-minion \
    file://salt-common.bash_completion \
    file://salt-common.logrotate \
    file://salt-api \
    file://salt-master \
    file://master \
    file://salt-syndic \
    file://cloud \
    file://roster \
    file://run-ptest \
"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit setuptools3_legacy update-rc.d ptest

# Avoid a QA Warning triggered by the test package including a file
# with a .a extension
INSANE_SKIP:${PN}-tests += "staticdev"

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

INITSCRIPT_PARAMS:${PN}-minion = "defaults 93 7"

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

ALLOW_EMPTY:${PN} = "1"
FILES:${PN} = ""

INITSCRIPT_PACKAGES = "${PN}-minion ${PN}-api ${PN}-master ${PN}-syndic"

DESCRIPTION_COMMON = "salt is a powerful remote execution manager that can be used to administer servers in a\
 fast and efficient way. It allows commands to be executed across large groups of servers. This means systems\
 can be easily managed, but data can also be easily gathered. Quick introspection into running systems becomes\
 a reality. Remote execution is usually used to set up a certain state on a remote system. Salt addresses this\
 problem as well, the salt state system uses salt state files to define the state a server needs to be in. \
Between the remote execution system, and state management Salt addresses the backbone of cloud and data center\
 management."

SUMMARY:${PN}-minion = "client package for salt, the distributed remote execution system"
DESCRIPTION:${PN}-minion = "${DESCRIPTION_COMMON} This particular package provides the worker agent for salt."
RDEPENDS:${PN}-minion = "${PN}-common (= ${EXTENDPKGV}) python3-core python3-msgpack python3-distro"
RDEPENDS:${PN}-minion += "${@bb.utils.contains('PACKAGECONFIG', 'zeromq', 'python3-pycryptodome python3-pyzmq (>= 13.1.0)', '',d)}"
RDEPENDS:${PN}-minion += "${@bb.utils.contains('PACKAGECONFIG', 'tcp', 'python3-pycryptodome', '',d)}"
RRECOMMENDS:${PN}-minion:append:x64 = "dmidecode"
RSUGGESTS:${PN}-minion = "python3-augeas"
CONFFILES:${PN}-minion = "${sysconfdir}/${PN}/minion ${sysconfdir}/init.d/${PN}-minion"
FILES:${PN}-minion = "${bindir}/${PN}-minion ${sysconfdir}/${PN}/minion.d/ ${CONFFILES:${PN}-minion} ${bindir}/${PN}-proxy"
INITSCRIPT_NAME:${PN}-minion = "${PN}-minion"
INITSCRIPT_PARAMS:${PN}-minion = "defaults"

SUMMARY:${PN}-common = "shared libraries that salt requires for all packages"
DESCRIPTION:${PN}-common ="${DESCRIPTION_COMMON} This particular package provides shared libraries that \
salt-master, salt-minion, and salt-syndic require to function."
RDEPENDS:${PN}-common = "\
    python3-backports-ssl-match-hostname \
    python3-charset-normalizer \
    python3-core \
    python3-dateutil \
    python3-fcntl \
    python3-jinja2 \
    python3-pyyaml \
    python3-requests (>= 1.0.0) \
    python3-singledispatch (>= 3.4.0.3) \
    python3-tornado (>= 4.2.1) \
"
RRECOMMENDS:${PN}-common = "lsb-release"
RSUGGESTS:${PN}-common = "python3-mako python3-git"
RCONFLICTS:${PN}-common = "python3-mako (< 0.7.0)"
CONFFILES:${PN}-common="${sysconfdir}/logrotate.d/${PN}-common"
FILES:${PN}-common = "${bindir}/${PN}-call ${PYTHON_SITEPACKAGES_DIR} ${CONFFILES:${PN}-common}"

SUMMARY:${PN}-ssh = "remote manager to administer servers via salt"
DESCRIPTION:${PN}-ssh = "${DESCRIPTION_COMMON} This particular package provides the salt ssh controller. It \
is able to run salt modules and states on remote hosts via ssh. No minion or other salt specific software needs\
 to be installed on the remote host."
RDEPENDS:${PN}-ssh = "${PN}-common (= ${EXTENDPKGV}) python3-core python3-msgpack"
CONFFILES:${PN}-ssh="${sysconfdir}/${PN}/roster"
FILES:${PN}-ssh = "${bindir}/${PN}-ssh ${CONFFILES:${PN}-ssh}"

SUMMARY:${PN}-api = "generic, modular network access system"
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

SUMMARY:${PN}-master = "remote manager to administer servers via salt"
DESCRIPTION:${PN}-master ="${DESCRIPTION_COMMON} This particular package provides the salt controller."
RDEPENDS:${PN}-master = "${PN}-common (= ${EXTENDPKGV}) python3-core python3-msgpack"
RDEPENDS:${PN}-master += "${@bb.utils.contains('PACKAGECONFIG', 'zeromq', 'python3-pycryptodome python3-pyzmq (>= 13.1.0)', '',d)}"
RDEPENDS:${PN}-master += "${@bb.utils.contains('PACKAGECONFIG', 'tcp', 'python3-pycryptodome', '',d)}"
CONFFILES:${PN}-master="${sysconfdir}/init.d/${PN}-master  ${sysconfdir}/${PN}/master"
RSUGGESTS:${PN}-master = "python3-git"
FILES:${PN}-master = "${bindir}/${PN} ${bindir}/${PN}-cp ${bindir}/${PN}-key ${bindir}/${PN}-master ${bindir}/${PN}-run ${bindir}/${PN}-unity ${bindir}/spm ${CONFFILES:${PN}-master}"
INITSCRIPT_NAME:${PN}-master = "${PN}-master"
INITSCRIPT_PARAMS:${PN}-master = "defaults"

SUMMARY:${PN}-syndic = "master-of-masters for salt, the distributed remote execution system"
DESCRIPTION:${PN}-syndic = "${DESCRIPTION_COMMON} This particular package provides the master of masters for \
salt; it enables the management of multiple masters at a time."
RDEPENDS:${PN}-syndic = "${PN}-master (= ${EXTENDPKGV}) python3-core"
CONFFILES:${PN}-syndic="${sysconfdir}/init.d/${PN}-syndic"
FILES:${PN}-syndic = "${bindir}/${PN}-syndic ${CONFFILES:${PN}-syndic}"
INITSCRIPT_NAME:${PN}-syndic = "${PN}-syndic"
INITSCRIPT_PARAMS:${PN}-syndic = "defaults"

SUMMARY:${PN}-cloud = "public cloud VM management system"
DESCRIPTION:${PN}-cloud = "provision virtual machines on various public clouds via a cleanly controlled profile and mapping system."
RDEPENDS:${PN}-cloud = "${PN}-common (= ${EXTENDPKGV}) python3-core"
RSUGGESTS:${PN}-cloud = "python3-netaddr python3-botocore"
CONFFILES:${PN}-cloud = "${sysconfdir}/${PN}/cloud"
FILES:${PN}-cloud = "${bindir}/${PN}-cloud ${sysconfdir}/${PN}/cloud.conf.d/ ${sysconfdir}/${PN}/cloud.profiles.d/ ${sysconfdir}/${PN}/cloud.providers.d/ ${CONFFILES:${PN}-cloud}"

SUMMARY:${PN}-tests = "salt stack test suite"
DESCRIPTION:${PN}-tests ="${DESCRIPTION_COMMON} This particular package provides the salt unit test suite."
RDEPENDS:${PN}-tests = "${PN}-common python3-pyzmq python3-six python3-tests python3-image bash"
FILES:${PN}-tests = "${PYTHON_SITEPACKAGES_DIR}/salt-tests/tests/"

RDEPENDS:${PN}-ptest += "salt-tests python3-distro python3-mock"

FILES:${PN}-bash-completion = "${sysconfdir}/bash_completion.d/${PN}-common"
