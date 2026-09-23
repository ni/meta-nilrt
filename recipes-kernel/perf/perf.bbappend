
# Keep perf's output path short on ARM.  The output files still reside in ${B}
# through the symlink, while recursive perf make commands use the short path.
#
# tools/perf/Makefile (the outer wrapper make picks up via -C) canonicalizes
# O= with "readlink -f" before handing off to Makefile.perf, which undoes the
# symlink and restores the long ${B} path.  Drop that canonicalization so the
# short path actually survives into the recursive build.
do_compile:prepend:xilinx-zynq() {
	rm -f /tmp/perf-output
	ln -s ${B} /tmp/perf-output
	sed -i 's/readlink -f \$(O) || echo \$(O)/echo \$(O)/' ${S}/tools/perf/Makefile
	if grep -q 'readlink -f \$(O) || echo \$(O)' ${S}/tools/perf/Makefile; then
		bbwarn "perf.bbappend: expected line 'readlink -f \$(O) || echo \$(O)' still present in tools/perf/Makefile after sed -- perf's Makefile may have changed upstream; the xilinx-zynq E2BIG workaround in perf.bbappend may need updating."
	fi
}

EXTRA_OEMAKE:remove:xilinx-zynq = "O=${B}"
EXTRA_OEMAKE:append:xilinx-zynq = " O=/tmp/perf-output"

