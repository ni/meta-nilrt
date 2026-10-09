ERROR_QA:append = " lto-input-without-gcc-lto"
IMAGE_QA_COMMANDS += "check_lto_inputs"
do_image_qa[depends] += "binutils-native:do_populate_sysroot"

python check_lto_inputs() {
    import os
    import stat
    import subprocess

    import oe.rootfs

    installed_packages = oe.rootfs.image_list_installed_packages(d)
    if "gcc-lto" in installed_packages:
        return

    readelf = bb.utils.which(d.getVar("PATH"), "readelf")
    if not readelf:
        bb.fatal("readelf is required to check the image for LTO inputs")

    rootfs = d.getVar("IMAGE_ROOTFS")
    lto_inputs = []
    for root, dirs, files in os.walk(rootfs):
        for filename in files:
            path = os.path.join(root, filename)
            if not stat.S_ISREG(os.lstat(path).st_mode):
                continue

            with open(path, "rb") as candidate:
                magic = candidate.read(8)
            if not (magic.startswith(b"\x7fELF") or magic in (b"!<arch>\n", b"!<thin>\n")):
                continue

            result = subprocess.run(
                [readelf, "-SW", path],
                stdout=subprocess.PIPE,
                stderr=subprocess.DEVNULL,
                text=True,
                errors="replace",
                check=False,
            )
            if ".gnu.lto_" in result.stdout:
                lto_inputs.append(os.path.relpath(path, rootfs))

    if lto_inputs:
        display_paths = ", ".join(lto_inputs[:20])
        if len(lto_inputs) > 20:
            display_paths += ", and %d more" % (len(lto_inputs) - 20)
        oe.qa.handle_error(
            "lto-input-without-gcc-lto",
            "%s contains GCC LTO inputs but gcc-lto is not installed: %s" %
            (d.getVar("PN"), display_paths),
            d,
        )
}
