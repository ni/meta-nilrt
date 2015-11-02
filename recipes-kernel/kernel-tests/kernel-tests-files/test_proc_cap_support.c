#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <sys/syscall.h>
#include <sys/prctl.h>
#include <errno.h>
#include <inttypes.h>
#include <assert.h>
#include <sys/capability.h>

//This test is testing this proces's capabilities. The curent process is run under
//root and it has all the capailities bits for Permitted and Effective set to 1.
//Here are not tested all the possible combunations of capabilities. Only a few
//representative combinations of capabilities are tested here. If these important
//combinations will pass, then I expect that all possible combinations should pass.
int main(char argc, char** argv)
{
	cap_t caps_proc_set, caps_proc_get;
	int i;

	caps_proc_set = cap_get_proc();
	caps_proc_get = cap_init();

	//We will toggle the Effective capabilities, then we will toggle the Inheritable caps:
	//The 'for' loop iterates with i from CAP_CHOWN to CAP_LAST_CAP from linux/capability.h:
	//#define CAP_CHOWN            0
	//#define CAP_DAC_OVERRIDE     1
	//#define CAP_DAC_READ_SEARCH  2
	//#define CAP_FOWNER           3
	//#define CAP_FSETID           4
	//#define CAP_KILL             5
	//#define CAP_SETGID           6
	//#define CAP_SETUID           7
	//#define CAP_SETPCAP          8
	//#define CAP_LINUX_IMMUTABLE  9
	//#define CAP_NET_BIND_SERVICE 10
	//#define CAP_NET_BROADCAST    11
	//#define CAP_NET_ADMIN        12
	//#define CAP_NET_RAW          13
	//#define CAP_IPC_LOCK         14
	//#define CAP_IPC_OWNER        15
	//#define CAP_SYS_MODULE       16
	//#define CAP_SYS_RAWIO        17
	//#define CAP_SYS_CHROOT       18
	//#define CAP_SYS_PTRACE       19
	//#define CAP_SYS_PACCT        20
	//#define CAP_SYS_ADMIN        21
	//#define CAP_SYS_BOOT         22
	//#define CAP_SYS_NICE         23
	//#define CAP_SYS_RESOURCE     24
	//#define CAP_SYS_TIME         25
	//#define CAP_SYS_TTY_CONFIG   26
	//#define CAP_MKNOD            27
	//#define CAP_LEASE            28
	//#define CAP_AUDIT_WRITE      29
	//#define CAP_AUDIT_CONTROL    30
	//#define CAP_SETFCAP          31
	//#define CAP_MAC_OVERRIDE     32
	//#define CAP_MAC_ADMIN        33
	//#define CAP_LAST_CAP         CAP_MAC_ADMIN
	for(i = CAP_CHOWN; i <= CAP_LAST_CAP; i++)
	{
		//We toggle the Effective capability:
		assert(0 == cap_set_flag(caps_proc_set, CAP_EFFECTIVE, 1, (cap_value_t*)&i, CAP_CLEAR));
		assert(0 == cap_set_proc(caps_proc_set));

		caps_proc_get = cap_get_proc();
		assert( NULL != caps_proc_get);

		//Check if the 'set' capability is equal with the 'get' capability.
		assert(0 == cap_compare( caps_proc_get, caps_proc_set));
		assert(0 == cap_set_flag(caps_proc_set, CAP_EFFECTIVE, 1, (cap_value_t*)&i, CAP_SET));

		//We toggle the Inheritable capability:
		assert(0 == cap_set_flag(caps_proc_set, CAP_INHERITABLE, 1, (cap_value_t*)&i, CAP_SET));
		assert(0 == cap_set_proc(caps_proc_set));

		caps_proc_get = cap_get_proc();
		assert(NULL != caps_proc_get);

		//Check is the 'set' capability is equal with the 'get' capability.
		assert(0 == cap_compare( caps_proc_get, caps_proc_set));
		assert(0 == cap_set_flag(caps_proc_set, CAP_INHERITABLE, 1, (cap_value_t*)&i, CAP_CLEAR));

		assert(0 == cap_set_proc(caps_proc_set));
	}

	//We check if we can turn off all the Inheritable, Effective, Permitted capabilities:
	caps_proc_set = cap_get_proc();
	cap_value_t cap_values[CAP_LAST_CAP];
	for(i = CAP_CHOWN; i <= CAP_LAST_CAP; i++)
	{
		cap_values[i] = (cap_value_t) i;
	}

	assert(0 == cap_set_flag(caps_proc_set, CAP_INHERITABLE, CAP_LAST_CAP, cap_values, CAP_CLEAR));
	assert(0 == cap_set_flag(caps_proc_set, CAP_EFFECTIVE, CAP_LAST_CAP, cap_values, CAP_CLEAR));
	assert(0 == cap_set_flag(caps_proc_set, CAP_PERMITTED, CAP_LAST_CAP, cap_values, CAP_CLEAR));
	assert(0 == cap_set_proc(caps_proc_set));

	caps_proc_get = cap_get_proc();
	assert(NULL != caps_proc_get);

	//Check is the 'set' capability is equal with the 'get' capability.
	assert(0 == cap_compare( caps_proc_get, caps_proc_set));

	cap_free(caps_proc_set);
	cap_free(caps_proc_get);

	return 0;
}
