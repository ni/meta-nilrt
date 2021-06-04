// SPDX-License-Identifier: LGPL-2.1-only
// Copyright (c) 2021 National Instruments.

#define _GNU_SOURCE
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <stdarg.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>
#include <sched.h>
#include <time.h>
#include <sys/types.h>
#include <sys/sysinfo.h>
#include <sys/mman.h>
#include <getopt.h>
#include <ctype.h>
#include <signal.h>

#define TEST_PRIO	98

#define NSEC_PER_USEC	1000ULL
#define NSEC_PER_SEC	1000000000ULL

#define RT_CPU_SET	"/dev/cgroup/cpuset/LabVIEW_tl_set"
#define SYSTEM_CPU_SET	"/dev/cgroup/cpuset/system_set"

static uint64_t max_latency = 0;
static uint64_t percentile_99_999 = 0;
static uint64_t percentile_99_9999 = 0;
static uint64_t test_duration = 60;

static void error_exit(char* msg)
{
	printf("error: %s\n", msg);
	printf("FAIL: nohz_test\n");
	exit(EXIT_FAILURE);
}

static void success_exit()
{
	printf("PASS: nohz_test\n");
	exit(EXIT_SUCCESS);
}

enum {
        OPT_HELP = 1,
        OPT_MAX_LATENCY,
        OPT_99_999,
        OPT_99_9999,
        OPT_DURATION,
};

static struct option opt_long[] = {
        {"max", required_argument, NULL, OPT_MAX_LATENCY},
        {"p3nines", required_argument, NULL, OPT_99_999},
        {"p4nines", required_argument, NULL, OPT_99_9999},
        {"duration", optional_argument, NULL, OPT_DURATION},
        {"help", no_argument, NULL, OPT_HELP},
        {NULL, 0, NULL, 0}
};

const char *opt_short = "m:p:q:d:h";

static void usage(char* comm)
{
        printf("Usage:\n"
               "%s <options>\n"
	       "\t-m <nS> --max=<nS>\t\tMax latency threshold in nanoseconds (required; 10ms max)\n"
	       "\t-p <nS> --p3nines=<nS>\t\t99.999%% latency threshold in nanoseconds (required)\n"
	       "\t-q <nS> --p4nines=<nS>\t\t99.9999%% latency threshold in nanoseconds (required)\n"
	       "\t-d <time> --duration=<time>\tTest duration in seconds\n"
	       "\t\t\t\t\t(m, h, d modifiers can be used to specify minutes, hours, or days)\n"
               "\t-h --help\t\t\tDisplay this help\n",
               comm);
}

static int validate_options()
{
	if (!max_latency) {
		printf("error: max latency not defined\n");
		return -1;
	}
	if (max_latency > 10000000L) {
		printf("error: max latency exceeds 10ms upper threshold\n");
		return -1;
	}
	if (!percentile_99_999) {
		printf("error: 99.999%% threshold not defined\n");
		return -1;
	}
	if (!percentile_99_9999) {
		printf("error: 99.9999%% threshold not defined\n");
		return -1;
	}
	if (percentile_99_999 >= percentile_99_9999) {
		printf("error: 99.999%% threshold value (%lu) is larger than 99.9999%% threshold value (%lu)\n",
		       percentile_99_999, percentile_99_9999);
		return -1;
	}
	if (percentile_99_9999 >= max_latency) {
		printf("error: 99.9999%% threshold value (%lu) is larger than maximum latency threshold value (%lu)\n",
		       percentile_99_9999, max_latency);
		return -1;
	}

	return 0;
}

static int parse_options(int argc, char *argv[])
{
        int c;
        int index = 0;
	char *endptr = NULL;

	if (argc < 2)
		goto err_exit;

        while ((c = getopt_long(argc, argv, opt_short, opt_long, &index)) >= 0) {
                switch (c) {
			case 'm':
			case OPT_MAX_LATENCY:
				max_latency = strtoll(optarg, &endptr, 10);
				break;
			case 'p':
			case OPT_99_999:
				percentile_99_999 = strtoll(optarg, &endptr, 10);
				break;
			case 'q':
			case OPT_99_9999:
				percentile_99_9999 = strtoll(optarg, &endptr, 10);
				break;
                        case 'd':
                        case OPT_DURATION:
                                test_duration = strtoll(optarg, &endptr, 10);
				if (optarg == endptr)
					goto err_exit;
				switch (tolower(*endptr)) {
					case 'm':
						/* minutes */
						test_duration *= 60;
						break;
					case 'h':
						/* hours */
						test_duration *= 60 * 60;
						break;
					case 'd':
						test_duration *= 60 * 60 * 24;
						/* days */
						break;
					case 0:
						/* default to seconds */
						break;
					default:
						printf("error: unrecognized modifier '%c' used for specifying the test duration\n", *endptr);
						goto err_exit;
				}
                                break;
                        case '?':
                        case 'h':
                        case OPT_HELP:
				goto err_exit;
                }
        }

	if (!validate_options())
		return 0;

  err_exit:
	usage(argv[0]);
	return -1;
}

