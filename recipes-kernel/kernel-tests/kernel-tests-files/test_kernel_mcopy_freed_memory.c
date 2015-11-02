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

/* Note that this test may return false negatives; even after the free, if
 * libc has not returned the memory back to the system, an access to it may
 * still not crash, even if accessed without the mcopy system call */
int main(int argc, char** argv)
{
	int mcopy_nb = 0;
	int test_array[5] = {1, 2, 3, 4, 5};
	int *src = NULL;
	int dst[5];
	int rc;

	mcopy_nb = get_mcopy_nb();

	src = (int *) malloc(5 * sizeof(int));
	test_and_fail(src == 0, "FAIL: Could not allocate src buffer (malloc)\n");

	memcpy(src, test_array, sizeof(test_array));
	free(src);
	rc = syscall(mcopy_nb, dst, src, sizeof(src));

	// we expect the above syscall to fail
	test_and_fail(rc == 0, "FAIL (possible false negative): syscall mcopy exited succesfully when expected to fail\n");

	return 0;
}
