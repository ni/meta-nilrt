FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://clamav-scan"

do_install:append() {
    # Install clamav-scan wrapper script for memory-constrained NILRT systems
    install -m 0755 ${WORKDIR}/clamav-scan ${D}${bindir}/clamav-scan
}

FILES:${PN} += "${bindir}/clamav-scan"

# NILRT-specific: Backup DNS configuration before installation
pkg_preinst:${PN}-freshclam:append() {
    if [ -n "$D" ]; then
        return 0
    fi
    
    # Backup from /etc/resolv.conf (primary source) if it exists and has content
    if [ -f /etc/resolv.conf ] && [ -s /etc/resolv.conf ]; then
        cp /etc/resolv.conf /tmp/resolv.conf.clamav-backup
    elif [ -f /var/run/resolv.conf ] && [ -s /var/run/resolv.conf ]; then
        cp /var/run/resolv.conf /tmp/resolv.conf.clamav-backup
    fi
}

# NILRT-specific: Add clamav user to adm group for /var/log access
# NILRT systems have ACLs on /var/log that only allow root and adm group
pkg_postinst:${PN}-freshclam:append() {
    if [ -n "$D" ]; then
        return 0
    fi
    
    # Create volatile directories using populate-volatile.sh
    if [ -e ${sysconfdir}/init.d/populate-volatile.sh ]; then
        ${sysconfdir}/init.d/populate-volatile.sh update
    fi
    
    # Add clamav user to adm group if not already a member
    if ! groups ${CLAMAV_USER} | grep -q adm; then
        usermod -a -G adm ${CLAMAV_USER}
    fi
    
    # Restore DNS configuration if needed (NILRT uses /var/run/resolv.conf)
    if [ ! -s /var/run/resolv.conf ]; then
        if [ -f /tmp/resolv.conf.clamav-backup ] && [ -s /tmp/resolv.conf.clamav-backup ]; then
            cp /tmp/resolv.conf.clamav-backup /var/run/resolv.conf
            chmod 644 /var/run/resolv.conf
        else
            echo "WARNING: DNS is not configured on this system."
            echo "ClamAV's freshclam requires DNS to download virus signature updates."
            echo "Please configure DNS by adding nameserver entries to /var/run/resolv.conf"
        fi
    fi
    
    # Clean up backup file
    rm -f /tmp/resolv.conf.clamav-backup
}
