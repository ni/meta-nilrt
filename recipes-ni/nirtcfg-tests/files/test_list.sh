#!/bin/bash

source $(dirname "$0")/ptest-format.sh      # for ptest_ functions.
source $(dirname "$0")/shared-functions.sh  # for nirtcfg_ functions.

ptest_test=$(basename "$0" ".sh")  # test_list


# SETUP #
#########

declare PTEST_ROOT=$(realpath $(dirname "$0"))

# SETUP TEMPDIR
if [ -z "${nirtcfg_tempdir}" ]; then
	source ./setup.sh
fi
cd "${nirtcfg_tempdir}"
mkdir "list"
cd "./list"
# END SETUP TEMPDIR

function count_tokens () {
	if [ -z "$1" ]; then
		echo "ERROR: no token values found." >&2
		ptest_fail
	fi

	declare -i num_values=$(wc -l <<<"$1")
	if [ $num_values -ne 6 ]; then
		echo "ERROR: inavlid number of tokens found; should be 6." >&2
		echo "$1" >&2
		ptest_fail
	fi
}


# TESTS #
#########

function test_list_compatible_operations () {
	ptest_pass
	# All of the following should *PASS*
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# list && another list
	nirtcfg_assert_pass --file config.ini --list --list
}

function test_list_incompatible_operations () {
	ptest_pass
	# all of the following functions should fail
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# list & set
	nirtcfg_assert_fail --file config.ini --list \
		--set section=wrong,token=something,value=else
	# list & get
	nirtcfg_assert_fail --file ./config.ini --list \
		--get section=test1,token=tok1
	# list & clear
	nirtcfg_assert_fail --file ./config.ini --list \
		--clear section=test1,token=tok1
	# list & migrate
	nirtcfg_assert_fail --file ./config.ini --list --migrate
	# list & rm-if-empty
	nirtcfg_assert_fail --file ./config.ini --list --rm-if-empty
}

# Test that we can reat the normal (well-formed) config file
function test_list_normal () {
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini
	list=$(nirtcfg --file ./config.ini --list)

	ptest_pass

	if [ -z "$list" ]; then
		echo "ERROR: no token values found." >&2
		ptest_fail
	fi

	declare -i num_values=$(wc -l <<<"$list")
	if [ $num_values -ne 6 ]; then
		echo "ERROR: invalid number of tokens found; should be 6." >&2
		echo "$list" >&2
		ptest_fail
	fi
}

# Test that ``list`` will section headers that contain non-alphanum ASCII
# characters.
function test_list_section_names () {
	cp "${PTEST_ROOT}/configs/list_section-names.ini" ./config.ini
	list=$(nirtcfg --file ./config.ini --list)

	ptest_pass

	declare -i num_values=$(grep "^\[test.ing\]" <<<"$list" | wc -l)
	if [ $num_values -ne 32 ]; then
		echo "ERROR: Incorrect number of section names read." >&2
		echo "$list" >&2
		ptest_fail
	fi
}


# MAIN #
########

ptest_change_subtest 0 "normal operation"
test_list_normal
ptest_report

ptest_change_subtest 1 "compatible operations"
test_list_compatible_operations
ptest_report

ptest_change_subtest 2 "incompatible operations"
test_list_incompatible_operations
ptest_report

ptest_change_subtest 3 "read section names"
test_list_section_names
ptest_report

exit $ptest_rc
