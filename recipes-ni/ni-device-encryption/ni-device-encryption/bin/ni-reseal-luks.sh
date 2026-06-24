#!/bin/bash
# Copyright (c) 2026 Emerson T&M (NI).
# LICENSE: MIT
#
# When called, this utility precalculates the new PCR hashes for the safemode
# and runmode boot components (kernel, initramfs, grub config, et c.), and
# enrolls the new hashes as valid values to release the LUKS decryption keys
# for the niconfig and userfs partitions.
set -e

# ==============================================================================
# CONSTANTS
# ==============================================================================

declare -A EXITCODES=(
	[OK]=0
	[ERR]=1
	[BADARGS]=2
	[BADENV]=3
	[IMPORT_ERROR]=4
)

PCR_BANK="sha256"
PCR_INDEXES="7"

# ==============================================================================
# IMPORTS
# ==============================================================================

SCRIPT_DIR="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"
source "$SCRIPT_DIR/../share/const_luks.sh" || exit ${EXITCODES[IMPORT_ERROR]}


# ==============================================================================
# FUNCTIONS
# ==============================================================================


# Checks that the script is being run in a suitable environment.
function _check_env() {
	if [[ "$EUID" -ne 0 ]]; then
		echo "This script must be run as root." >&2
		exit ${EXITCODES[BADENV]}
	fi
	if ! type tpm2_pcrread >/dev/null 2>&1; then
		echo "tpm2-tools does not appear to be installed. Please install tpm2-tools and try again." >&2
		exit ${EXITCODES[BADENV]}
	fi
	if ! tpm2_pcrread sha256 >/dev/null 2>&1; then
		echo "Unable to read PCR values from the TPM. Please ensure that the TPM is properly initialized and try again." >&2
		exit ${EXITCODES[BADENV]}
	fi
}


function parse_args() {
	local argv=("$@")
	local positionals=()

	while [[ ${#argv[@]} -gt 0 ]]; do
		case "${argv[0]}" in
			-n|--dry-run)
				opt_dry_run=true
				;;
			-h|--help)
				usage 1
				exit ${EXITCODES[OK]}
				;;
			-y|--yes)
				opt_yes=true
				;;
			-l|--label)
				if [[ ${#argv[@]} -lt 2 ]]; then
					echo "--label requires an argument" >&2
					usage 2
					exit ${EXITCODES[BADARGS]}
				fi
				case "${argv[1]}" in
					niconfig-luks|nirootfs-luks)
						arg_label="${argv[1]}"
						;;
					*)
						echo "Invalid label '${argv[1]}'. Must be 'niconfig-luks' or 'nirootfs-luks'." >&2
						usage 2
						exit ${EXITCODES[BADARGS]}
						;;
				esac
				argv=("${argv[@]:1}")
				;;
			-*|--*)
				echo "Unknown option: ${argv[0]}" >&2
				usage 2
				exit ${EXITCODES[BADARGS]}
				;;
			*)
				positionals+=("${argv[0]}")
				;;
		esac
		argv=("${argv[@]:1}")
	done

	# Parse positional arguments
	if [[ ${#positionals[@]} -gt 0 ]]; then
		arg_keyfile="${positionals[0]}"
	fi

	if $opt_dry_run; then
		echo "Dry run mode enabled. No changes will be made."
	fi
}


# Reseals a single LUKS keyslot with new PCR policies.
function _reseal_device_keyslot() {
	local device="$1"
	local keyslot="$2"
	local master_keyfile="$3"

	echo "Resealing PCR state to LUKS keyslot $keyslot on device $device."

	# If the user has not provided a keyfile, try to reuse the clevis key in
	# the keyslot. If there isn't one, we'll have to bail since we have no key.
	if [ -z "$master_keyfile" ]; then
		local keyfile=$(mktemp -p /dev/shm ni-reseal-luks.XXXXXX.keyfile)
		chmod 0600 "$keyfile"
		trap "shred -u $keyfile" EXIT

		clevis luks pass -d "$device" -s "$keyslot" >"$keyfile" || {
			echo >&2 "ERROR: No master keyfile provided, but no existing clevis bind policy on $device, slot $keyslot. Unable to make an original seal without the master keyfile."
			exit ${EXITCODES[ERR]}
		}
	else
		local keyfile="$master_keyfile"
	fi

	if $opt_dry_run; then
		echo >&2 "Dry run. Skipping."
		test -z "$master_keyfile" && shred -u "$keyfile" && trap - EXIT || :
		return
	fi


	if [ -n "$master_keyfile" ]; then
		# Remove the old TPM2 policy, if it exists. Clevis complains otherwise.
		clevis luks unbind \
			-f \
			-d "$device" \
			-s "$keyslot" \
			tpm2 2>/dev/null || :
		# Assign a new TPM2 policy to the keyslot.
		clevis luks bind \
			-d "$device" \
			-s "$keyslot" \
			-k "$keyfile" \
			-y \
			tpm2 \
			"{ \"pcr_bank\": \"$PCR_BANK\", \"pcr_ids\": \"$PCR_INDEXES\" }"
	else
		clevis luks regen \
			-q \
			-d "$device" \
			-s "$keyslot"
	fi
	
	# Clean up the tempkey, if we're using one
	test -z "$master_keyfile" && shred -u "$keyfile" && trap - EXIT || :
}


# Reseals the specified LUKS device by enrolling new PCR policies for both the
# safemode and runmode keyslots.
function reseal_device() {
	local device="$1"  # e.g. the LUKS device path to encrypt
	local keyfile="$2"  # File path to a keyfile that can open the LUKS volume.

	# Let the user know where the key is coming from.
	case $keyfile in
		"")
			echo "No master keyfile provided. Attempting to reseal using existing TPM policies."
			;;
		"-")
			# Read keyfile from stdin
			echo "Reading keyfile from stdin..."
			;;
		*)
			if [ ! -f "$keyfile" ]; then
				echo "ERROR: Provided keyfile '$keyfile' does not exist or is not a file." >&2
				exit ${EXITCODES[BADARGS]}
			fi
			echo "Using provided keyfile: $keyfile"
			;;
	esac

	_reseal_device_keyslot "$device" "$LUKS_KEYSLOT_SAFEMODE" "$keyfile"
	_reseal_device_keyslot "$device" "$LUKS_KEYSLOT_RUNMODE" "$keyfile"

	clevis luks list -d "$device"
}


