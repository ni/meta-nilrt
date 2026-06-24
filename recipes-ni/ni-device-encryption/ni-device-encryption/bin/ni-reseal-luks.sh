#!/bin/bash
# Copyright (c) 2026 Emerson T&M (NI).
# LICENSE: MIT
#
# When called, this utility precalculates the new PCR hashes for the safemode
# and runmode boot components (kernel, initramfs, grub config, et c.), and
# enrolls the new hashes as valid values to release the LUKS decryption keys
# for the niconfig and userfs partitions.
#
# PCR 15 is pre-calculated from the artifact lists and sealed together with
# PCR 7 (Secure Boot state) using Clevis's pcr_digest parameter. This allows
# the keys to be resealed before rebooting, eliminating the vulnerability window
# that would otherwise exist if we decrypted, rebooted, and then resealed. Also,
# rebooting to get the new PCR values would be difficult because we would have
# to reboot to both safemode and runmode since the PCR values are different for
# each boot mode.
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

# ==============================================================================
# IMPORTS
# ==============================================================================

SCRIPT_DIR="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"
source "$SCRIPT_DIR/../share/const_luks.sh" || exit ${EXITCODES[IMPORT_ERROR]}


# ==============================================================================
# FUNCTIONS
# ==============================================================================


# Prints the LUKS2 metadata of $device as JSON on stdout.
function _luks_json() {
	local device="$1"
	cryptsetup luksDump --dump-json-metadata "$device" 2>/dev/null
}


# Prints the numbers of all active LUKS keyslots on $device, one per line, in
# ascending numeric order.
function _luks_active_keyslots() {
	local device="$1"
	_luks_json "$device" | jq -r '.keyslots | keys_unsorted[]' | sort -n
}


# Prints the ids of all clevis tokens bound to keyslot $keyslot on $device, one
# per line. LUKS2 tokens reference keyslots via a "keyslots" array and a token
# id is independent of the keyslot number, so this correctly handles both the
# id != slot case and a keyslot carrying more than one clevis token.
function _clevis_token_ids_for_slot() {
	local device="$1"
	local keyslot="$2"
	_luks_json "$device" \
		| jq -r --arg ks "$keyslot" \
			'.tokens | to_entries[]
			 | select(.value.type == "clevis" and (.value.keyslots | index($ks)))
			 | .key'
}


# Prints the numbers of all keyslots bound to a clevis token on $device, one
# per line, in ascending numeric order and de-duplicated.
function _clevis_keyslots() {
	local device="$1"
	_luks_json "$device" \
		| jq -r '.tokens | to_entries[]
			 | select(.value.type == "clevis")
			 | .value.keyslots[]' \
		| sort -nu
}


# Shreds any temporary key/PCR material recorded in _cleanup_files and unmounts
# the temporary nigrub mount recorded in _cleanup_mount. Registered as the EXIT
# trap so that secrets are never left behind in /dev/shm and nigrub is never
# left mounted if the script aborts partway through (e.g. via 'set -e').
function _cleanup() {
	set +e
	local f
	for f in "${_cleanup_files[@]}"; do
		[ -n "$f" ] && [ -e "$f" ] && shred -u "$f"
	done
	_cleanup_files=()
	if [ -n "$_cleanup_mount" ]; then
		umount "$_cleanup_mount" 2>/dev/null
		rmdir "$_cleanup_mount" 2>/dev/null
		_cleanup_mount=""
	fi
}


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
	if ! type jose >/dev/null 2>&1; then
		echo "jose is not installed. Please install jose and try again." >&2
		exit ${EXITCODES[BADENV]}
	fi
	if ! type jq >/dev/null 2>&1; then
		echo "jq is not installed. Please install jq and try again." >&2
		exit ${EXITCODES[BADENV]}
	fi
	if ! blkid --label "nigrub" >/dev/null 2>&1; then
		echo "nigrub partition not found. Cannot access GRUB artifacts for PCR 15 pre-calculation." >&2
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


