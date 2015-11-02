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

#define fail(MSG, ...) printf("FAIL: %s: " MSG "\n", __FILE__, ##__VA_ARGS__);
#define test_and_fail(STMT, MSG, ...) if (STMT) { fail(MSG, ##__VA_ARGS__); exit(1); }

#define PROGRAM_PATH "./cap_support_exe_to_test"
#define DEBUG_PRINTF 0

int execute_linux_command(const char *command, char *stdout_and_stderr, int result_size)
{
	FILE *f = NULL;
	char tmp[200];

	memset(stdout_and_stderr, 0, result_size);

	f = popen(command, "r");
	test_and_fail(f == 0, "Error opening pipe to %s", command);

	while(fgets(tmp, sizeof(tmp), f))
		strncat(stdout_and_stderr, tmp, strlen(tmp));

	pclose(f);

	// if the command issued an Error output, print it and fail the test
	test_and_fail(0 == strncmp("Error:", tmp, strlen("Error:")), "%s", tmp);

	return 0;
}

//Launches an executable situated at path "exec_file_path" with a command line
//parameter. The parameter can be a capability or "-1". If the parameter is a
//capability, then the called executable will try to perform some operations which
//require that capability. If the called executable can execute with success the
//related operations, this function will return 1. If the command line parameter
//is "-1", then the called executable will return it's capabilities (as seen from
//inside), and the parameter "get_caps_child" will be filled with that capabilities.
//The executable must be designed to support the respective capability operations.
int try_file_capability(const char *exec_file_path, int cap, cap_t *get_caps_child)
{
	char command_result[800];
	char command[200];
	int return_val = 0;

	command_result[0] = '\0';
	command[0] = '\0';

	//command := "su lvuser -c '/home/root/dist/tested_program 14'"
	sprintf(command, "%s '%s %d'", "su lvuser -c", exec_file_path, cap);

	test_and_fail(execute_linux_command(command, command_result, sizeof(command_result)),
		      "Failed to execute external command %s", command_result);

#if DEBUG_PRINTF
	printf("{{%s}}\n", command);
	printf("[[%s]]\n", command_result);
#endif

	if (cap != -1) {
		//Normal capability tested
		sscanf(command_result, "%d", &return_val);
	} else if (get_caps_child != NULL) {
		//Get the capabilities as seen from inside the process of executable exec_file_path.
		*get_caps_child = cap_from_text(command_result);
		if (*get_caps_child != NULL)
			return_val = 1;
	}

	return return_val;
}

//This test is setting some capabilities on a executable file, then the executable is
//called, and performs some operations which require capability rights. Then we
//check if the executable could perform or could not perform these operations.
int main(char argc, char** argv)
{
	char command_result[800];
	cap_t caps_write, caps_read;

	test_and_fail(getuid(), "This program needs to be run as root");

	caps_write = cap_init();
	command_result[0] = '\0';

	//We currently check the executable only against these capabilities.
	cap_value_t cap_values[CAP_LAST_CAP + 1];
	cap_values[0] = CAP_SYS_TIME;
	cap_values[1] = CAP_SYS_NICE;
	cap_values[2] = CAP_IPC_LOCK;
	//we intentionally do not set CAP_SYS_BOOT
	int nr_caps = 3;

	//Set on the 'cap_values' capabilities flags:
	cap_clear(caps_write);
	assert(0 == cap_set_flag(caps_write, CAP_PERMITTED, nr_caps, cap_values, CAP_SET));
	assert(0 == cap_set_flag(caps_write, CAP_EFFECTIVE, nr_caps, cap_values, CAP_SET));
	assert(0 == cap_set_file(PROGRAM_PATH, caps_write));

	//Check if the executable can perform operaions which require capabilities.
	assert(1 == try_file_capability(PROGRAM_PATH, CAP_SYS_TIME, NULL));
	assert(1 == try_file_capability(PROGRAM_PATH, CAP_SYS_NICE, NULL));
	assert(1 == try_file_capability(PROGRAM_PATH, CAP_IPC_LOCK, NULL));
	//If tested_program fails to Reboot the system, is a Pass.
	assert(1 == try_file_capability(PROGRAM_PATH, CAP_SYS_BOOT, NULL));

	caps_read = cap_init();
	assert(1 == try_file_capability(PROGRAM_PATH, -1, &caps_read));
	assert(NULL != caps_read);

#if DEBUG_PRINTF
	printf("{Child process CAPs:%s}\n", cap_to_text(caps_read, NULL));
#endif

	//This may be redundant, overchecking it.
	assert(0 == cap_compare(caps_write, caps_read));

	//Clear the 'cap_values' capabilities flags:
	assert(0 == cap_set_flag(caps_write, CAP_PERMITTED, nr_caps, cap_values, CAP_CLEAR));
	assert(0 == cap_set_flag(caps_write, CAP_EFFECTIVE, nr_caps, cap_values, CAP_CLEAR));
	assert(0 == cap_set_file(PROGRAM_PATH, caps_write));

	//Check if the executable cannot perform operaions which require capabilities.
	assert(0 == try_file_capability(PROGRAM_PATH, CAP_SYS_TIME, NULL));
	assert(0 == try_file_capability(PROGRAM_PATH, CAP_SYS_NICE, NULL));
	assert(0 == try_file_capability(PROGRAM_PATH, CAP_IPC_LOCK, NULL));
	assert(1 == try_file_capability(PROGRAM_PATH, -1, &caps_read));
	assert(NULL != caps_read);

#if DEBUG_PRINTF
	printf("{Child process CAPs:%s}\n", cap_to_text(caps_read, NULL));
#endif

	//Check if the capabilities reoprted by executable file are the expected ones.
	assert(0 == cap_compare(caps_write, caps_read));

	cap_free(caps_write);
	cap_free(caps_read);
}
