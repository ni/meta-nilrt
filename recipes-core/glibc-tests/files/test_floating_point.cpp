#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdarg.h>
#include <stdbool.h>
#include <unistd.h>
#include <fcntl.h>
#include <getopt.h>
#include <pthread.h>
#include <signal.h>
#include <sched.h>
#include <string.h>
#include <time.h>
#include <errno.h>
#include <limits.h>
#include <sys/prctl.h>
#include <sys/stat.h>
#include <sys/sysinfo.h>
#include <sys/types.h>
#include <sys/utsname.h>
#include <sys/mman.h>

#define gettid() syscall(__NR_gettid)

#define USEC_PER_SEC		1000000
#define NSEC_PER_SEC		1000000000

typedef struct result
{
	int64_t arr_size;
	int64_t op_time;
	result()
	{
		arr_size = 0;
		op_time = -1;
	}
}result_t;

enum operation
{
	addition=0, //do not change value, used for indexing
	substraction,
	multiplication,
	division
};

static inline void my_gettime(struct timespec *t)
{
	int ret = clock_gettime(CLOCK_MONOTONIC, t);
}

static inline int64_t calcdiff_us(struct timespec *t1, struct timespec *t2)
{
	int64_t diff;
	diff = USEC_PER_SEC * (long long)((int) t2->tv_sec - (int) t1->tv_sec);
	diff += ((int) t2->tv_nsec - (int) t1->tv_nsec) / 1000;
	return diff;
}

static inline double my_rand_double()
{
	//errno_t err;
	double d = 0;
	unsigned int max = 2043245235, number;

	//err = rand_s(&number );
	number = rand();

	//if (err != 0)
		//printf("The rand_s function failed!\n");
	//else
		d = (double) number / (double) UINT_MAX * max;
	return d;
}

void init_arrays(double *x, double *y, double *z, unsigned int arr_size)
{
	for (unsigned int i = 0; i < arr_size; i++) {
		x[i] = my_rand_double();
		y[i] = my_rand_double();
		z[i] = 0.0;
	}
}

int test_fp(int arr_size, result_t *result, operation op)
{
	double *x, *y, *z;
	struct timespec t1, t2;
	int64_t op_time = -1;
	int i;

	x = (double *) malloc(sizeof(double) * arr_size);
	y = (double *) malloc(sizeof(double) * arr_size);
	z = (double *) malloc(sizeof(double) * arr_size);

	init_arrays(x, y, z, arr_size);
	my_gettime(&t1);
	switch(op)
	{
	case addition:
		for (i = 0; i < arr_size; i++) {
			z[i] = x[i] + y[i];
		}
		break;
	case substraction:
		for (i = 0; i < arr_size; i++) {
			z[i] = x[i] - y[i];
		}
		break;
	case multiplication:
		for (i = 0; i < arr_size; i++) {
			z[i] = x[i] * y[i];
		}
		break;
	case division:
		for (i = 0; i < arr_size; i++) {
			z[i] = x[i] / y[i];
		}
		break;
	}
	my_gettime(&t2);
	op_time = calcdiff_us(&t1, &t2);
	free(x), free(y), free(z);

	if(result!=NULL)
		{
			result->arr_size = arr_size;
			result->op_time = op_time;
		}

	return 0;
}

void conf_sched(int cpu)
{
	cpu_set_t mask;
	struct sched_param schedp;

	if (cpu != -1) {
		CPU_ZERO(&mask);
		CPU_SET(cpu, &mask);
	}

	memset(&schedp, 0, sizeof(schedp));
	schedp.sched_priority = 50; // use getmax() func to get the priority
	sched_setscheduler(0, SCHED_FIFO, &schedp);
}

void arithmetic_test(int cpu, int arr_size, operation op)
{
	result_t result;
	conf_sched(cpu);
	const char operation_name[][4]={"add","sub","mul","div"};

	char cpu_str=' ';
	if(cpu!=-1)
		cpu_str=(char)cpu+'0';

	test_fp(arr_size, &result, op);
	printf("# %s on CPU%c time=%dus arr_size=%d\n", operation_name[(int)op], cpu_str, result.op_time, arr_size);
}

//------------------------------------------------------------------------------
//This test is a benchmark, not a Pass/Fail test.
//Your target must have at least ~230 MB free RAM for 10 000 000 element arrays.

int main(int argc, char** argv)
{
	printf("#\n# Floating point benchmark\n#\n");
	for(int cpu=-1; cpu<=1; cpu++) //-1 all cpu's, CPU0 and CPU1
	{
		for(int arr_size=10000; arr_size<=10000000; arr_size*=10)
		{
			for(int op=(int)addition; op<=(int)division; op++)
			{
				arithmetic_test(cpu, arr_size, (operation)op);
			}
		}
	}
}