# Reseals a single LUKS keyslot with new PCR 7+15 policies using pcr_digest.
function _reseal_device_keyslot() {
	local device="$1"
	local keyslot="$2"
	local keyfile="$3"  # a keyfile that can open the LUKS volume
	local pcr_digest="$4"  # base64-encoded PCR7[32]||PCR15[32] for Clevis

	echo "Resealing PCR state to LUKS keyslot $keyslot on device $device."

	# Remove the old TPM2 policy, if it exists. Clevis complains otherwise.
	# Calling "clevis luks unbind" when TPM PCR values don't match the seal
	# (which is always the case here: we are sealing against *future* PCR values)
	# causes clevis to fall back to prompting for a passphrase on stdin which hangs.
	# Instead, use cryptsetup directly with the master key file to avoid this.
	# Identify any clevis tokens bound to this keyslot *before* killing it, then
	# kill the keyslot and remove its now-orphaned tokens. A keyslot may carry
	# more than one clevis token, so remove them all.
	local _toks _tok
	mapfile -t _toks < <(_clevis_token_ids_for_slot "$device" "$keyslot")
	cryptsetup luksKillSlot -q --key-file "$keyfile" "$device" "$keyslot" 2>/dev/null || :
	for _tok in "${_toks[@]}"; do
		[ -n "$_tok" ] && cryptsetup token remove --token-id "$_tok" "$device" 2>/dev/null || :
	done
	# Bind a new TPM2 policy sealed against PCR 7 + PCR 15 using the
	# pre-calculated pcr_digest. The pcr_digest encodes the expected state
	# of both PCRs at the time of the next boot, allowing pre-calculation
	# before rebooting.
	#
	# A failure here is consequential: the old keyslot has already been killed
	# above, so if the bind does not complete the slot is left unpolicied. Catch
	# it explicitly (rather than letting 'set -e' abort silently) and tell the
	# operator how to recover.
	if ! clevis luks bind \
			-d "$device" \
			-s "$keyslot" \
			-k "$keyfile" \
			-y \
			tpm2 \
			"{ \"pcr_bank\": \"$PCR_BANK\", \"pcr_ids\": \"7,15\", \"pcr_digest\": \"$pcr_digest\" }"; then
		echo >&2 "ERROR: Failed to bind new TPM policy to keyslot $keyslot on $device."
		echo >&2 "       Keyslot $keyslot has been cleared and is now unpolicied. Re-run"
		echo >&2 "       ni-reseal-luks while another keyslot is still TPM-unlockable"
		echo >&2 "       to restore it."
		exit ${EXITCODES[ERR]}
	fi
}


