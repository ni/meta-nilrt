#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include <assert.h>

#define EXPECTED_STACK_SIZE_KB 256

int main(char argc, char** argv)
{
	size_t stacksize;
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_getstacksize (&attr, &stacksize);
	int pthread_stack_size_kb = stacksize / 1024;
	assert(EXPECTED_STACK_SIZE_KB == pthread_stack_size_kb);
}
