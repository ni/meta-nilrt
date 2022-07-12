// SPDX-License-Identifier: MIT
/*
 * rwlockbomb.c
 *
 * gcc -pthread rwlockbomb.c -o rwlockbomb
 *
 * test to exercise PI with glibc rwlocks on PREEMPT_RT kernels
 */

#define _GNU_SOURCE
#include <stdatomic.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <pthread.h>
#include <err.h>
#include <errno.h>
#include <sched.h>
#include <time.h>

#define NSEC_PER_USEC	1000ULL
#define NSEC_PER_SEC	1000000000ULL
#define TEST_LENGTH_SEC	10

static pthread_barrier_t barrier_start;
static pthread_barrier_t barrier_finish;
static pthread_rwlock_t rwlock;
static atomic_int *thread_counter;
static atomic_int *thread_done;

static void set_priority(int policy, int prio)
{
	struct sched_param schedp;

	memset(&schedp, 0, sizeof(schedp));
	schedp.sched_priority = prio;
	if (sched_setscheduler(0, policy, &schedp) < 0)
		err(1, "Failed to set the test thread priority");
}

static unsigned long long tsdiff(struct timespec *start, struct timespec *end)
{
	unsigned long long t1 =
		(unsigned long long)(start->tv_sec) * NSEC_PER_SEC +
		start->tv_nsec;
	unsigned long long t2 =
		(unsigned long long)(end->tv_sec) * NSEC_PER_SEC +
		end->tv_nsec;

	return t2 - t1;
}

static void do_busy_work(unsigned long long nsec)
{
	struct timespec start_ts;
	struct timespec ts;

	clock_gettime(CLOCK_MONOTONIC, &start_ts);
	do {
		clock_gettime(CLOCK_MONOTONIC, &ts);
	} while (tsdiff(&start_ts, &ts) < nsec);
}

static void* lopri_thread(void *param)
{
	int thread_count = *(int *)param;
	int *counters = NULL;
	int threads_not_done = 1;
	int i = 0;

	free(param);

	counters = reallocarray(NULL, thread_count, sizeof(atomic_int));
	set_priority(SCHED_OTHER, 0);
	pthread_barrier_wait(&barrier_start);

	while (threads_not_done) {
		pthread_rwlock_wrlock(&rwlock);
		do_busy_work(100ULL);
		pthread_rwlock_unlock(&rwlock);
		do_busy_work(300ULL);

		/*
		 * check in on the higher priority threads from time to time.
		 *
		 * since they're higher priority than us, and rwlock is not
		 * locked anymore, then the threads _should_ be making progress.
		 *
		 * Note: With the Linux kernel default of /proc/sys/kernel/sched_rt_runtime_us
		 * of 950000 (which puts all realtime tasks in a 0.95 s window leaving
		 * 0.05 s free for non-real-time tasks), it is expected that realtime
		 * tasks will not make progress in that window.
		 */
		if ((++i % 4096) == 0) {
			threads_not_done = 0;
			for (int th = 1; th < thread_count; ++th) {
				int thread_is_done = atomic_load(&thread_done[th]) == 1;
				int newval = atomic_load(&thread_counter[th]);

				if (!thread_is_done) {
					if (counters[th] == newval) {
						printf("thread %d seems stuck (nv=%d), are we throttled?\n", th, newval);
						fflush(stdout);
					}
					counters[th] = newval;

					threads_not_done = 1;
				}
			}
		}
	}

	free(counters);

	printf("low priority thread done\n");
	pthread_barrier_wait(&barrier_finish);

	return NULL;
}

static void* hipri_thread(void *param)
{
	struct timespec start_ts;
	struct timespec ts;
	int thread_index = *(int *)param;

	free(param);

	atomic_store(&thread_done[thread_index], 0);
	set_priority(SCHED_FIFO, 59);
	pthread_barrier_wait(&barrier_start);

	clock_gettime(CLOCK_MONOTONIC, &start_ts);

	do {
		atomic_fetch_add(&thread_counter[thread_index], 1);

		pthread_rwlock_wrlock(&rwlock);
		do_busy_work(100ULL);
		pthread_rwlock_unlock(&rwlock);

		clock_gettime(CLOCK_MONOTONIC, &ts);
	} while (tsdiff(&start_ts, &ts) < TEST_LENGTH_SEC*NSEC_PER_SEC);

	printf("high priority thread %d done\n", thread_index);
	atomic_store(&thread_done[thread_index], 1);

	set_priority(SCHED_OTHER, 0);

	pthread_barrier_wait(&barrier_finish);

	return NULL;
}

int main(int argc, char *argv[])
{
	int i;
	int thread_count;
	int barrier_count;
	int *intval;
	char thname[16] = {0};
	pthread_t *threads;

	thread_count = sysconf(_SC_NPROCESSORS_ONLN)*2 + 1;
	barrier_count = thread_count + 1;
	threads = reallocarray(NULL, thread_count, sizeof(pthread_t));
	thread_counter = reallocarray(NULL, thread_count, sizeof(atomic_int));
	thread_done = reallocarray(NULL, thread_count, sizeof(atomic_int));
	if (!threads)
		err(1, "couldn't allocate thread handles");
	if (pthread_rwlock_init(&rwlock, NULL))
		err(1, "Failed to create rwlock");
	if (pthread_barrier_init(&barrier_start, NULL, barrier_count))
		err(1, "Failed to create barrier");
	if (pthread_barrier_init(&barrier_finish, NULL, barrier_count))
		err(1, "Failed to create barrier");

	intval = malloc(sizeof(int));
	if (!intval)
		err(1, "couldn't allocate memory for thread param");
	*intval = thread_count;
	if (pthread_create(&threads[0], NULL, lopri_thread, intval) != 0)
		err(1, "Failed to create lopri thread");
	pthread_setname_np(threads[0], "lopri");

	for (i = 1; i < thread_count; ++i) {
		intval = malloc(sizeof(int));
		if (!intval)
			err(1, "couldn't allocate memory for thread param");
		*intval = i;
		if (pthread_create(&threads[i], NULL, hipri_thread, intval) != 0)
			err(1, "Failed to create hipri thread");
		sprintf(thname, "hipri_%03d", i);
		pthread_setname_np(threads[i], thname);
	}

	/* all threads are started, kick them off */
	pthread_barrier_wait(&barrier_start);

	pthread_barrier_wait(&barrier_finish);

	for (i = 0; i < thread_count; ++i)
	{
		/* wait for thread completion */
		pthread_join(threads[i], NULL);
	}
	printf("finish\n");
	return 0;
}
