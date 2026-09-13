SUMMARY = "Seed a 'root' user into the NIAuth database"
DESCRIPTION = "First-boot init script that adds a 'root' NIAuth account to the \
administrators group so the NI System Web Server (WebDAV/SIWS) can authenticate \
web-service calls as 'root'. NIAuth keys its Digest/SRP credentials on the user \
name, so a distinct 'root' record is required in addition to the built-in \
'admin' user."
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
LICENSE = "MIT"
SECTION = "base"

SRC_URI = "\
        file://ni-niauth-root-user \
"

S = "${UNPACKDIR}"

inherit update-rc.d

INITSCRIPT_NAME = "ni-niauth-root-user"
# Start after the niauth daemon (init.d/niauth is S05) in the multi-user runlevels.
INITSCRIPT_PARAMS = "start 99 2 3 4 5 ."

do_install () {
        install -d ${D}${sysconfdir}/init.d/
        install -m 0700 ${S}/ni-niauth-root-user ${D}${sysconfdir}/init.d/
}