# Reseals the specified LUKS device by enrolling new PCR 7+15 policies for
# both the safemode and runmode keyslots.
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

	# Mount nigrub so we can measure artifacts on that partition.
	# Register the mount and temp files with the EXIT trap (_cleanup) so secrets
	# in /dev/shm are shredded and nigrub is unmounted even if we abort below.
	local nigrub_mount
	nigrub_mount=$(mktemp -d /tmp/ni-reseal-nigrub.XXXXXX)
	_cleanup_mount="$nigrub_mount"
	mount -L nigrub "$nigrub_mount" || {
		echo "ERROR: Could not mount nigrub partition." >&2
		exit ${EXITCODES[ERR]}
	}

	# Read current PCR 7 (Secure Boot state; stable across boots while Secure
	# Boot policy is unchanged)
	local pcr7_file
	pcr7_file=$(mktemp -p /dev/shm ni-reseal-pcr7.XXXXXX.bin)
	chmod 0600 "$pcr7_file"
	_cleanup_files+=("$pcr7_file")

	if ! tpm2_pcrread "${PCR_BANK}:7" --output "$pcr7_file"; then
		echo "ERROR: Failed to read PCR 7 from TPM." >&2
		exit ${EXITCODES[ERR]}
	fi

	# Pre-calculate PCR 15 for safemode and compute pcr_digest = base64(PCR7||PCR15)
	local pcr_digest_safemode_file pcr_digest_safemode
	pcr_digest_safemode_file=$(mktemp -p /dev/shm ni-reseal-pcr-digest.XXXXXX.bin)
	chmod 0600 "$pcr_digest_safemode_file"
	_cleanup_files+=("$pcr_digest_safemode_file")

	echo "Pre-calculating PCR 15 for safemode..."
	if ! ni-pcr-precalc \
			--artifact-list /usr/lib/ni-device-encryption/share/ni-pcr-safemode-artifacts.list \
			--nigrub-mount "$nigrub_mount" \
			--pcr7 "$pcr7_file" \
			--output-pcr-digest "$pcr_digest_safemode_file"; then
		echo "ERROR: ni-pcr-precalc failed for safemode." >&2
		exit ${EXITCODES[ERR]}
	fi
	# Keep the variable declaration and assignment separate. Combining them like
	# 'local pcr_digest_safemode=$(...)' would mask jose's exit status behind
	# 'local' and defeat 'set -e'.
	pcr_digest_safemode=$(jose b64 enc -I "$pcr_digest_safemode_file" -o -)

	# Pre-calculate PCR 15 for runmode and compute pcr_digest = base64(PCR7||PCR15)
	local pcr_digest_runmode_file pcr_digest_runmode
	pcr_digest_runmode_file=$(mktemp -p /dev/shm ni-reseal-pcr-digest.XXXXXX.bin)
	chmod 0600 "$pcr_digest_runmode_file"
	_cleanup_files+=("$pcr_digest_runmode_file")

	echo "Pre-calculating PCR 15 for runmode..."
	if ! ni-pcr-precalc \
			--artifact-list /usr/lib/ni-device-encryption/share/ni-pcr-runmode-artifacts.list \
			--nigrub-mount "$nigrub_mount" \
			--pcr7 "$pcr7_file" \
			--output-pcr-digest "$pcr_digest_runmode_file"; then
		echo "ERROR: ni-pcr-precalc failed for runmode." >&2
		exit ${EXITCODES[ERR]}
	fi
	# Keep the declaration and assignment separate (see the safemode note above).
	pcr_digest_runmode=$(jose b64 enc -I "$pcr_digest_runmode_file" -o -)

	# nigrub and the PCR/digest temp files are no longer needed; clean up now
	# rather than waiting for the EXIT trap so the next reseal_device call starts
	# fresh.
	_cleanup
	# _cleanup runs 'set +e' internally and does not restore it; re-enable
	# 'set -e' so the rest of this function keeps aborting on error.
	set -e

	# When no master keyfile is provided, extract the passphrase from whichever
	# slot is currently unlockable via the TPM.
	# Any valid LUKS passphrase can authenticate against any slot for kill/rebind.
	# Hold the extracted passphrase in an unlinked tmpfs file referenced only by
	# an fd: the data lives in the kernel page cache and is freed automatically
	# when the fd is closed (function return, process exit, or any abort path), so
	# it never persists as a named file and needs no shred or EXIT-trap cleanup.
	# We pass /proc/$$/fd/$fd (script PID expanded here) so the path stays valid
	# for subprocesses such as clevis.
	local _extracted_keyfile="" _extracted_key_fd=""
	if [ -z "$keyfile" ]; then
		local _extracted_tmp
		_extracted_tmp=$(mktemp -p /dev/shm ni-reseal-luks.XXXXXX.keyfile)
		chmod 0600 "$_extracted_tmp"
		exec {_extracted_key_fd}<>"$_extracted_tmp"
		rm -f "$_extracted_tmp"
		_extracted_keyfile="/proc/$$/fd/$_extracted_key_fd"
		local _found=false
		# Try every clevis-bound slot. At best, if the reserved safemode/
		# runmode slot matching the current boot mode has not already been sealed
		# against a future PCR state during the current boot, then that reserved slot
		# might unlock. Otherwise, we expect the special "current-boot" slot to unlock
		# since it was sealed against the current PCR state just for this situation.
		local _all_clevis_slots
		mapfile -t _all_clevis_slots < <(_clevis_keyslots "$device")
		for _slot in "${_all_clevis_slots[@]}"; do
			# Redirect stdin from /dev/null so that this stays non-interactive.
			# 'clevis luks pass' gets its secret from the TPM (not stdin) and
			# returns the passphrase on stdout, so /dev/null does not affect the
			# success path. It future-proofs against a clevis version that falls
			# back to prompting for a passphrase on stdin: instead of hanging, the
			# call hits EOF and fails fast, and the loop moves on to the next slot.
			# This is not entirely speculative, as we hit a case where "clevis luks
			# unbind" would prompt for a passphrase on stdin if the TPM PCRs did not
			# match the seal.
			if clevis luks pass -d "$device" -s "$_slot" \
					</dev/null >"$_extracted_keyfile" 2>/dev/null; then
				echo "Unlocked slot $_slot via TPM for live-reseal."
				keyfile="$_extracted_keyfile"
				_found=true
				break
			fi
		done
		if ! $_found; then
			exec {_extracted_key_fd}>&-
			echo "ERROR: No master keyfile and no TPM-unlockable slot found." >&2
			exit ${EXITCODES[ERR]}
		fi
	fi

	# Everything above this point is read-only with respect to the on-disk LUKS
	# header (nigrub has already been unmounted, and passphrase extraction only
	# releases an existing key). Under --dry-run, report what a real reseal would
	# do and stop here, before the first header-modifying operation (the bridge
	# luksAddKey below).
	if $opt_dry_run; then
		echo "Dry run: validated inputs for $device; no changes were made."
		echo "  safemode pcr_digest (PCR 7+15): $pcr_digest_safemode"
		echo "  runmode  pcr_digest (PCR 7+15): $pcr_digest_runmode"
		echo "  Would reseal keyslot $LUKS_KEYSLOT_SAFEMODE (safemode) and keyslot $LUKS_KEYSLOT_RUNMODE (runmode) to PCR 7+15."
		local _plan_slot
		while IFS= read -r _plan_slot; do
			[[ "$_plan_slot" == "$LUKS_KEYSLOT_SAFEMODE" ]] && continue
			[[ "$_plan_slot" == "$LUKS_KEYSLOT_RUNMODE" ]] && continue
			echo "  Would remove extra keyslot $_plan_slot."
		done < <(_luks_active_keyslots "$device")
		if [ -n "$_extracted_keyfile" ]; then
			echo "  Would add and then remove a temporary bridge keyslot for live-reseal."
		fi
		# Close (and thereby free) the extracted passphrase now; process exit would
		# also close it, but release it as soon as it is no longer needed.
		[ -n "$_extracted_key_fd" ] && exec {_extracted_key_fd}>&- && _extracted_key_fd=""
		return 0
	fi

	# In the keyfile-less path we extracted the passphrase above; add a bridge
	# keyslot with that same passphrase so it remains valid throughout all
	# kill+rebind operations. Without this, killing the original slot (which
	# provided the passphrase) would invalidate the only auth key before the
	# second slot is rebound.
	local _bridge_slot=""
	if [ -n "$_extracted_keyfile" ]; then
		# Snapshot the active keyslots before adding the bridge so we can identify
		# which slot the bridge landed in by diffing afterward.
		local _slots_before
		_slots_before="$(_luks_active_keyslots "$device")"
		if ! cryptsetup luksAddKey --batch-mode \
				--key-file "$keyfile" "$device" "$keyfile" 2>/dev/null; then
			exec {_extracted_key_fd}>&-
			echo "ERROR: Could not add bridge keyslot for live-reseal." >&2
			exit ${EXITCODES[ERR]}
		fi
		# The bridge slot is whichever slot became active as a result of the
		# luksAddKey above. Diffing the before/after active-slot sets is robust
		# regardless of which physical slot cryptsetup chose and keeps us from
		# mistakenly selecting a dead master slot 0 or the special "current-boot"
		# slot.
		_bridge_slot="$(_luks_active_keyslots "$device" \
			| grep -vxF "$_slots_before" | head -1)"
		echo "Added bridge keyslot ${_bridge_slot} for live-reseal."
	fi

	_reseal_device_keyslot "$device" "$LUKS_KEYSLOT_SAFEMODE" "$keyfile" "$pcr_digest_safemode"
	_reseal_device_keyslot "$device" "$LUKS_KEYSLOT_RUNMODE" "$keyfile" "$pcr_digest_runmode"

	# Remove any remaining keyslots that are neither safemode nor runmode:
	# the dead master-key slot (slot 0) left over from initial luksFormat,
	# and the current-boot slot added by nisystemformat for same-session
	# re-opening (sealed to current-boot PCRs, now superseded).
	# IMPORTANT: Do this BEFORE removing the bridge so that the bridge
	# provides an alternate slot accepting $keyfile.  cryptsetup luksKillSlot
	# requires at least one OTHER active slot that matches the supplied
	# keyfile; without the bridge, killing the current-boot slot would fail.
	while IFS= read -r _extra_slot; do
		[[ "$_extra_slot" == "$LUKS_KEYSLOT_SAFEMODE" ]] && continue
		[[ "$_extra_slot" == "$LUKS_KEYSLOT_RUNMODE" ]] && continue
		[[ -n "${_bridge_slot:-}" && "$_extra_slot" == "$_bridge_slot" ]] && continue
		cryptsetup luksKillSlot -q --key-file "$keyfile" "$device" "$_extra_slot" 2>/dev/null \
			&& echo "Removed extra keyslot ${_extra_slot}." || :
	done < <(_luks_active_keyslots "$device")

	# Remove the bridge keyslot now that all extra slots have been cleaned up.
	# Use luksRemoveKey (passphrase-based) instead of luksKillSlot (slot-number-
	# based): after the current-boot slot is gone the bridge is the only slot
	# that accepts $keyfile, and luksKillSlot would then fail its own
	# "other-slot authentication" requirement.
	if [ -n "${_bridge_slot:-}" ]; then
		cryptsetup luksRemoveKey -q --key-file "$keyfile" "$device" 2>/dev/null || :
		echo "Removed bridge keyslot ${_bridge_slot}."
	fi

	# Close (and thereby free) the extracted passphrase as soon as it is no longer
	# needed rather than waiting for process exit, to minimize the window the key
	# material is resident. Closing the fd releases the unlinked tmpfs inode.
	[ -n "$_extracted_key_fd" ] && exec {_extracted_key_fd}>&- && _extracted_key_fd=""

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
	-n, --dry-run      Validate the reseal (PCR pre-calculation and TPM unlock)
	                   and print the plan without modifying any LUKS keyslots.
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

# Temporary state cleaned up by the EXIT trap (_cleanup).
_cleanup_mount=""
_cleanup_files=()


# ==============================================================================
# MAIN
# ==============================================================================

_check_env || exit ${EXITCODES[BADENV]}
parse_args "$@" || exit ${EXITCODES[BADARGS]}

# Ensure temporary secrets and mounts are cleaned up on any exit.
trap _cleanup EXIT

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
