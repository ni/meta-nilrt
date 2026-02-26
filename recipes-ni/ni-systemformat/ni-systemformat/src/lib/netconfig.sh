# Shell functions for handling network configuration backup and restore.


# ==============================================================================
# CONSTANTS
# ==============================================================================

NETCFG_MODE=bypass
NETCFG_TMP=/tmp/netconfig.$$
NINETCFGUTIL=ninetcfgutil


# ==============================================================================
# IMPORTS
# ==============================================================================

source ${BASH_SOURCE%/*}/usage.sh || exit 8


# ==============================================================================
# FUNCTIONS
# =============================================================================


# Pre-format backup: Save the current network configuration to a temporary
# location, so that it can be restored after formatting. If NETCFG_MODE is
# "none", also erase the saved config and pull the default config from the
# system.
netconfig_pre()
{
	[[ $NETCFG_MODE == bypass || $NETCFG_MODE == none ]] && return 0
	rm -rf $NETCFG_TMP
	mkdir -p $NETCFG_TMP
	OPTS=()
	case "$NETCFG_MODE" in
		all) OPTS+=(-g secondary) ;&
		primary) OPTS+=(-g primary) ;;
	esac
	$NINETCFGUTIL save -d $NETCFG_TMP ${OPTS[@]}
}
export -f netconfig_pre


# Post-restore cleanup
netconfig_post()
{
	case "$NETCFG_MODE" in
		bypass)
			return 0 ;;
		all|primary)
			$NINETCFGUTIL restore -d $NETCFG_TMP
			rm -rf $NETCFG_TMP
			;;
		none)
			rm -rf $NETCFG_TMP
			mkdir -p $NETCFG_TMP
			$NINETCFGUTIL erase -d $NETCFG_TMP -g primary -g secondary
			rm -rf $NETCFG_TMP
			mkdir -p $NETCFG_TMP
			$NINETCFGUTIL pulldefault -d $NETCFG_TMP -g primary -g secondary
			rm -rf $NETCFG_TMP
			;;
	esac
}
export -f netconfig_post