# Prints usage information to the specified file descriptor (default: stderr).
function usage() {
	local fp_out="${1:-2}"

	cat >&$fp_out <<EOF
Precalculates and enrolls new PCR values for the safemode and runmode boot
components to release LUKS decryption keys.

Usage:
	Print usage information and exit:
		$(basename "$0") [-h]
	Reseal LUKS keys:
		$(basename "$0") [-n] [-y] [-l LABEL] [KEYFILE]

Options:
	-h, --help         Show this help message and exit.
	-n, --dry-run      Perform a dry run without enrolling new PCR values.
	-y, --yes          Automatically confirm enrollment of new PCR values without
	                   prompting.
	-l, --label LABEL  Only reseal the LUKS partition with this filesystem label.
	                   Must be 'niconfig-luks' or 'nirootfs-luks'. If omitted, all
	                   such partitions are resealed.

Arguments:
	KEYFILE        Path to a keyfile that can open the LUKS volume.
	               If '-', the key will be read from stdin.
	               If not provided, the script will attempt to reseal the LUKS
	               partitions using the existing TPM policies.
EOF
}


# ==============================================================================
# GLOBALS
# ==============================================================================

opt_dry_run=false
opt_yes=false
arg_keyfile=""
arg_label=""


# ==============================================================================
# MAIN
# ==============================================================================

_check_env || exit ${EXITCODES[BADENV]}
parse_args "$@" || exit ${EXITCODES[BADARGS]}

# Determine which LUKS devices to reseal.
if [[ -n "$arg_label" ]]; then
	# --label was specified: operate only on that single labeled partition.
	# (parse_args has already restricted it to a known LUKS filesystem label.)
	label_device=$(blkid --label "$arg_label" --output device || :)
	if [[ -z "$label_device" ]]; then
		echo "ERROR: No LUKS partition with label '$arg_label' was found." >&2
		exit ${EXITCODES[BADARGS]}
	fi
	luks_devices=("$label_device")
else
	# No --label: discover all known LUKS partitions by label.
	path_niconfig_luks=$(blkid --label "niconfig-luks" --output device || :)
	path_userfs_luks=$(blkid --label "nirootfs-luks" --output device || :)
	luks_devices=()
	[[ -n "$path_niconfig_luks" ]] && luks_devices+=("$path_niconfig_luks")
	[[ -n "$path_userfs_luks" ]] && luks_devices+=("$path_userfs_luks")
fi

# NOOP if there are no LUKS partitions (that we care about).
if [[ ${#luks_devices[@]} -eq 0 ]]; then
	echo "No LUKS partitions found. Nothing to reseal."
	exit ${EXITCODES[OK]}
fi

# Prompt the user for informed consent.
# This part is mostly to defend against accidental script execution.
if ! $opt_yes; then
	echo "This will enroll new PCR values for the safemode and runmode boot components to release the LUKS keys for the following partitions:"
	for _dev in "${luks_devices[@]}"; do echo -e "\t$_dev"; done
	read -p "To continue, type 'RESEAL': " -r
	if [[ ! $REPLY =~ ^RESEAL$ ]]; then
		echo "Aborting."
		exit ${EXITCODES[OK]}
	fi
fi

# Reseal each device.
for _dev in "${luks_devices[@]}"; do
	reseal_device "$_dev" "$arg_keyfile"
done

exit ${EXITCODES[OK]}
