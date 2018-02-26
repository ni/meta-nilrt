#!/bin/bash
# Exercises nirtcfg --rm-if-empty operations.

source $(dirname "$0")/ptest-format.sh      # for ptest_ functions.
source $(dirname "$0")/shared-functions.sh  # for nirtcfg_ functions.

ptest_test=$(basename "$0" ".sh")  # test_rm-if-empty


# SETUP #
#########

declare PTEST_ROOT=$(realpath $(dirname "$0"))

# SETUP TEMPDIR
if [ -z "${nirtcfg_tempdir}" ]; then
	source ./setup.sh
fi
cd "${nirtcfg_tempdir}"
mkdir "rm"
cd "./rm"
# END SETUP TEMPDIR


# TESTS #
#########

# Test that operations which are compatible with --rm are allowed to proceed.
function test_rm_compatible_operations () {
	ptest_pass
	# All of the following should *PASS*
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# rm-if-empty & clear
	nirtcfg_assert_pass --clear section=test1,token=tok1 --rm-if-empty

	# rm-if-empty & rm-if-empty
	nirtcfg_assert_pass --rm-if-empty --rm-if-empty
}

# Test that operations that are incompatible with --rm are rejected.
function test_rm_incompatible_operations () {
	ptest_pass
	# all of the following functions should fail
	cp "${PTEST_ROOT}/configs/base_config.ini" ./config.ini

	# rm-if-empty & get
	nirtcfg_assert_fail --file config.ini \
		--get section=test1,token=tok1,value=bar --rm-if-empty

	# rm-if-empty & set
	nirtcfg_assert_fail --file config.ini \
		--set section=test1,token=tok1,value=bar --rm-if-empty

	# rm-if-empty & list
	nirtcfg_assert_fail --file ./config.ini --rm-if-empty --list

	# rm-if-empty & migrate
	nirtcfg_assert_fail --file ./config.ini --rm-if-empty --migrate
}


# Test that calling rm-if-empty on an empty file will remove it.
test_rm_alone_size_0 () {
	printf -- '' >./config.ini
	ptest_pass

	nirtcfg_assert_pass --file ./config.ini --rm-if-empty
	if [ -e "./config.ini" ]; then
		echo "ERROR: Expected rm-if-empty operation to remove backing file." >&2
		echo "ERROR: It did not." >&2
		ptest_fail
	fi
}

# Test that calling rm-if-empty on a file that contains only whitespace
# characters (itc: ' ', '\t', '\n') will remove the file.
test_rm_alone_whitespace () {
	cat <<EOF >./config.ini

	
      
EOF
	ptest_pass

	nirtcfg_assert_pass --file ./config.ini --rm-if-empty
	if [ -e "./config.ini" ]; then
		echo "ERROR: Expected rm-if-empty operation to remove backing file." >&2
		echo "ERROR: It did not." >&2
		ptest_fail
	fi
}

# Test that calling rm-if-empty on a file that contains non-whitespace
# characters will not remove the file.
test_rm_alone_not_empty () {
	cat <<EOF >./config.ini
This is some test text.

asldfkjasdf
EOF
	ptest_pass

	nirtcfg_assert_pass --file ./config.ini --rm-if-empty
	if [ ! -e "./config.ini" ]; then
		echo "ERROR: Expected rm-if-empty operation to keep the backing file." >&2
		echo "ERROR: It was removed instead." >&2
		ptest_fail
	fi
}

# Test that calling ``--clear <last section in file> --rm-if-empty`` will clear
# the final section token and remove the backing file.
test_rm_clear_last () {
	cat <<EOF >./config.ini
[testsection]
foo=bar

EOF
	ptest_pass

	nirtcfg_assert_pass --file ./config.ini \
		--clear section=testsection,token=foo --rm-if-empty

	if [ -e "./config.ini" ]; then
		echo "ERROR: Expected rm-if-empty operation to remove backing file." >&2
		echo "ERROR: It did not." >&2
		ptest_fail
	fi
}


# Test that calling ``--clear <non-last section in file> --rm-if-empty`` will
# not result in the backing file being removed. (We test the actual result of
# the ``--clear`` operation in :test_clear.sh)
test_rm_clear_not_last () {
	cat <<EOF >./config.ini
[testsection]
foo=bar
another=value

EOF
	ptest_pass

	nirtcfg_assert_pass --file ./config.ini \
		--clear section=testsection,token=foo --rm-if-empty

	if [ ! -e "./config.ini" ]; then
		echo "ERROR: Expected rm-if-empty operation to keep the backing file." >&2
		echo "ERROR: It was removed instead." >&2
		ptest_fail
	fi
}

# Test that calling ``--rm-if-empty`` on a backing file that does not exist
# will return code 0.
test_rm_file_dne () {
	rm -f ./config.ini

	ptest_pass
	nirtcfg_assert_pass --file ./config.ini --rm-if-empty
}


# MAIN #
########

ptest_change_subtest 0 "compatible operations"
test_rm_compatible_operations
ptest_report

ptest_change_subtest 1 "incompatible operations"
test_rm_incompatible_operations
ptest_report

ptest_change_subtest 2 "standalone call - empty (size 0)"
test_rm_alone_size_0
ptest_report

ptest_change_subtest 3 "standalone call - empty (whitespace)"
test_rm_alone_whitespace
ptest_report

ptest_change_subtest 4 "standalone call - not empty"
test_rm_alone_not_empty
ptest_report

ptest_change_subtest 5 "clear last entry"
test_rm_clear_last
ptest_report

ptest_change_subtest 6 "clear non-last entry"
test_rm_clear_not_last
ptest_report

ptest_change_subtest 7 "file does not exist"
test_rm_file_dne
ptest_report

exit $ptest_rc
