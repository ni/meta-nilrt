#! /bin/sh
# Copyright (c) 2012-2013 National Instruments.
# All rights reserved.

DAEMON=/usr/sbin/crdd

test -x "$DAEMON" || exit 0

case "$1" in
  start)
        start-stop-daemon --start --quiet -x $DAEMON
        ;;
  stop)
        start-stop-daemon --stop --quiet -x $DAEMON
        ;;
  force-reload|restart|reload)
        start-stop-daemon --stop --quiet -x $DAEMON
	start-stop-daemon --start --quiet -x $DAEMON
        ;;
  *)
        echo "Usage: $DAEMON {start|stop|restart|reload|force-reload}" >&2
        exit 1
        ;;
esac

exit 0