static int write_text_file(const char *path, const char* text)
{
	FILE *f = fopen(path, "w");
	if (!f)
		return -1;
	if (fprintf(f, "%s\n", text) < 0)
		return -1;
	fclose(f);
	return 0;
}

#define CPU_SET_TASK(_SET_, _TID_) (write_text_file(_SET_ "/tasks", _TID_))
#define CPU_SET_MASK(_SET_, _MASK_) (write_text_file(_SET_ "/cpus", _MASK_))

static int setup_cpu_sets()
{
	char rt_set[8];
	char system_set[8];
	int ret = 0;

	int ncpus = get_nprocs();

	if (ncpus < 2)
		error_exit("Test requires a system with at least 2 CPUs available");

	snprintf(system_set, 8, "0-%d", ncpus - 2);
	ret = CPU_SET_MASK(SYSTEM_CPU_SET, system_set);
	if (ret < 0)
		return ret;

	/* reserve last available CPU for RT task */
	snprintf(rt_set, 8, "%d", ncpus - 1);
	ret = CPU_SET_MASK(RT_CPU_SET, rt_set);

	return ret;
}

static int set_rt_cpu_set_affinity(pid_t pid)
{
	char pid_str[12];

	snprintf(pid_str, 12, "%u", pid);
	return CPU_SET_TASK(RT_CPU_SET, pid_str);
}

static int set_fifo_priority(int prio)
{
	struct sched_param schedp;

	memset(&schedp, 0, sizeof(schedp));
	schedp.sched_priority = prio;
	return sched_setscheduler(0, SCHED_FIFO, &schedp);
}

static int flush_disk_io()
{
	int ret;

	ret = write_text_file("/proc/sys/vm/drop_caches", "3");
	sync();
	return ret;
}

static void setup()
{
        signal(SIGHUP, SIG_IGN);

	if (mlockall(MCL_CURRENT | MCL_FUTURE) < 0)
		error_exit("Failed to mlockall memory");

	if (setup_cpu_sets() < 0)
		error_exit("Failed to configure CPU sets");

	if (set_rt_cpu_set_affinity(getpid()) < 0)
		error_exit("Failed to affinitize test to CPU set");

	if (set_fifo_priority(TEST_PRIO) < 0)
		error_exit("Failed to set the test scheduling priority");

	if (flush_disk_io() < 0)
		error_exit("Failed to flush disk caches");
}

static inline uint64_t tsdiff(struct timespec *start, struct timespec *end)
{
	uint64_t t1 = (uint64_t)(start->tv_sec) * NSEC_PER_SEC +
		start->tv_nsec;
	uint64_t t2 = (uint64_t)(end->tv_sec) * NSEC_PER_SEC +
		end->tv_nsec;

	return t2 - t1;
}

static void check_percentiles(uint64_t *data, uint64_t data_sz, uint64_t total_count)
{
	uint64_t i;
	uint64_t sum;
	double p_99_999;
	double p_99_9999;

	p_99_999 = (double)total_count * 0.99999;
	for (i = 0, sum = 0; i < data_sz; i++) {
		sum += data[i];
		if (sum >= p_99_999)
			break;
	}
	printf("99.999%%: %lu ns\n", i);
	if (i > percentile_99_999)
		error_exit("99.999%% threshold exceeded");

	p_99_9999 = (double)total_count * 0.999999;
	/* the 99.9999% treshold is always greather than the 99.999%
	 * treshold so we can continue where we've left off */
	for (i=i+1; i < data_sz; i++) {
		sum += data[i];
		if (sum >= p_99_9999)
			break;
	}
	printf("99.9999%%: %lu ns\n", i);
	if (i > percentile_99_9999)
		error_exit("99.9999%% threshold exceeded");
}

static void test(uint64_t duration, bool warmup)
{
	static uint64_t *hist_data;
	static uint64_t hist_cnt;
	struct timespec prev_ts;
	struct timespec ts;
	time_t end_sec;
	uint64_t max_dt;
	uint64_t dt;

	max_dt = 0;
	hist_cnt = 0;
	hist_data = (uint64_t*)calloc(max_latency, sizeof(uint64_t));
	if (!hist_data)
		error_exit("Failed to allocate space for histogram data");

	clock_gettime(CLOCK_MONOTONIC, &prev_ts);
	end_sec = prev_ts.tv_sec + test_duration;

	while (prev_ts.tv_sec < end_sec) {
		clock_gettime(CLOCK_MONOTONIC, &ts);
		dt = tsdiff(&prev_ts, &ts);

		if (dt > max_dt)
			max_dt = dt;
		if (!warmup) {
			if (dt >= max_latency) {
				printf("maximum latency: %lu ns\n", dt);
				error_exit("Maximum latency exceeded");
			}
			hist_data[dt]++;
		}
		hist_cnt++;
		prev_ts = ts;
	}

	if (!warmup) {
		printf("Maximum latency: %lu ns\n", max_dt);
		check_percentiles(hist_data, max_latency, hist_cnt);
	}
	free(hist_data);
}

int main(int argc, char *argv[])
{
	if (parse_options(argc, argv) < 0)
		error_exit("Failed to parse arguments\n");

	setup();

	/* 60 seconds warm-up */
	test(60, true);

	test(test_duration, false);

	success_exit();
}
