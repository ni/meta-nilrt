//The purpose of this test is specifically to check if RT throttling is disabled
//and to confirm the presence of RT patch.

//This is NOT a general purpose jitter benchmark, this test just confirms if the
//RT throttling is disabled, and checks if the RT patch is applied or not.
//It makes a time critical loop and measures the standard deviation of jitter,
//the largest jitter spike, and the mean jitter wich are used to validate if
//RT throtling is disabled.
#define _GNU_SOURCE

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <sched.h>
#include <string.h>
#include <math.h> //sqrt()
#include <assert.h>

#define RESULT_SIZE 10
#define NS_IN_SEC 1000000000
#define LOOP_ITERATIONS 6000000
#define NOOP_ITERATIONS 100

//Log some debug data into a file.
#define FILE_LOG 1

#ifdef FILE_LOG
#define file_log(F, M, ...) fprintf(F, M, ##__VA_ARGS__)
#else
#define file_log(F, M, ...)
#endif

#define fail(MSG, ...) printf("FAIL: %s: " MSG "\n", __FILE__, ##__VA_ARGS__);
#define test_and_fail(STMT, MSG, ...) if (STMT) { fail(MSG, ##__VA_ARGS__); exit(1); }

#define HEADER_MSG "IF YOU RUN THIS PROGRAM, YOUR TARGET WILL BECOME UNRESPONSIVE(~2 MIN FOR GUMSTIX; A FEW\n\
SECONDS FOR ZYNQ) BECAUSE THIS PROGRAM RUNS IN REAL TIME, SCHED_FIFO, WITH PRIORITY 99.\n"

struct timespec measured_time[LOOP_ITERATIONS+1];
double population[LOOP_ITERATIONS+1];

void conf_sched()
{
	int priority_max;
	priority_max = sched_get_priority_max(SCHED_FIFO);

	cpu_set_t mask;
	struct sched_param schedp;

	int cpu = 0; //run current process on cpu 0
	CPU_ZERO(&mask);
	CPU_SET(cpu, &mask);
	test_and_fail(sched_setaffinity(0, sizeof(mask), &mask) == -1,"FAIL: CPU affinity could not be set\n");

	memset(&schedp, 0, sizeof(schedp));
	schedp.sched_priority = priority_max;
	test_and_fail(sched_setscheduler(0, SCHED_FIFO, &schedp) != 0, "FAIL: Could not set scheduler FIFO policy\n");
}

double calculate_mean(double *population)
{
	double sum = 0;
	double mean = 0;
	int i;

	if (population == NULL)
		return -1;

	for (i = 0; i < LOOP_ITERATIONS; i++)
		sum += population[i];

	//LOOP_ITERATIONS also represents the length of out array.
	mean = (long double)sum/(long double)LOOP_ITERATIONS;

	return mean; //nanoseconds
}

double calculate_squared_diff(double *population, double mean_ns)
{
	int i;
	double diff; //The difference of each data point from the mean.
	double sum = 0.0;
	for(i = 0; i < LOOP_ITERATIONS; i++)
	{
		diff = population[i] - mean_ns;
		diff = diff * diff;
		sum +=diff;
	}

	sum /= LOOP_ITERATIONS;
	sum = sqrt((double)sum); //also make the sqrt

	return sum; //nanoseconds
}

//http://en.wikipedia.org/wiki/Standard_deviation
//returns standard deviation in nanoseconds:
double  calculate_standard_deviation(double *population, double *mean_ns)
{
	if (population == NULL)
		fail("(NON-FATAL) population == NULL");

	//Compute the mean (average) of population:
	*mean_ns = calculate_mean(population);

	//Compute the squared differences (squared_diff) of each data point from the mean:
	return calculate_squared_diff(population, *mean_ns); //nanoseconds
}

int main(int argc, char** argv)
{
	double time_std_dev, time_max_jitter ,time_mean;
	double time_diff; //temp variable
	struct timespec time;
	int i, j, dummy;

#if FILE_LOG
	FILE *fp_log = NULL; //we can log some data in this file (this file can be used for debugging)
#endif

	//printf(HEADER_MSG);

	//Make some configurations, to run this process with greatest priority.
	conf_sched();

	//This is the time-critical loop.
	//Logging into buffer "measured_time" the time intervals between consecutive interations:
	//We use i<=LOOP_ITERATIONS and not i<LOOP_ITERATIONS, because we want to generate N+1
	//events, and we want to obtain N (LOOP_ITERATIONS) time periods between the N+1 events.
	for(i=0; i<=LOOP_ITERATIONS; i++)
	{
		clock_gettime(CLOCK_REALTIME, &time);
		measured_time[i] = time;

		//Do some operations, for which we want to measure the jitter:
		for(j = 0; j< NOOP_ITERATIONS; j++)
		{
			dummy++; //Make some noops, in this case a addition, we can chooe what op. we want.
		}
	}

#if FILE_LOG
	fp_log = fopen("log.txt", "w");
	assert(fp_log != NULL);
#endif

	//From measured time intervals calculate time period between iterations:
	for(i=0; i<LOOP_ITERATIONS; i++)
	{
		double time_a_ns, time_b_ns;
		time_a_ns = (double)measured_time[i].tv_sec * (double)NS_IN_SEC + (double)measured_time[i].tv_nsec;
		time_b_ns = (double)measured_time[i+1].tv_sec * (double)NS_IN_SEC + (double)measured_time[i+1].tv_nsec;

		population[i] = time_b_ns - time_a_ns;//time period between iterations
	}

	time_std_dev = calculate_standard_deviation(population, &time_mean);

	file_log(fp_log, "standard_deviation=[%f]\n", time_std_dev);
	file_log(fp_log, "time_mean=[%f]\n", time_mean);

	//Calculate maximum jitter:
	//	(make a diferrence between each element and the population mean)
	time_max_jitter = 0;
	for(i=0; i<LOOP_ITERATIONS; i++)
	{
		if(population[i] > time_mean)
			time_diff = population[i] - time_mean;
		else
			time_diff = time_mean - population[i];

		if(time_diff > time_max_jitter)
		{
			time_max_jitter = time_diff;
			//This tells us the index, when a new max is found.
			file_log(fp_log, "jitter = %f, i=%d\n", time_max_jitter,i);
		}
	}

	file_log(fp_log, "time_max_jitter=[%f]\n", time_max_jitter);

	//This test might be calibrated for diferent hardware. Diferent thresholds
	//can be choosed... The following threshold values were found experimentally.

	//Testing the jitter standard deviation values:
	//When RT throttling is disabled, we usually have a value below 1500 ns, when
	//throttling is enabled, we have much higher values for std_deviation
	//around 63639 nanoseconds.
	//http://nitalk.natinst.com/docs/DOC-64589#comment-46396
	test_and_fail(time_std_dev > 1500, "jitter standard deviation above threshold\n");

	//Testing the jitter mean value:
	//(testing the mean jitter value might not be relevant because it can have
	//identical values with throttle disabled or enabled)
	//http://nitalk.natinst.com/docs/DOC-64589#comment-46396
	test_and_fail(time_mean > 1800, "jitter mean value above threshold\n");

	//testing the jitter max value;
	//http://nitalk.natinst.com/docs/DOC-64589#comment-46396
	test_and_fail(time_max_jitter > 150000, "max jitter value above threshold\n");

#if FILE_LOG
	fclose(fp_log);
#endif

	return 0;
}
