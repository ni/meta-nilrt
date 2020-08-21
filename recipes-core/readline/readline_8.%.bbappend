
# pgup/pgdn search bash command history

do_install_append(){
  sed -i "/delete-char/s/^# //g" ${D}${sysconfdir}/inputrc
  sed -i "/history-search/s/^# //g" ${D}${sysconfdir}/inputrc
}

