# Bash library containing functions for encrypting partitions with LUKS
# and backing those LUKS partitions with keys stored in HSMs such as TPMs.

source ${BASH_SOURCE%/*}/util.sh || exit 1


# Check that the system has a TPM device and exit with an error if not.
function check_tpm() {
	if ! has_tpm; then
		die BAD_ENVIRONMENT "Encryption was requested but no TPM device was found."
	fi
}
export -f check_tpm


# Check if the system has a TPM device.
# Returns 0 if a TPM device is found, 1 otherwise.
function has_tpm() {
	if [ -d /sys/class/tpm ]; then
		return 0
	else
		return 1
	fi
}
export -f has_tpm


