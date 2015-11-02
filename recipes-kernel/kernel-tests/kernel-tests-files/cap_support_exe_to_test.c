//The external test "test_exe_cap_support.c" is setting some file capabilities
//to this executable file. Then calls this executable with parameters.
#define _GNU_SOURCE

#include <stdio.h>
#include <unistd.h>
#include <sys/syscall.h>
#include <errno.h>
#include <sched.h>
#include <string.h>
#include <sys/time.h>
#include <sys/mman.h>
#include <stdlib.h>
#include <sys/resource.h>
#include <unistd.h>
#include <sys/reboot.h>
#include <linux/reboot.h>
#include <sys/capability.h>

//Checking if the CAP_IPC_LOCK is set:
//the RLIMIT_MEMLOCK soft resource limit defines a limit on how much
//memory an unprivileged process may lock.
int try_mem_lock()
{
	char *mem_temp;
	struct rlimit rlim;
	if(getrlimit(RLIMIT_MEMLOCK, &rlim) != 0)
		return -1;

	//Alocate a zone of memory larger than RLIMIT_MEMLOCK:
	mem_temp = (char*)malloc((size_t)rlim.rlim_cur + 1);
	if( mem_temp == NULL )
		return -2;
	errno = 0;
	//Try to lock the allocated memory zone:
	//mlock will fail if this process doesn't have CAP_IPC_LOCK and tries to lock
	//a memory zone larger than RLIMIT_MEMLOCK.
	if( mlock(mem_temp, (size_t)rlim.rlim_cur + 1) != 0 )
		if ( errno == ENOMEM )
			return 0;//FAIL

	if( munlock(mem_temp, (size_t)rlim.rlim_cur + 1) !=0 )
		return -4;//FAIL
	return 1;//PASS
}

//Checking if the CAP_SYS_NICE is set or not:
int try_conf_sched()
{
	cpu_set_t mask, mask_read;
	struct sched_param schedp, schedp_read;
	int cpu = 0; //run current process on cpu 0
	CPU_ZERO(&mask);
	CPU_SET(cpu, &mask);
	errno = 0;

	//Set the affinity 0, and the process priority on 99 (sched_get_priority_max):
	if( sched_setaffinity(0, sizeof(mask), &mask) == -1 )
		if(errno == EPERM)
			return 0;//FAIL

	memset(&schedp, 0, sizeof(schedp));
	memset(&schedp, 0, sizeof(schedp_read));
	schedp.sched_priority = sched_get_priority_max(SCHED_FIFO);
	errno = 0;

	if( sched_setscheduler(0, SCHED_FIFO, &schedp) != 0 )
		if(errno == EPERM)
			return 0;//FAIL

	//Check if the scheduler was set to SCHED_FIFO and if the CPU affinity is 0
	//and if the process priority is 99 (sched_get_priority_max):
	if(SCHED_FIFO != sched_getscheduler(0))
		return 0;//Fail, scheduler is not SCHED_FIFO.

	if(sched_getparam(0, &schedp_read) != 0 )
		return 0; //Fail, cannot getparam.
	else
		if(schedp_read.sched_priority != sched_get_priority_max(SCHED_FIFO))
			return 0;//Fail, process priority is not the max priority.

	CPU_ZERO(&mask_read);
	if( sched_getaffinity(0, sizeof(mask_read), &mask_read) != 0)// 0 -> this pid
		return 0;//Fail, cannot obtain affinity.
	else
		//sched_getaffinity succeded
		if(CPU_ISSET(cpu, &mask_read) == 0) //test if the mask contains the CPU cpu
			return 0;//Fail, process is not running on CPU 0.

	return 1;//PASS
}

//Checking if the system time can be modified, if CAP_SYS_TIME is set:
int try_set_time()
{
	struct timeval tv_old, tv_new;
	if( gettimeofday(&tv_old, NULL) != 0 )
		return 0;
	tv_new = tv_old;
	tv_new.tv_sec +=300; //Try to set the clock 300 seconds forward.
	errno = 0;
	if( settimeofday(&tv_new, NULL) != 0 )
		if(errno == EPERM)
			return 0;//FAIL; Cannot set timeofday.

	//Checking if the time was actually set. tv_new should be greater than tv_old + 200 seconds.
	//Probably this is redundant since "settimeofday()" should return -1 in case
	//of failure.
	gettimeofday(&tv_new, NULL);
	if( (tv_old.tv_sec + 200) > tv_new.tv_sec )
	{
		return 0;//FAIL
	}

	//Set the time back, so it will remain approx. unchanged.
	settimeofday(&tv_old, NULL);

	return 1;//PASS
}

//Checking if CAP_SYS_BOOT is not set. We are checking if this process can actually
//reboot the system. If it fails to reboot, everything is ok and this function returns 1.
//If the system will boot, google test will die, and AST will know that test failed
//because lost connection to google test.
int try_reboot_system()
{
	errno = 0;
	//result = reboot(LINUX_REBOOT_MAGIC1, LINUX_REBOOT_MAGIC2C, LINUX_REBOOT_CMD_HALT, &a);
	reboot(LINUX_REBOOT_CMD_RESTART);
	if (errno == EPERM)
		return 1; //Success, in case of beeing unable to reboot because of not having
				  //capability CAP_SYS_BOOT.
	else
		return 0; //Fail for some other reason, highly improbable.
}

//This executable receives a capability number in argv[1] and makes some operations which
//require that capability. If the operations performed succesfully, then it prints
//"1". If the operations which required the capability failed to run, then the program
//will print "0" to the console.
//If "-1" is send as a argument, then this programm will print it's process caps.
int main(int argc, char** argv)
{
	cap_t caps_process;
	int arg;

	if (argc != 2)
	{
		printf("Error: ussage ./cap_support_exe_to_text <integer-capability>\n");
		return 0;
	}

	arg = atoi(argv[1]);
	switch (arg)
	{
		case CAP_IPC_LOCK:
			printf("%d", try_mem_lock());
		break;
		case CAP_SYS_NICE:
			printf("%d", try_conf_sched());
		break;
		case CAP_SYS_TIME:
			printf("%d", try_set_time());
		break;
		case CAP_SYS_BOOT:
			printf("%d", try_reboot_system());
		case -1:
			caps_process = cap_get_proc();
			printf("%s", cap_to_text(caps_process, NULL));
			cap_free(caps_process);
		break;
		default:
			printf("Error: the provided capability %d, is not checked in the executable.\n", arg);
		break;
	}

	return 0;
}
