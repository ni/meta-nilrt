//This test verifies that there are no page faults after using mlockall(MCL_CURRENT|MCL_FUTURE),
//so there is no need to pre-touch the thread's stack after calling mlockall.

#include <pthread.h>
#include <sys/mman.h>
#include <sys/resource.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

const int ITERATIONS=10;
//64 MB stack:
#define STACKSIZE (1048576 * 64)
#define ARRAYLEN (STACKSIZE - 65536)
#define DEBUG_PRINTF 0

#define fail(MSG, ...) printf("FAIL: %s: " MSG "\n", __FILE__, ##__VA_ARGS__);
#define test_and_fail(STMT, MSG, ...) if (STMT) { fail(MSG, ##__VA_ARGS__); exit(1); }

typedef struct
{
	bool doStackTouch;
	bool doMLock;
	bool pageFaults;
} thread_data_t;

static long threadPageFaults()
{
	struct rusage usage;
	test_and_fail(getrusage(RUSAGE_THREAD, &usage) != 0, "Error getting resource usage!");
	return usage.ru_minflt + usage.ru_majflt;
}

static void innerproc()
{
	volatile char buf[ARRAYLEN];
	for (int i=ARRAYLEN; i>0; i -= 4096)
	{
		buf[i-1] = 0;
	}
}

static void preTouchStack()
{
	innerproc();
}

static void* proc(void* thread_args)
{
	thread_data_t *args = (thread_data_t*) thread_args;

	bool doStackTouch = args->doStackTouch;
	bool doMLock = args->doMLock;

	if (doMLock)
	{
		//who is killing the thread?
		test_and_fail(mlockall(MCL_CURRENT|MCL_FUTURE), "Error locking pages");
	}
	if (doStackTouch)
		preTouchStack();

	long pages0, pages1;

	pages0 = threadPageFaults();
	innerproc();
	pages1 = threadPageFaults();
	args->pageFaults = pages1 - pages0;

#if DEBUG_PRINTF
	std::cout << "Faults: "<< args->pageFaults << " ";
#endif

	if (doMLock)
		munlockall();

	return 0;
}

static void runIteration(bool doStackTouch, bool doMLock)
{
	pthread_t test_thread;
	pthread_attr_t attr;
	thread_data_t thread_args;
	int err=0;

	thread_args.doStackTouch = doStackTouch;
	thread_args.doMLock = doMLock;
	pthread_attr_init(&attr);
	pthread_attr_setstacksize(&attr, STACKSIZE);

	err = pthread_create(&test_thread, &attr, proc, &thread_args );
	if (err)
	{
		printf("#Error: pthread_create returned code %d\nFAIL:stack_touch_test\n", err);
		exit(1);
	}

	err = pthread_join(test_thread, NULL);
	if (err)
	{
		printf("#Error: pthread_join returned code %d\nFAIL:stack_touch_test\n", err);
		exit(1);
	}
	//In case of touching the stack or using mlock(..) we expect to have 0 page
	//faults. If no mlock(..) or stack touch is made, then we expect to have more
	//than 0 page faults.
	if(doStackTouch || doMLock)
	{
		test_and_fail(thread_args.pageFaults != 0, "page fault when touching stack or using mlock");
	}
	else
	{
		test_and_fail(thread_args.pageFaults = 0, "no page fault without mklock and stack touch");
	}
}

static void runConfiguration(bool doStackTouch, bool doMLock)
{
#if DEBUG_PRINTF
	std::cout << "Configuration: doStackTouch=" << doStackTouch << " doMLock=" << doMLock
		<< std::endl << "===" << std::endl;
#endif
	for (int i=0; i<ITERATIONS; i++)
	{
		runIteration(doStackTouch, doMLock);
	}
#if DEBUG_PRINTF
	std::cout << std::endl << std::endl;
#endif
}

int main(int argc, char** argv)
{
	runConfiguration(false, false);
	runConfiguration(false, true);
	runConfiguration(true, false);
	runConfiguration(true, true);
}
