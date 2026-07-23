FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Ship the NI-maintained AIDE policy as /etc/aide.conf, replacing the upstream
# sample. FILESEXTRAPATHS:prepend already makes the base recipe's own
# "file://aide.conf" resolve to our copy, but we list it here (and install it
# below) so the override is explicit and cannot silently regress if a future
# upstream bump drops that SRC_URI entry. The monitored/excluded rules are inline
# in our aide.conf, so blank the class-appended include/skip lists to avoid
# duplicate entries.
SRC_URI += "file://aide.conf"
AIDE_INCLUDE_DIRS = ""
AIDE_SKIP_DIRS = ""

# First-boot baseline initialization (sysvinit).
SRC_URI += "file://aide-init"
inherit update-rc.d
INITSCRIPT_NAME = "aide-init"
INITSCRIPT_PARAMS = "defaults 99"

# Scheduled integrity checks via cron -> syslog.
SRC_URI += "file://aide-check file://aide-check.cron file://aide.logrotate"

# Runtime deps for the shipped scripts:
#  - cronie schedules aide-check
#  - logrotate rotates the persistent report log (ships /etc/logrotate.d/aide)
#  - coreutils provides sha256sum and nice, which the NILRT BusyBox config
#    disables (CONFIG_SHA256SUM/CONFIG_NICE). Both utilities live in the main
#    coreutils package (they are NOT among NI's per-binary coreutils-* splits),
#    so the full package is the correct dependency. coreutils is already part of
#    the runmode base image, so this makes the dependency explicit and does not
#    add image footprint.
RDEPENDS:${PN} += "cronie coreutils logrotate"

# Database lives outside every monitored directory (see aide.conf); install the
# NI policy as /etc/aide.conf, create the database dir, and install the
# first-boot init script + scheduled-check wrapper, cron job and logrotate rule.
do_install:append () {
	install -d -m 0700 ${D}${localstatedir}/lib/aide
	install -d ${D}${sysconfdir}
	install -m 0644 ${UNPACKDIR}/aide.conf ${D}${sysconfdir}/aide.conf
	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${UNPACKDIR}/aide-init ${D}${sysconfdir}/init.d/aide-init
	install -d ${D}${sbindir}
	install -m 0755 ${UNPACKDIR}/aide-check ${D}${sbindir}/aide-check
	install -d ${D}${sysconfdir}/cron.d
	install -m 0644 ${UNPACKDIR}/aide-check.cron ${D}${sysconfdir}/cron.d/aide-check
	install -d ${D}${sysconfdir}/logrotate.d
	install -m 0644 ${UNPACKDIR}/aide.logrotate ${D}${sysconfdir}/logrotate.d/aide
}

FILES:${PN} += "\
	${sysconfdir}/aide.conf \
	${localstatedir}/lib/aide \
	${sysconfdir}/init.d/aide-init \
	${sbindir}/aide-check \
	${sysconfdir}/cron.d/aide-check \
	${sysconfdir}/logrotate.d/aide \
"
