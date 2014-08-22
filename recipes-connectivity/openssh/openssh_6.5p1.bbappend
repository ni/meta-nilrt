do_install_append () {
    # modify sshd so that if sshd is not enabled in ni-rt.ini, sshd does not start
    sed 's|check_for_no_start() {|&\
    # if sshd is not enabled in ni-rt.ini, do not start sshd\
    enable=`/usr/local/natinst/bin/nirtcfg --get section=SystemSettings,token=sshd.enabled,value="false" \|tr "[:upper:]" "[:lower:]"`\
    if [ "$enable" != "true" ]; then\
        [ "${VERBOSE}" != "no" ] \&\& echo "SSHD not enabled in ni-rt.ini"\
        exit 0\
    fi|' -i ${D}${sysconfdir}/init.d/sshd
}
