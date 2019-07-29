FILESEXTRAPATHS_append := "${THISDIR}/files:"

# ${PN}-transconf
inherit transconf-hook
SRC_URI =+ "file://transconf-hooks/"
RDEPENDS_${PN}-transconf += "bash"
TRANSCONF_HOOKS_${PN} = "transconf-hooks/shadow"
