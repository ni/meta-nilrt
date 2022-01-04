# Creating /usr/bin/python symlink to point to python3 here because OE-core doesn't.
pkg_postinst_${PN}-core () {
	if [ -f $D${bindir}/python3 ]; then
		ln -sf ${bindir}/python3 $D${bindir}/python
	fi
}
