#include <stdio.h>
#include <stdlib.h>
#include <semaphore.h>
#include <fcntl.h>
#include <pwd.h>
#include <unistd.h>
#include <pthread.h>

#define fail(MSG, ...) printf("FAIL: %s: " MSG "\n", __FILE__, ##__VA_ARGS__);
#define test_and_fail(STMT, MSG, ...) if (STMT) { fail(MSG, ##__VA_ARGS__); exit(1); }

int getid(const char *uname, uid_t *uid, gid_t *gid)
{
	struct passwd *pswd = getpwnam(uname);
	if (!pswd) {
		perror("getpwnam() failed:");
		return -1;
	}
	*uid = pswd->pw_uid;
	*gid = pswd->pw_gid;
	return 0;
}

//This test verifies that two processes can share mutex/semaphore
int main(int argc, char** argv)
{
	sem_t *sem;
	uid_t uid, uid_current;
	gid_t gid;
	const char *user_name = "lvuser";
	uid_current = getuid();
	test_and_fail(0 != getid(user_name, &uid, &gid), "Could not get id");
	test_and_fail(0 != seteuid(uid), "Could not setuid");
	sem = sem_open("Test-Sem", O_CREAT, S_IRWXU|S_IRWXG|S_IRWXO , 1);
	test_and_fail(sem == SEM_FAILED, "Unable to create cross-process named semaphore");
	sem_close(sem);
}
