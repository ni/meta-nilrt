# TODO: remove once OE-core decides to symlink python3 as /usr/bin/python instead of 2.
pkg_postinst_${PN}-core () {
	if [ -f $D${bindir}/python3 ]; then
		ln -sf ${bindir}/python3 $D${bindir}/python
	fi
}
