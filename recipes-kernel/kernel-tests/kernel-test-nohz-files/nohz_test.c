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
#include <linux/limits.h>

#define TEST_PRIO	1

#define NSEC_PER_USEC	1000ULL
#define NSEC_PER_SEC	1000000000ULL

#define LOG_DIR	"/var/local/ptest-results/kernel-test-nohz"

static uint64_t max_latency = 0;
static uint64_t percentile_99_999 = 0;
static uint64_t percentile_99_9999 = 0;
static uint64_t test_duration = 60;

struct histogram_data {
	uint64_t cnt;
	uint64_t max;
	uint64_t size;
	uint64_t *data;
};

static void error_exit(const char* error, const char *format, ...)
{
	va_list arglist;

	printf("error: %s\n", error);
	if (format) {
		va_start(arglist, format);
		vprintf(format, arglist);
		va_end(arglist);
	}
	printf("FAIL: nohz_test\n");
	exit(EXIT_FAILURE);
}

static void success_exit(const char *format, ...)
{
	va_list arglist;

	if (format) {
		va_start(arglist, format);
		vprintf(format, arglist);
		va_end(arglist);
	}
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

#define RT_CPU_SET	"/dev/cgroup/cpuset/LabVIEW_tl_set"
#define SYSTEM_CPU_SET	"/dev/cgroup/cpuset/system_set"
#define CPU_SET_TASK(_SET_, _TID_) (write_text_file(_SET_ "/tasks", _TID_))
#define CPU_SET_MASK(_SET_, _MASK_) (write_text_file(_SET_ "/cpus", _MASK_))

static int cgroup_v1_setup(int ncpus)
{
	char rt_set[8];
	char system_set[8];
	int ret = 0;

	snprintf(system_set, 8, "0-%d", ncpus - 2);
	ret = CPU_SET_MASK(SYSTEM_CPU_SET, system_set);
	if (ret < 0)
		return ret;

	/* reserve last available CPU for RT task */
	snprintf(rt_set, 8, "%d", ncpus - 1);
	ret = CPU_SET_MASK(RT_CPU_SET, rt_set);

	return ret;
}

static int cgroup_v1_assign_to_rt_set(pid_t pid)
{
	char pid_str[12];

	snprintf(pid_str, 12, "%u", pid);
	return CPU_SET_TASK(RT_CPU_SET, pid_str);
}

static bool is_lvrt_cgroup_v1()
{
	return (access(RT_CPU_SET, F_OK) == 0);
}

static int set_curr_affinity(int cpu)
{
	cpu_set_t set;

	CPU_ZERO(&set);
	CPU_SET(cpu, &set);

	return sched_setaffinity(0, sizeof(set), &set);
}

static int set_cpu_affinities()
{
	int ret = -1;
	int ncpus = get_nprocs();

	if (ncpus < 2)
		error_exit("Test requires a system with at least 2 CPUs available", NULL);

	if (is_lvrt_cgroup_v1()) {
		/* use cgroups with lvrt v1 layout to isolate the last available
		   CPU for our test; move everything else to the other CPUs */
		ret = cgroup_v1_setup(ncpus);
		if (ret < 0)
			return ret;

		ret = cgroup_v1_assign_to_rt_set(getpid());
	} else {
		/* set our affinity to the last available CPU which is already
		   isolated for this test via the 'isolcpus' boot parameter */
		ret = set_curr_affinity(ncpus - 1);
	}

	return ret;
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
		error_exit("Failed to mlockall memory", NULL);

	if (set_cpu_affinities() < 0)
		error_exit("Failed to configure CPU affinities", NULL);

	if (set_fifo_priority(TEST_PRIO) < 0)
		error_exit("Failed to set the test scheduling priority", NULL);

	if (flush_disk_io() < 0)
		error_exit("Failed to flush disk caches", NULL);
}

static inline uint64_t tsdiff(struct timespec *start, struct timespec *end)
{
	uint64_t t1 = (uint64_t)(start->tv_sec) * NSEC_PER_SEC +
		start->tv_nsec;
	uint64_t t2 = (uint64_t)(end->tv_sec) * NSEC_PER_SEC +
		end->tv_nsec;

	return t2 - t1;
}

static uint64_t get_percentile(double percent, struct histogram_data *h)
{
	uint64_t i;
	uint64_t sum;
	double pval;

	pval = (double)h->cnt * (percent / 100.0);

	for (i = 0, sum = 0; i < h->size; i++) {
		sum += h->data[i];

		if (sum >= pval)
			break;
	}
	return i;
}

static void test(uint64_t duration, struct histogram_data *h)
{
	struct timespec prev_ts;
	struct timespec ts;
	time_t end_sec;
	uint64_t dt;

	clock_gettime(CLOCK_MONOTONIC, &prev_ts);
	end_sec = prev_ts.tv_sec + duration;

	while (prev_ts.tv_sec < end_sec) {
		clock_gettime(CLOCK_MONOTONIC, &ts);
		dt = tsdiff(&prev_ts, &ts);

		if (h) {
			if (dt > h->max)
				h->max = dt;

			if (dt >= max_latency)
				break;

			h->data[dt]++;
			h->cnt++;
		}
		prev_ts = ts;
	}
}

static int log_results(struct histogram_data *h)
{
	FILE *log;
	uint64_t i;
	char path[PATH_MAX];
	time_t t = time(NULL);
	struct tm tm = *localtime(&t);

	if (!h)
		return -1;

	snprintf(path, PATH_MAX,
		 "%s/histogram-%d_%02d_%02d-%02d_%02d_%02d.log",
		 LOG_DIR,
		 tm.tm_year + 1900, tm.tm_mon + 1, tm.tm_mday,
		 tm.tm_hour, tm.tm_min, tm.tm_sec);
	log = fopen(path, "w");
	if (!log) {
		perror("error: failed to open log file");
		return -1;
	}

	fprintf(log, "# NO_HZ_FULL polling test histogram log\n");
	fprintf(log, "# date: %d-%02d-%02d %02d:%02d:%02d\n",
		tm.tm_year + 1900, tm.tm_mon + 1, tm.tm_mday,
		tm.tm_hour, tm.tm_min, tm.tm_sec);
	fprintf(log, "#\n");
	fprintf(log, "ns\tcount\n");

	for (i = 0; i < h->size && i < h->max; i++)
		fprintf(log, "%lu\t%lu\n", i, h->data[i]);

	fprintf(log, "# total count: %lu (samples)\n", h->cnt);
	fprintf(log, "# histogram max latency: %lu (ns)\n", h->max);
	fprintf(log, "# 99.999 percentile: %lu (ns)\n", get_percentile(99.999, h));
	fprintf(log, "# 99.9999 percentile: %lu (ns)\n", get_percentile(99.9999, h));
	fclose(log);
}

static void validate_results(struct histogram_data *h)
{
	uint64_t p_99_999;
	uint64_t p_99_9999;
	const char *stats_summary = "Total count: %lu (samples)\n"
                                    "Maximum latency: %lu (ns)\n"
                                    "99.999 percentile: %lu (ns)\n"
                                    "99.9999 percentile: %lu (ns)\n";

	if (!h)
		error_exit("Results validation failed; no histogram data", NULL);

	p_99_999 = get_percentile(99.999, h);
	p_99_9999 = get_percentile(99.9999, h);

	if (h->max >= max_latency)
		error_exit("Maximum latency exceeded", stats_summary, h->cnt, h->max, p_99_999, p_99_9999);
	if (p_99_999 > percentile_99_999)
		error_exit("99.999%% threshold exceeded", stats_summary, h->cnt, h->max, p_99_999, p_99_9999);
	if (p_99_9999 > percentile_99_9999)
		error_exit("99.9999%% threshold exceeded", stats_summary, h->cnt, h->max, p_99_999, p_99_9999);

	success_exit(stats_summary, h->cnt, h->max, p_99_999, p_99_9999);
}

int main(int argc, char *argv[])
{
	struct histogram_data histogram;

	if (parse_options(argc, argv) < 0)
		error_exit("Failed to parse arguments\n", NULL);

	histogram.cnt = 0;
	histogram.max = 0;
	histogram.size = max_latency;
	histogram.data = (uint64_t*)calloc(histogram.size, sizeof(uint64_t));
	if (!histogram.data)
		error_exit("Failed to allocate space for histogram data", NULL);

	setup();

	/* 60 seconds warm-up */
	test(60, NULL);

	test(test_duration, &histogram);
	log_results(&histogram);
	validate_results(&histogram);
}
