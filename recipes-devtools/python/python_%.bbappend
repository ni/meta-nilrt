# we don't want python 2 to take precedence over the default python 3 because
# it breaks salt if it gets installed from the feeds alongside python 3
do_install_append () {
	rm -f ${D}${bindir}/python
}
