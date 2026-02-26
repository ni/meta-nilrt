# Functions for printing nisystemformat usage and parsing the CLI options.
#
# Depends upon util.sh.


# Exit with usage information and an error message.
function die_with_usage() {
	local exit_code="$1"  # exit code key from EXITCODES
	local msg="${*:2}"  # error message text
	echo -e "*** Error: ${BASH_SOURCE[1]}:${BASH_LINENO[1]}: $msg" >&2
	usage
	exit ${EXITCODES[$exit_code]}
}
export -f die_with_usage


# Parse the command-line arguments for the nisystemformat utility and set
# global variables accordingly.
function parse_args() {
	local argv=("$@")
	# Script invoked with no command-line args?
	if [ ${#argv[@]} -eq "0" ]; then
		die_with_usage INVALID_ARGUMENT "No command-line arguments specified."
	fi

	while getopts "4cfhln:orst:" option
	do
		case $option in
			4)  set_mode runlevel4;;
			c)  VOL=config;;
			f)  set_mode format;;
			h)
				usage
				exit ${EXITCODES[OK]};
				;;
			l)  set_mode list;;
			n)  NETCFG_MODE="$OPTARG";;
			o)  RESET_OVERLAY=yes;;
			r)  RELAUNCH=yes;;
			s)  set_mode status;;
			t)  TYPE=$OPTARG;;
			*)
				die_with_usage INVALID_ARGUMENT "Unsupported option -$option"
				;;
		esac
	done

	validate_args
}
export -f parse_args


# Set the script command mode, stored in the global ``MODE`` variable.
function set_mode()
{
	local mode="$1"
	if [ -n "$MODE" ]; then
		die_with_usage INVALID_ARGUMENT "Only one operation can be specified at a time."
	fi
	MODE=$mode
}
export -f set_mode


# Print usage information for the nisystemformat utility.
usage()
{
	cat >&2 <<EOF
# Commands

Print help and exit:
	$BASENAME -h
Format the userfs with a specified filesystem type:
	$BASENAME -f -t <type> [-c] [-r] [-n <mode>]
Print the current filesystem type of the userfs:
	$BASENAME -s [-c]
List possible filesystem types:
	$BASENAME -l


## RAUC only commands

Reboot to runlevel 4:
	$BASENAME -4 [-r]
Reset the user overlay without formatting the userfs:
	$BASENAME -f -o [-r] [-n <mode>]


# Options

-c          Target the command at the niconfig volume instead of the userfs.
-t  <type>  Filesystem type
-r          Relaunch the system webserver after format
-n          Preserve network config according to <mode>:
	all      Preserve all settings
	primary  Preserve primary, reset secondary
	none     Reset all settings
	bypass   No configuration handling (default)
EOF
}
export -f usage


# Validate command line arguments and combinations thereof, exiting with an
# error message if any invalid args or arg combinations are found.
function validate_args() {
	if [ -z "$MODE" ]; then
		die_with_usage INVALID_ARGUMENT "No operation specified."
	fi


	if [ "$OSMODE" = runmode-rauc ]; then
		if [ -n "$TYPE" ]; then
			die_with_usage INVALID_ARGUMENT \
				"fstype option (-t) cannot be specified on RAUC targets."
		elif [ -n "$VOL" ]; then
			die_with_usage INVALID_ARGUMENT \
				"niconfig volume (-c) cannot be specified on RAUC targets."
		fi
	else
		if [ "$RESET_OVERLAY" = yes ]; then
			die_with_usage INVALID_ARGUMENT \
				"Resetting the overlay is not supported in $OSMODE."
		elif [ "$MODE" = runlevel4 ]; then
			die_with_usage INVALID_ARGUMENT \
				"Booting into run level 4 is not supported in $OSMODE."
		fi
	fi

	if [ "$MODE" = format -a "$OSMODE" = runmode ]; then
		die_with_usage INVALID_ARGUMENT "Formatting is not available in runmode."
	fi

	if [ "$RELAUNCH" = yes ]; then
		if [ "$OSMODE" = runmode-rauc ]; then
			if [ "$MODE" = list -o "$MODE" = status ]; then
				die_with_usage INVALID_ARGUMENT \
					"Reboot can only be used when formatting or booting into run level 4."
			fi
		else
			if [ "$MODE" != format -o "$VOL" != config ]; then
				die_with_usage INVALID_ARGUMENT \
					"relaunch (-r) can only be used when formatting config partition (-f -c)"
			fi
			if [ "$OSMODE" = restore ]; then
				die_with_usage INVALID_ARGUMENT \
					"relaunch (-r) cannot be used in restore mode"
			fi
		fi
	fi

	if [ -n "$TYPE" ]; then
		if [ "$MODE" = status -o "$MODE" = list ]; then
			die_with_usage INVALID_ARGUMENT \
				"fstype option (-t) cannot be used with status -s or -l"
		fi
	fi

	if [ "$MODE" = list -a -n "$VOL" ]; then
		die_with_usage INVALID_ARGUMENT \
			"niconfig volume (-c) cannot be specified when listing fs types (-l)"
	fi

	if [ "$MODE" != format -a "$NETCFG_MODE" != "bypass" ]; then
		die_with_usage INVALID_ARGUMENT \
			"Network settings (-n) can only be specified when formatting (-f)"
	fi

	case "$NETCFG_MODE" in
		all|primary|none|bypass) ;;
		*)
			die_with_usage INVALID_ARGUMENT \
				"Unknown network config setting $NETCFG_MODE"
			;;
	esac

	if [ "$MODE" = format -a "$OSMODE" != runmode-rauc ]; then
		if [ -z "$TYPE" ]; then
			die_with_usage INVALID_FSTYPE "No filesystem type was specified"
		fi
	fi
}
