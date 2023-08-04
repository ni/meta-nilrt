FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

DEPENDS += "update-rc.d-native"
DEPENDS:remove = "polkit"
REQUIRED_DISTRO_FEATURES:remove = "polkit"

SRC_URI += "\
   file://bolt \
   file://boltctl \
   file://90-bolt.rules \
   file://org.freedesktop.bolt.conf \
   file://0001-daemon-support-running-without-PolicyKit.patch \
   file://0002-exported-authorize-all-methods-by-default.patch \
"

FILES:${PN} += "${sysconfdir}/init.d/bolt"

EXTRA_OEMESON = "-Dpolkit-required=false"

do_install:append() {
   install -d ${D}${sysconfdir}/init.d
   install -m 0755 ${WORKDIR}/bolt ${D}${sysconfdir}/init.d/bolt

   # Move boltctl to a directory that's not in PATH and install a wrapper
   # in its place
   mv ${D}${bindir}/boltctl ${D}${libexecdir}/
   install -m 0755 ${WORKDIR}/boltctl ${D}${bindir}/

   # Overwrite udev rules file so it works on systems without systemd
   install -m 0644 ${WORKDIR}/90-bolt.rules ${D}${base_libdir}/udev/rules.d/

   # Overwrite dbus config file to allow only root to run privileged commands
   install -m 0644 ${WORKDIR}/org.freedesktop.bolt.conf ${D}${datadir}/dbus-1/system.d/

   update-rc.d -r ${D} bolt           start 3 2 3 5 .
}
