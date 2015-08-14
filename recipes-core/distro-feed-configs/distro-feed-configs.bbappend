DISTRO_FEED_PREFIX     = "NIOE"
DISTRO_FEED_URI        = "http://nickdanger.amer.corp.natinst.com/feeds/NG/NIOEFeeds/15.0/latest_trunk/${MACHINE_ARCH}"

DISTRO_FEED_PREFIX_02  = "NIOEExtras"
DISTRO_FEED_URI_02     = "http://nickdanger.amer.corp.natinst.com/feeds/NG/NIOEExtrasFeed/0.0/latest_trunk/${MACHINE_ARCH}"

DISTRO_FEED_PREFIX_03  = "NI"
DISTRO_FEED_URI_03     = "http://nickdanger.amer.corp.natinst.com/feeds/NG/NIFeeds/0.0/latest_trunk/${MACHINE_ARCH}"

NI_FEEDS_append_arm    = "all cortexa9-vfpv3 xilinx-zynq"
NI_FEEDS_append_x86-64 = "all core2-64 x64"

do_compile() {
    mkdir -p ${S}/${sysconfdir}/opkg

    for feed in ${NI_FEEDS}; do
        echo "src/gz ${DISTRO_FEED_PREFIX}-${feed} ${DISTRO_FEED_URI}/${feed}" > ${S}/${sysconfdir}/opkg/${DISTRO_FEED_PREFIX}-${feed}-feed.conf
        echo "src/gz ${DISTRO_FEED_PREFIX_02}-${feed} ${DISTRO_FEED_URI_02}/${feed}" > ${S}/${sysconfdir}/opkg/${DISTRO_FEED_PREFIX_02}-${feed}-feed.conf
        echo "src/gz ${DISTRO_FEED_PREFIX_03}-${feed} ${DISTRO_FEED_URI_03}/${feed}" > ${S}/${sysconfdir}/opkg/${DISTRO_FEED_PREFIX_03}-${feed}-feed.conf
    done
}
