require recipes-devtools/squashfs-tools/squashfs-tools_${PV}.bb

EXTRA_OEMAKE = "MAKEFLAGS= LZMA_SUPPORT=1 LZMA_DIR=../.. XZ_SUPPORT=1 EXTRA_LDFLAGS=-static"

UTILS_DIR = "${TMPDIR}/deploy/utils"

python do_strip () {
    import oe.package
    path = os.path.abspath(d.getVar('UTILS_DIR', True) + os.sep +
                           d.getVar("sbindir", True) + os.sep + "mksquashfs")
    oe.package.runstrip((path, 4, d.getVar("STRIP", True)))
}

do_install_append () {
	mkdir -p "${UTILS_DIR}/${sbindir}"
	install -m 0755 mksquashfs "${UTILS_DIR}/${sbindir}/"
}

addtask strip after do_install before do_package do_populate_sysroot
