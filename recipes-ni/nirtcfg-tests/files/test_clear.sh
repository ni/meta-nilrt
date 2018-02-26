#!/bin/bash
# Exercises nirtcfg --clear operations.

source $(dirname "$0")/ptest-format.sh      # for ptest_ functions.
source $(dirname "$0")/shared-functions.sh  # for nirtcfg_ functions.

ptest_test=$(basename "$0" ".sh")  # test_clear


# SETUP #
#########

declare PTEST_ROOT=$(realpath $(dirname "$0"))

# SETUP TEMPDIR
if [ -z "${nirtcfg_tempdir}" ]; then
	source ./setup.sh
fi
cd "${nirtcfg_tempdir}"
mkdir "clear"
cd "./clear"
# END SETUP TEMPDIR


# TESTS #
#########

# Test that operations which are compatible with --clear are allowed to proceed.
function test_clear_compatible_operations () {
	ptest_pass
	# All of the following should *PASS*
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# clear & clear
	nirtcfg_assert_pass --file ./config.ini \
		--clear section=test2,token=tok21 \
		--clear section=test2,token=tok22
	# clear & get
	nirtcfg_assert_pass --file ./config.ini \
		--clear section=test2,token=tok23 \
		--get section=test1,token=tok24
	# clear & set
	nirtcfg_assert_pass --file ./config.ini \
		--clear section=test2,token=tok24 \
		--set section=test2,token=tok25,value=testvalue
	# clear & rm-if-empty
	nirtcfg_assert_pass --file ./config.ini \
		--clear section=test1,token=tok1 \
		--rm-if-empty
}

# Test that operations that are incompatible with --clear are rejected.
function test_clear_incompatible_operations () {
	ptest_pass
	# all of the following functions should fail
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# clear & list
	nirtcfg_assert_fail --file ./config.ini \
		--clear section=test1,token=tok1 \
		--list
	# clear & migrate
	nirtcfg_assert_fail --file ./config.ini \
		--clear section=test1,token=tok1 \
		--migrate
}

# Clear the last item from a section and validate that it also clears the
# section header.
function test_clear_last_from_section () {
	ptest_pass
	#cp "${PTEST_ROOT}/configs/clear_section.ini" ./config.ini
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	nirtcfg_assert_pass --file ./config.ini --clear section=test1,token=tok1

	# Expect that no [test1] variables are in the file.
	variables=$(nirtcfg --file ./config.ini --list)
	if [ $(grep -e "\[test1\]" <<<"$variables") ]; then
		echo "ERROR: token not removed from config file." >&2
		ptest_fail
	fi

	# Expect that the [test1] header is not in the file.
	if [ $(grep -e "\[test1\]" ./config.ini) ]; then
		echo "ERROR: [test1] section not remove from backing file." >&2
		ptest_fail
	fi
}

# Test that clearing a value from a file that does not exist: passes and
# returns no stdout.
function test_clear_file_dne () {
	ptest_pass
	rm -f ./config.ini

	nirtcfg_assert_no_stdout --file ./config.ini --clear \
		section=something,token=foo
}

# Test that clearing a token from a file that exists, with that token present,
# succeeds and that the token is actually removed.
function test_clear_normal () {
	ptest_pass
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	nirtcfg_assert_pass --file ./config.ini --clear section=test2,token=tok21
	variables=$(nirtcfg --file ./config.ini --list)
	if [ $(grep -e "\[test2\]tok21" <<<"$variables") ]; then
		echo "ERROR: [test2]tok21 not removed from backing file." >&2
		ptest_fail
	fi
}


# MAIN #
########

ptest_change_subtest 0 "normal operation"
test_clear_normal
ptest_report

ptest_change_subtest 1 "compatible operations"
test_clear_compatible_operations
ptest_report

ptest_change_subtest 2 "incompatible operations"
test_clear_incompatible_operations
ptest_report

ptest_change_subtest 3 "file does not exist"
test_clear_file_dne
ptest_report

ptest_change_subtest 4 "clear last from section"
test_clear_last_from_section
ptest_report

exit $ptest_rc
