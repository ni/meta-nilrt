DESCRIPTION = "Linux kernel module versioning tools for NI drivers"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

S = "${WORKDIR}"

SRC_URI += "file://versioning_utils.sh \
"

RDEPENDS_${PN} = "bash"

FILES_${PN} = "/usr/local/natinst/tools/versioning_utils.sh \
"

# Skip QA Issue due to rdepends on libgcc-dev [dev-deps]
# This is intentional because module versioning requires libgcc-dev
INSANE_SKIP_${PN} += "dev-deps"

do_install () {
	install -d ${D}/usr/local/natinst/tools
	install -m 0755 ${S}/versioning_utils.sh ${D}/usr/local/natinst/tools/versioning_utils.sh
}
