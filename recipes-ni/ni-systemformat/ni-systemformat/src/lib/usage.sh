# Functions for printing nisystemformat usage and parsing the CLI options.
#
# Depends upon util.sh.

source ${BASH_SOURCE%/*}/util.sh || exit 8


# ==============================================================================
# OPTIONS
# ==============================================================================

export MODE=""  # Command mode to run in; one of "format", "status", "list", or "runlevel4"
export NETCFG_MODE=bypass  # Whether and how to preserve network config when formatting
export OPT_ENCRYPT=no  # Whether to encrypt the partition with LUKS when formatting
export RELAUNCH=no  # Whether to relaunch the system webserver after format
export TYPE=""  # Filesystem type to format with, for format command
export VOL=userfs  # Volume to operate on; either "userfs" or "config"


# ==============================================================================
# FUNCTIONS
# ==============================================================================


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

	while getopts "4cefhln:orst:" option
	do
		case $option in
			4)  set_mode runlevel4;;
			c)  VOL=config;;
			e)  OPT_ENCRYPT=yes;;
			f)  set_mode format;;
			h)
				usage
				exit ${EXITCODES[OK]};
				;;
			l)  set_mode list;;
			n)  NETCFG_MODE="$OPTARG";;
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
	if [ -n "${MODE:-}" ]; then
		die_with_usage INVALID_ARGUMENT "Only one operation can be specified at a time."
	fi
	MODE=$mode

	if [ "$MODE" = format -a "$EUID" -ne 0 ]; then
		die INSUFFICIENT_PRIVILEGES "Insufficient privileges for the requested operation; aborting."
	fi
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
	$BASENAME -f -t <type> [-c [-r]] [-e] [-n <mode>]
Print the current filesystem type of the userfs:
	$BASENAME -s [-c]
List possible filesystem types:
	$BASENAME -l


# Options

-c          Target the command at the niconfig volume instead of the userfs.
-e          Encrypt the partition with LUKS, when formatting.
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
	case "$MODE" in
		format) _validate_args_format;;
		list) ;;
		status) ;;
		*)
			die_with_usage INVALID_ARGUMENT "No command specified."
			;;
	esac
}
export -f validate_args


# Validate arguments for the format command
function _validate_args_format() {
	if [ "$OSMODE" = runmode ]; then
		die_with_usage INVALID_ARGUMENT "Formatting is not available in runmode."
	fi

	# Option: relaunch
	if [ "$RELAUNCH" = yes ]; then
		if [ "$OSMODE" = restore ]; then
			die_with_usage INVALID_ARGUMENT \
				"relaunch (-r) cannot be used in restore mode"
		fi
		if [ "$VOL" != config ]; then
			die_with_usage INVALID_ARGUMENT \
				"relaunch (-r) can only be used when formatting config partition (-f -c)"
		fi
	fi

	# Option: network config preservation mode
	case "$NETCFG_MODE" in
		all|primary|none|bypass) ;;
		*)
			die_with_usage INVALID_ARGUMENT \
				"Unknown network config setting $NETCFG_MODE"
			;;
	esac

	# Option: fstype
	if [ -z "$TYPE" ]; then
		die_with_usage INVALID_FSTYPE "No filesystem type was specified"
	fi
}
