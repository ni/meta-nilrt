#!/bin/sh
# Copyright (c) 2026 Emerson
# SPDX-License-Identifier: MIT
#
# A helper script which will interrogate the value of ``/proc/cmdline`` and
# export shell variables to the NILRT runmode initramfs scripts.
#set -eu


# ==============================================================================
# CONSTANTS
# ==============================================================================

CMDLINE_PATH="/proc/cmdline"
if ! [ -r "$CMDLINE_PATH" ]; then
	echo "ERROR: Unable to read $CMDLINE_PATH" >&2
	exit 1
fi

CMDLINE="$(cat "$CMDLINE_PATH")"


# ==============================================================================
# FUNCTIONS
# ==============================================================================


# UTILITIES

# Checks if a given key=value pair is present in the kernel command line.
# Returns either the key-value if found, or the provided default value if not.
_cmdline_get_key() {
	key="$1"      # The key to search for in the command line.
	default="$2"  # The value to return, if the key is not found.

	value="$(echo "$CMDLINE" | sed -n "s/.*\b${key}=\([^ ]*\).*/\1/p" || true)"
	echo "${value:-$default}"
}


# Checks if a given word is present in the kernel command line.
# Returns 0 (success) if the word is found, or 1 (failure) if not.
_word_in_cmdline() {
	word="$1"
	echo "$CMDLINE" | grep -qw "$word"
}


# Check if **any** of the given words are present in the kernel command line.
# Returns 0 (success) if at least one word is found, or 1 (failure) otherwise.
_words_in_cmdline() {
	for word in "$@"; do
		if _word_in_cmdline "$word"; then
			return 0
		fi
	done
	return 1
}


# VARIABLE PARSING

# Parse the value of the DEBUG variable.
parse_debug() {
	DEBUG=""  # default
	# debian uses "debug" to enable debug logging.
	# dracut uses "rd.debug" for the same purpose.
	# systemd supports both.
	if _words_in_cmdline "debug" "rd.debug"; then
		DEBUG="yes"; return
	fi
}


# Parse the value of the RD_BREAK variable.
parse_rd_break() {
	RD_BREAK=""  # default
	# Note. We have to check for key-value first, because the _word functions
	# will match the key portions as well.

	# debian supports keyvalue breaks for fine-grained control.
	if [ "$(_cmdline_get_key "break" "NONE")" != "NONE" ]; then
		RD_BREAK="$(_cmdline_get_key "break" "NONE")"
		return
	fi
	# dracut also supports keyvalues in their break syntax.
	if [ "$(_cmdline_get_key "rd.break" "NONE")" != "NONE" ]; then
		RD_BREAK="$(_cmdline_get_key "rd.break" "NONE")"
		return
	fi
	# debian, systemd, and dracut use these values generically
	if _words_in_cmdline "rd.break" "break"; then
		RD_BREAK="premount"
		return
	fi

}

# Parse the value of the VERBOSE variable.
parse_verbose() {
	VERBOSE=""  # default
	# debian and systemd both use "quiet" to suppress verbose (INFO) logging.
	if ! _word_in_cmdline "quiet"; then
		VERBOSE="yes"; return
	fi
	# dracut uses rd.info
	if _word_in_cmdline "rd.info"; then
		VERBOSE="yes"; return
	fi
}


# ==============================================================================
# MAIN
# ==============================================================================

parse_debug
parse_rd_break
parse_verbose


# ==============================================================================
# EXPORTS
# ==============================================================================

export VERBOSE
export DEBUG
export RD_BREAK
