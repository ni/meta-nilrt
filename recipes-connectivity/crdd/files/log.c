/**
   Copyright (c) 2012, National Instruments Corp.

   Permission to use, copy, modify, and/or distribute this software for any
   purpose with or without fee is hereby granted, provided that the above
   copyright notice and this permission notice appear in all copies.

   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "log.h"

static FILE *log_fd = NULL;

int log_open()
{
	if (log_fd)
		return 0;

	log_fd = fopen("/dev/kmsg", "ae");
	if (log_fd == NULL)
		return -errno;

	return 0;
}

void log_close()
{
	int ret;

	if (log_fd) {
		do {
			ret = fclose(log_fd);
		} while (ret == EINTR);
		log_fd = NULL;
	}
}

int log_err(const char* format, ...)
{
	int ret;
	va_list ap;

	if (log_fd == NULL)
		return 0;

	va_start(ap, format);
	ret = vfprintf(log_fd, format, ap);
	va_end(ap);

	return ret;
}
