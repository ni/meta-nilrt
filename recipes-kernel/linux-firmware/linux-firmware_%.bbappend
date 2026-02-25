ALLOW_EMPTY:${PN}-i915 = "1"
PACKAGES_DYNAMIC = "${PN}-i915-.*"

python populate_packages:prepend () {
    def i915_license_hook(f, pkg, file_regex, output_pattern, modulename):
        d.setVar('LICENSE:%s' % pkg, 'Firmware-i915')

    i915_fwdir = d.expand('${nonarch_base_libdir}/firmware/i915')

    # i915 firmware file names have the following structure:
    #   <codename>_<subsystem>_<version>.bin
    # create split packages based on Intel CPU/GPU codename and subsystem
    pkgs = do_split_packages(d,
                             i915_fwdir,
                             r'^([^_]+_[^_]+).*\.bin$',
                             output_pattern='${PN}-i915-%s',
                             description='Intel i915:%s firmware',
                             extra_depends='${PN}-i915-license',
                             prepend=True,
                             allow_links=True,
                             hook=i915_license_hook
                             )
    if pkgs:
        d.appendVar('RDEPENDS:%s-i915' % d.getVar('PN'), ' ' + ' '.join(pkgs))
}
