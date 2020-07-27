# we want to distinguish only between nilrt and nilrt-nxg distros so we need
# nilrt-xfce to be the same as nilrt
do_install_append() {
	sed -i 's/DISTRIB_ID=nilrt-xfce/DISTRIB_ID=nilrt/' ${D}${sysconfdir}/lsb-release
	echo "DISTRIB_CODENAME=${NILRT_FEED_NAME}" >> ${D}${sysconfdir}/lsb-release
}
