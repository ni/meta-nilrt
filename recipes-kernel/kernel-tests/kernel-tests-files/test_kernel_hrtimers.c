// The purpose of this test is to validate clock configuration, such that we never go lower than current CPU frequencies we have with gumstix and zynq.
// The reason is that we have had issues where the main clock in the kernel was set to 32kHz which messes with our resolution in the Execution Trace Toolkit.

// Feedback from Sankara Muthukrishnan: " With high-resolution timer enabled in panda, for instance, the difference between time-stamps is less than 800 ns
// except a few jitter-causing spikes. Without high-resolution timer, the difference between time-stamps, for instance, is ~35 us (i.e several iterations will
// give you the same time-stamp and there will be a jump by ~35 us). If our intent is to see whether high-resolution timer itself is supported or not, this
// should be sufficient."

// Ovidiu reported: "gumstix 1615, 1769; zynq 747, 750" nanoseconds between simple clock_gettime(CLOCK_MONOTONIC, ...) functions
// Therefore we decided to go with 2000ns as the limit. Watch out for gumstix, they might go over; I'm not sure (will adapt the test if needed)

// Original post: http://nitalk.natinst.com/docs/DOC-64582

#define _GNU_SOURCE

#include <limits.h>
#include <sched.h>
#include <stdio.h>
#include <time.h>
#include <assert.h>
#include <string.h>

#define NUM_LOOPS 10000000

void conf_sched()
{
	cpu_set_t mask;
	struct sched_param schedp;
	int cpu = 0; // run current process on cpu 0

	CPU_ZERO(&mask);
	CPU_SET(cpu, &mask);

	// make the test to fail if CPU affinity could not be set
	assert(-1 != sched_setaffinity(0, sizeof(mask), &mask));

	// use sched_get_priority_max() func to get the max priority of a system, on Gumstix is 99
	memset(&schedp, 0, sizeof(schedp));
	schedp.sched_priority = sched_get_priority_max(SCHED_FIFO);
	assert(0 == sched_setscheduler(0, SCHED_FIFO, &schedp));
}

long timing_delta(void)
{
	struct timespec t1, t2;
	long min_ns = LONG_MAX;
	int i;

	for (i = 0; i < NUM_LOOPS; i++) {
		clock_gettime(CLOCK_MONOTONIC, &t1);
		clock_gettime(CLOCK_MONOTONIC, &t2);
		long ns = t2.tv_nsec - t1.tv_nsec < 0 ?
			1000000000 + t2.tv_nsec - t1.tv_nsec :
			t2.tv_nsec - t1.tv_nsec;

		if (ns && (ns < min_ns)) {
			min_ns = ns;
		}
	}

	printf("# INFO %s Best timing approximation: %ldns \n", __FILE__, min_ns);
	return min_ns;
}

// We do a time delta test. If the delta is above 2000ns (watch out for gumstix, they might go over; I'm not sure)
// the test will fail.
int main(char argc, char** argv)
{
	conf_sched();
	long timing_delta_v = timing_delta();
	assert(timing_delta_v > 0);
	assert(timing_delta_v <= 2000);
	return 0;
}
