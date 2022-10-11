
inherit nilrt-permit-commercial

python do_sanity_check_commercial() {
    if '--enable-nonfree' in d.getVar('EXTRA_OECONF'):
        bb.fatal('ffmpeg may not enable nonfree codecs.')
}
