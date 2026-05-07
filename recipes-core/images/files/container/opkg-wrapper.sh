#!/bin/sh
# opkg wrapper for NILRT containers.
#
# LabVIEW/VeriStand expect the target to reboot after installation so the
# LabVIEW RT engine (lvrt) re-reads its startup application (e.g. the
# VeriStand Engine rtexe). Containers are not rebooted, and an already-running
# lvrt does not pick up a newly-installed startup app on its own.
#
# This wrapper runs the real opkg and, after a successful install/upgrade,
# restarts nilvrt -- but ONLY when:
#   1. an RT-relevant package was installed/upgraded (ni-veristand-engine*,
#      ni-labview-realtime*), AND
#   2. lvrt is currently idle, i.e. not serving an application.
#
# The idle check protects running work: installing an unrelated package
# (trace-cmd, etc.) never restarts lvrt, and even an RT-relevant upgrade is
# skipped while lvrt is actively running a VI/engine, so deployed
# applications are not torn down underneath the user. lvrt opens its
# application-level ports (VeriStand engine 2040/2050, VI Server 3363) only
# while an application is loaded; when idle, none of them listen.
#
# Installed to /usr/local/bin/opkg, which precedes /usr/bin/opkg on PATH.

REAL_OPKG=/usr/bin/opkg

# lvrt_busy: return 0 (busy) if lvrt is currently serving an application, i.e.
# any application-level port is in the LISTEN state (st == 0A in /proc/net/tcp):
#   2040 -> 0x07F8 (VeriStand engine)
#   2050 -> 0x0802 (VeriStand engine)
#   3363 -> 0x0D23 (LabVIEW VI Server)
# Returns 1 (idle) if none of them are listening.
lvrt_busy() {
    for f in /proc/net/tcp /proc/net/tcp6; do
        [ -r "$f" ] || continue
        if awk 'NR > 1 {
                    split($2, a, ":")
                    if ($4 == "0A" && (a[2] == "07F8" || a[2] == "0802" || a[2] == "0D23"))
                        found = 1
                }
                END { exit found ? 0 : 1 }' "$f"; then
            return 0
        fi
    done
    return 1
}

# is_rt_pkg: return 0 if the given package name is one whose installation
# affects the lvrt startup application and therefore warrants a restart.
is_rt_pkg() {
    case "$1" in
        ni-veristand-engine*|ni-labview-realtime*) return 0 ;;
        *) return 1 ;;
    esac
}

"$REAL_OPKG" "$@"
rc=$?

# Parse the opkg command line: find the subcommand (skipping global options and
# their arguments) and collect the package-name operands that follow it.
subcommand=""
pkgs=""
expect_option_arg=0
for arg in "$@"; do
    if [ -n "$subcommand" ]; then
        case "$arg" in
            -*) ;;            # ignore per-command options
            *) pkgs="$pkgs $arg" ;;
        esac
        continue
    fi
    if [ "$expect_option_arg" -eq 1 ]; then
        expect_option_arg=0
        continue
    fi
    case "$arg" in
        -f|--conf|-o|--offline-root|-t|--tmp-dir|-l|--lists-dir|--cache|--add-arch|--add-dest)
            expect_option_arg=1
            ;;
        --conf=*|--offline-root=*|--tmp-dir=*|--lists-dir=*|--cache=*|--add-arch=*|--add-dest=*)
            ;;
        -*)
            ;;
        *)
            subcommand="$arg"
            ;;
    esac
done

# Decide whether an RT-relevant package was involved.
rt_relevant=0
case "$subcommand" in
    install)
        for p in $pkgs; do
            if is_rt_pkg "$p"; then rt_relevant=1; break; fi
        done
        ;;
    upgrade)
        if [ -z "$pkgs" ]; then
            # "opkg upgrade" with no operands upgrades everything, which may
            # include an RT package; rely on the idle gate below.
            rt_relevant=1
        else
            for p in $pkgs; do
                if is_rt_pkg "$p"; then rt_relevant=1; break; fi
            done
        fi
        ;;
esac

# Restart lvrt only on a successful, RT-relevant transaction while lvrt is idle.
if [ "$rc" -eq 0 ] && [ "$rt_relevant" -eq 1 ] && [ -x /etc/init.d/nilvrt ]; then
    if lvrt_busy; then
        echo "opkg: lvrt is serving an application; skipping lvrt restart to avoid interrupting it." >&2
    else
        echo "opkg: restarting lvrt to launch newly-installed RT application..." >&2
        /etc/init.d/nilvrt stop 2>/dev/null
        sleep 1
        /etc/init.d/nilvrt start 2>/dev/null
    fi
fi

exit $rc
