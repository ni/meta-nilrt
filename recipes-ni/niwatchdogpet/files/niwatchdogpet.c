/*
 *  NI Watchdog Petter
 *  Copyright 2014 National Instruments Corporation.
 */

#define _POSIX_SOURCE 1
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/ioctl.h>
#include <linux/types.h>
#include <stdint.h>
#include <stdio.h>


/* It'd be cool to get this (and the watchdog name) from our Linux headers,
 * "linux/niwatchdog.h", but we can't yet do that from our OpenEmbedded build.
 * Until then, we can ship this hack, since we don't expect to keep this petter
 * utility around for any longer than we don't have a way to pet the watchdog
 * through a device attribute. */
#define NIWATCHDOG_IOCTL_PET			_IOR('W', 6, __u32)
#define NIWATCHDOG_NAME	"niwatchdog"

int main(int argc, char **argv)
{
	int fd;
	int result;
	uint32_t state;

	fd = open("/dev/" NIWATCHDOG_NAME, 0);
	if (fd < 0) {
		fprintf(stderr, "Could not open watchdog device.\n");
		return -1;
	}

	result = ioctl(fd, NIWATCHDOG_IOCTL_PET, &state);

	return result;
}
