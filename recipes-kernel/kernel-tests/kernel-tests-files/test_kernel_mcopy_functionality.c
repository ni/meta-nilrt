#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define fail(MSG, ...) printf("FAIL: %s: " MSG "\n", __FILE__, ##__VA_ARGS__);
#define test_and_fail(STMT, MSG, ...) if (STMT) { fail(MSG, ##__VA_ARGS__); exit(1); }

int get_mcopy_nb()
{
	int __NR_mcopy = 0;
	FILE *f = 0;

	f = fopen("/sys/kernel/ni_syscall_mcopy", "r");
	test_and_fail(f == 0, "Could not open /sys/kernel/ni_syscall_mcopy\n");

	fscanf(f, "%d", &__NR_mcopy);
	test_and_fail(__NR_mcopy == 0, "Invalid value read from /sys/kernel/ni_syscall_mcopy\n");

	return __NR_mcopy;
}

int main(int argc, char** argv)
{
	int mcopy_nb;
	static int src[5] = {10, 16, 348, -26, 634188};
	int dst[5];
	int rc;

	mcopy_nb = get_mcopy_nb();

	rc = syscall(mcopy_nb, dst, src, sizeof(src));
	test_and_fail(rc != 0, "FAIL: mcopy syscall %d exited with code %d\n", mcopy_nb, rc);

	rc = memcmp(src, dst, sizeof(src));
	test_and_fail(rc != 0, "FAIL: mcopy syscall output is not identical with its input");

	return 0;
}
