DESCRIPTION = "Linux kernel module versioning tools for NI drivers"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

S = "${WORKDIR}"

SRC_URI += "file://versioning_utils.sh \
"

RDEPENDS_${PN} = "\
	squashfs-tools \
	gcc \
	gcc-symlinks \
	make \
	binutils \
	libgcc-dev \
	ncurses \
"

FILES_${PN} = "/usr/local/natinst/tools/versioning_utils.sh \
"

# Skip QA Issue due to rdepends on libgcc-dev [dev-deps]
# This is intentional because module versioning requires libgcc-dev
INSANE_SKIP_${PN} += "dev-deps"

do_install () {
	install -d ${D}/usr/local/natinst/tools
	install -m 0755 ${S}/versioning_utils.sh ${D}/usr/local/natinst/tools/versioning_utils.sh
}
