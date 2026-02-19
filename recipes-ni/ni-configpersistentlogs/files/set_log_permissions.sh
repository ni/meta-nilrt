#!/bin/sh

LOG_DIR="/var/log"

if [ -d "${LOG_DIR}" ]; then
	chown root:adm "${LOG_DIR}"
	chmod 0770 "${LOG_DIR}"
	if command -v setfacl >/dev/null 2>&1; then
		setfacl -d -m g:adm:rwx "${LOG_DIR}"
		setfacl -d -m o::0 "${LOG_DIR}"
	fi
fi

exit 0
