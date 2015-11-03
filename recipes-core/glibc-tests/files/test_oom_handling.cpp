#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <sys/wait.h>

#define N                          10000
#define CHILD_EXIT_SUCCESS         2
#define DEBUG_PRINTF               0

#define fail(MSG, ...) printf("FAIL: %s: " MSG "\n", __FILE__, ##__VA_ARGS__);
#define test_and_fail(STMT, MSG, ...) if (STMT) { fail(MSG, ##__VA_ARGS__); exit(1); }

int allocate_memory (void)
{
	int i, n = 0;
	char *pp[N];

	for (n = 0; n < N; n++)
	{
		pp[n] = (char*)malloc(1<<20);
#if DEBUG_PRINTF
		printf("malloc: %d\n", n);
#endif
		if (pp[n] == NULL)
			break;
	}

	for (i = 0; i < n; i++)
	{
		memset (pp[i], 0, (1<<20));
#if DEBUG_PRINTF
		printf("memset: %d\n", i);
#endif
	}
#if DEBUG_PRINTF
	printf("OK\n");
#endif
	return 0;
}

//Child process allocates a lot of memory, then uses the allocated memory.
//Returns 0 if child is killed with SIGKILL (we suppose OOM Killer is the actual killer).
//Returns 1 if child is not killed (OOM Killer was not invoked on child).
int check_memory_overcommit()
{
	pid_t child_pid;
	//Because we cannot register a callback handler to catch SIGKILL (to check if
	//the process was killed or not by OOM Killer) we can make a fork and check
	//for the exit code of the child process. If the child process was not killed
	//and returned the exit code CHILD_EXIT_SUCCESS, then we know that memory
	//over commit is turned off. If the child gets killed, the memory over commit
	//is not turned off.
	child_pid = fork();
	if(child_pid == -1)
	{
#if DEBUG_PRINTF
		printf("Parent: -Can't fork.\n");
#endif
		return -1;
	}
	if(child_pid == 0)
	{	//code that runs only in child:
		allocate_memory();
		_exit(CHILD_EXIT_SUCCESS);
	}
	else
	{	//code that runs only in parent (waiting for child return value):
		int status;
		waitpid(child_pid, &status, WUNTRACED);
		if(WIFEXITED(status))
		{
			if(WEXITSTATUS(status) == CHILD_EXIT_SUCCESS)
				return 1; //If child terminated normally, then return 1.
			else
				return -2; //Highly unlikely.
		}
		else
		{
			if(WIFSIGNALED(status))
				if(WTERMSIG(status) == SIGKILL)
				{
#if DEBUG_PRINTF
					printf("Child was killed with signal SIGKILL and has STATUS=%d.\n", status);
#endif
					return 0;
				}
		}
	}
	return -3;
}

//Check if system configuration is ok: overcommit_memory == 2 and overcommit_ratio <= 100.
int main(int argc, char** argv)
{
	//We expect memory overcommit to be deactivated.
	test_and_fail(check_memory_overcommit()!=1, "memory overcommit is activated");
}
