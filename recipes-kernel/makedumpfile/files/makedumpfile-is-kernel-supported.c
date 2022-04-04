// SPDX-License-Identifier: GPL-2.0-or-later
/*
 * makedumpfile-is-kernel-supported.c
 *
 * makedumpfile has a supported kernel version range, but doesn't expose a
 * way to check the current kernel version against it... so this simple
 * executable sucks in the header file in order to do so.
 */

#include <errno.h>
#include <limits.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/utsname.h>

// get KVER_MAJ_SHIFT, KVER_MIN_SHIFT, KERNEL_VERSION, OLDEST_VERSION, LATEST_VERSION:
#include "makedumpfile.h"

// inverse of the KERNEL_VERSION() macro
#define VERSION_FMT "%d.%d.%d"
#define VERSION_SPLIT(x) \
	((x) >> KVER_MAJ_SHIFT) & ((1 << (32 - KVER_MAJ_SHIFT)) - 1), \
	((x) >> KVER_MIN_SHIFT) & ((1 << (KVER_MAJ_SHIFT - KVER_MIN_SHIFT)) - 1), \
	(x) & ((1 << KVER_MIN_SHIFT) - 1)

#define PTEST_NAME "makedumpfile-is-kernel-supported"

void ptest_pass() {
	printf("PASS: %s\n", PTEST_NAME);
	exit(0);
}

void ptest_fail() {
	printf("FAIL: %s\n", PTEST_NAME);
	exit(1);
}

bool get_kernel_version(char *release, uint32_t *version) {
	unsigned long maj, min, rel;
	char *start, *end;

	*version = 0;

	start = release;
	maj = strtol(start, &end, 10);
	if (maj == LONG_MAX)
		return false;

	start = end + 1;
	min = strtol(start, &end, 10);
	if (min == LONG_MAX)
		return false;

	start = end + 1;
	rel = strtol(start, &end, 10);
	if (rel == LONG_MAX)
		return false;

	*version = KERNEL_VERSION(maj, min, rel);
	return true;
}

int main(int argc, char *argv[])
{
	struct utsname utsname;
	uint32_t version;
	int err;

	if (uname(&utsname)) {
		err = errno;
		printf("ERROR: uname() failed to retrieve kernel information: %s\n",
			strerror(err));
		ptest_fail();
	}

	if (!get_kernel_version(utsname.release, &version)) {
		printf("ERROR: failure parsing kernel version '%s'\n",
			utsname.release);
		ptest_fail();
	}

	if ((version < OLDEST_VERSION) || (LATEST_VERSION < version)) {
		printf("ERROR: kernel version " VERSION_FMT " not within supported range [" VERSION_FMT " .. " VERSION_FMT "]\n",
			VERSION_SPLIT(version),	VERSION_SPLIT(OLDEST_VERSION),
			VERSION_SPLIT(LATEST_VERSION));
		ptest_fail();
	}

	ptest_pass();
}
