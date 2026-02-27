# Utility functions for nisystemformat scripts.
# These functions depend on the EXITCODES associative array, which should be
# defined in the top level nisystemformat script before sourcing this file.
#
# Depends on exit_codes.sh for the EXITCODES associative array.

source ${BASH_SOURCE%/*}/exit_codes.sh || exit 8


COMMAND_RETRY_COUNT=30
COMMAND_RETRY_SLEEP=1


# Exit with an error message.
function die() {
	local exit_code="$1"  # exit code key from EXITCODES
	local msg="${*:2}"  # error message text
	echo -e "*** Error: ${BASH_SOURCE[1]}:${BASH_LINENO[1]}: $msg" >&2
	exit ${EXITCODES[$exit_code]}
}
export -f die


# Bash ERR trap handler: Prints an error message and exits with the
# UNKNOWN_ERROR exit code.
function handle_err() {
	local src_path="$1"
	local line_no="$2"
	local func_name="$3"
	local exit_code="$4"
	echo "*** Error: $src_path:$line_no (fn=$func_name): Unexpected status code $exit_code" >&2
	exit ${EXITCODES[UNKNOWN_ERROR]}
}
export -f handle_err


# Install the default error handler for the nisystemformat scripts, which will
# print an error message and exit with the UNKNOWN_ERROR exit code whenever a
# command exits with a non-zero status and is not otherwise handled.
function install_default_err_handler() {
	trap 'handle_err ${BASH_SOURCE} ${LINENO} ${FUNCNAME:-unknown} $?' ERR
}
export -f install_default_err_handler


# Log a message with the specified log level.
# Error messages will also be sent to syslog with the "user.err" priority.
function log() {
	local level="$1"  # log level (e.g. "INFO", "WARN", "ERROR")
	local msg="${*:2}"  # message text

	case "$level" in
		INFO)
			if [ "${VERBOSE:-}" = true ]; then
				echo "INFO: $msg"
			fi
			;;
		WARN)
			echo "WARN: $msg"
			;;
		ERROR)
			echo "ERROR: $msg"
			logger -p user.err "nisystemformat: $msg" || :
			;;
		*)
			echo "UNKNOWN: $msg"
			;;
	esac
}
export -f log


# Creates a temporary file (as through mktemp) in a private directory in
# shared memory which will be deleted on process exit.
# The private directory path is exported as ``TMPDIR_PRIVATE`` and reused
# for subsequent calls to this function.
function mktemp_private() {
	local template="${1:-XXXXXX}"  # template for the temp file

	# If the TMPDIR_PRIVATE doesn't exist yet, create it.
	if [ -z "$TMPDIR_PRIVATE" -o ! -d "$TMPDIR_PRIVATE" ]; then
		TMPDIR_PRIVATE=$(mktemp -d -p /dev/shm ni-systemformat.XXXXXX)
		chmod 0700 "$TMPDIR_PRIVATE"
		trap 'rm -rf "$TMPDIR_PRIVATE"' EXIT INT TERM
	fi

	# Create the temp file in the private directory.
	local tmpfile="$(mktemp -p "$TMPDIR_PRIVATE" "$template")"
	chmod 0600 "$tmpfile"
	echo "$tmpfile"
}
export -f mktemp_private
export TMPDIR_PRIVATE=""


# Retry loop for commands: Executes the specified command repeatedly
# until it either exits with 0 or max number of retries are exhausted.
function with_retry() {
	local cmd="$@"  # command to execute (with args)
	for attemptItr in `seq 1 "$COMMAND_RETRY_COUNT"`; do
		if eval "$cmd"; then
			return 0
		fi

		echo "$* failed with $? (attempt $attemptItr)" >&2
		sleep "$COMMAND_RETRY_SLEEP"
	done

	echo "ERROR: $* failed ($COMMAND_RETRY_COUNT attempts)" >&2
	return 1
}
export -f with_retry
