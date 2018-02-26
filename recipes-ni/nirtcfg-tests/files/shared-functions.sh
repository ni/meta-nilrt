#! /bin/bash
# This file contains common functions for driving ``nirtcfg`` calls.


# NIRTCFG WRAPPERS #
####################

# Prints debugging information about the lest ``nircfg`` call to stderr
function nirtcfg_debug_output () {
	printf "<args>%s</args>\n" "$nirtcfg_args" >&2
	printf "<return_code>%d</return_code>\n" >&2
	printf "<stdout>%s</stdout>\n" "$nirtcfg_stdout" >&2
	printf "<stderr>%s</stderr>\n" "$nirtcfg_stderr" >&2
}

# Calls ``nirtcfg`` and stores the relevant output information into temporary
# files in the current working directory
function nirtcfg_store_output () {
	nirtcfg_args="$@"
	echo "" >./nirtcfg_stdout
	echo "" >./nirtcfg_stderr
	echo "" >./nirtcfg_rc

	# squash bash stderr in the nirtcfg call to quench messages about SIGABRTs
	# being thrown. Once CAR 390073 is fixed, remove the last 2>/dev/null.
	$(bash -c "nirtcfg $nirtcfg_args 1>./nirtcfg_stdout 2>./nirtcfg_stderr" \
		2>/dev/null)
	echo $? >./nirtcfg_rc

	IFS= read -d  -r nirtcfg_stdout <./nirtcfg_stdout
	IFS= read -d  -r nirtcfg_stderr <./nirtcfg_stderr
	nirtcfg_rc=$(cat ./nirtcfg_rc)
}


# EXPECTS #
###########

# Calls ``nirtcfg`` with the arguments specifed by ${@:2}, stores its output,
# evaluates the ``test`` arguments provided by $1, prints debugging information
# if the call fails the conditional, and returns the success result.
# 1: ``test`` conditional string (should be single quoted, can use variable
#    substitution)
# 2+: Arguments to pass to ``nirtcfg``
function nirtcfg_assert () {
	args="${@:2}"
	nirtcfg_store_output $args

	if `eval test ${1}`; then
		return 0
	else
		printf "<nirtcfg_args>%s</nirtcfg_args>\n" "$args"
		nirtcfg_debug_output
		ptest_fail
		return 1
	fi
}

# Calls ``nirtcfg`` with the arguments specifed by ${@:2}, stores its output,
# evaluates the ``test`` arguments provided by $1 against whitespace-trimmed
# nirtcfg output, prints debugging information if the call fails the
# conditional, and returns the success result.
# 1: ``test`` conditional string (should be single quoted, can use variable
#    substitution)
# 2+: Arguments to pass to ``nirtcfg``
function nirtcfg_assert_trws () {
	args="${@:2}"
	nirtcfg_store_output $args

	read nirtcfg_stdout <<<"$nirtcfg_stdout"  # trim whitespace
	read nirtcfg_stderr <<<"$nirtcfg_stderr"  # trim whitespace
	if `eval test ${1}`; then
		return 0
	else
		printf "<nirtcfg_args>%s</nirtcfg_args>\n" "$args"
		nirtcfg_debug_output
		ptest_fail
		return 1
	fi
}


function nirtcfg_assert_pass () {
	nirtcfg_assert '${nirtcfg_rc} -eq 0' $@ && return 0 || return 1
}

function nirtcfg_assert_fail () {
	nirtcfg_assert '${nirtcfg_rc} -ne 0' $@ && return 0 || return 1
}

function nirtcfg_assert_stderr () {
	nirtcfg_assert '-n "${nirtcfg_stderr}"' $@ && return 0 || return 1
}

function nirtcfg_assert_no_stderr () {
	nirtcfg_assert '-z "${nirtcfg_stderr}"' $@ && return 0 || return 1
}

function nirtcfg_assert_stdout () {
	nirtcfg_assert '-n "${nirtcfg_stdout}"' $@ && return 0 || return 1
}

function nirtcfg_assert_no_stdout () {
	nirtcfg_assert '-z "${nirtcfg_stdout}"' $@ && return 0 || return 1
}

function nirtcfg_assert_any_value () {
	nirtcfg_assert_trws '${nirtcfg_rc} -eq 0 -a -n "${nirtcfg_stdout}"' $@ \
		&& return 0 || return 1
}

function nirtcfg_assert_value () {
	value="$1"
	args="${@:2}"

	nirtcfg_assert_trws \
		'$nirtcfg_rc -eq 0 -a "$nirtcfg_stdout" = "$value"' \
		$args \
		&& return 0 || return 1
}

function nirtcfg_assert_no_value () {
	nirtcfg_assert_trws '${nirtcfg_rc} -eq 0 -a -z "${nirtcfg_stdout}"' $@ \
		&& return 0 || return 1
}
