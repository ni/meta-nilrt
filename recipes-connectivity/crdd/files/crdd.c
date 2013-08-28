/**
   Central Regulatory Domain Daemon for Linux
   Copyright (c) 2012, National Instruments Corp.

   Permission to use, copy, modify, and/or distribute this software for any
   purpose with or without fee is hereby granted, provided that the above
   copyright notice and this permission notice appear in all copies.

   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

#define _GNU_SOURCE
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/prctl.h>

#include <netlink/netlink.h>
#include <netlink/socket.h>
#include <netlink/msg.h>
#include <netlink/attr.h>

#include "log.h"

#define REGULATORY_AGENT	"crda"
#define OOM_SCORE		-1000
#define MAX_UEVENT_ENVP		256

static struct nl_sock* monitor_init()
{
	struct nl_sock* sk;
	int ret;

	sk = nl_socket_alloc();
	if (sk == NULL) {
		log_err("crdd: failed to allocate netlink socket\n");
		return NULL;
	}

	nl_socket_disable_seq_check(sk);

	ret = nl_connect(sk, NETLINK_KOBJECT_UEVENT);
	if (ret < 0) {
		log_err("crdd: failed to bind to netlink socket (%d)\n", ret);
		goto err_out;
	}

	ret = nl_socket_set_passcred(sk, 1);
	if (ret < 0) {
		log_err("crdd: failed to enable credential passing (%d)\n", ret);
		goto err_out;
	}

	ret = nl_socket_add_memberships(sk, 1, 0);
	if (ret < 0) {
		log_err("crdd: failed to register membership to the kernel "
			"uevent group (%d)\n", ret);
		goto err_out;
	}

	return sk;

  err_out:
	nl_socket_free(sk);
	return NULL;
}

static void monitor_uninit(struct nl_sock* sk)
{
	if (sk)
		nl_socket_free(sk);
}

static void close_stdio()
{
	close(STDIN_FILENO);
	close(STDOUT_FILENO);
	close(STDERR_FILENO);
}

static void free_uevent_envp(char *envp[])
{
	char *p;

	if (!envp)
		return;

	for(p = envp[0]; p; p++)
		free(p);

	free(envp);
}

static char** get_uevent_envp(char *uevent_path)
{
	int i;
	FILE *f = NULL;
	size_t pathsz;
	char *path;
	char *envp[MAX_UEVENT_ENVP];
	char **result = NULL;
	char *line = NULL;
	size_t len = 0;
	ssize_t bytes = 0;

	pathsz = sizeof("/sys") + strlen(uevent_path) + sizeof("/uevent") - 1;
	path = malloc(pathsz);
	if (!path) {
		log_err("crdd: failed to allocate memory for path to uevent\n");
		goto out;
	}
	snprintf(path, pathsz, "/sys%s/uevent", uevent_path);

	f = fopen(path, "r");
	if (f == NULL) {
		log_err("crdd: failed to open uevent file %s\n", path);
		goto out;
	}

	for (i = 0; i < (MAX_UEVENT_ENVP - 1); i++) {
		char *delim;

		bytes = getline(&line, &len, f);
		if (bytes <= 0)
			break;

		/* strip out the end of line delimiter */
		delim = strrchr(line, '\n');
		if (delim)
			bytes = (ssize_t)(delim - line);

		envp[i] = malloc(bytes + 1);
		if (!envp[i]) {
			log_err("crdd: failed to allocate memory at line %d\n",
				__LINE__);
			goto out;
		}
		memcpy(envp[i], line, bytes);
		envp[i][bytes] = 0;
	}
	envp[i] = NULL;

	result = envp;

  out:
	if (!result)
		free_uevent_envp(envp);

	free(path);
	free(line);

	if (f)
		fclose(f);

	return result;
}

static char* agent_argv[] = {REGULATORY_AGENT, NULL};
static int run_agent(char *uevent_path)
{
	int err;
	pid_t pid;

	pid = fork();
	switch (pid) {
		case 0:
			prctl(PR_SET_PDEATHSIG, SIGTERM);
			close_stdio();
			err = execvpe(agent_argv[0], agent_argv,
				      get_uevent_envp(uevent_path));

			/* exec only returns on error */
			log_err("crdd: error %d running %s\n", err, REGULATORY_AGENT);
			exit(EXIT_FAILURE);
		case -1:
			log_err("crdd: failed to spawn central regulatory "
				"domain agent\n");
			return -1;
		default:
			return 0;
	}
}


void adjust_oom_score()
{
	FILE *f = fopen("/proc/self/oom_score_adj", "we");
	if (!f)
		return;

	fprintf(f, "%d\n", OOM_SCORE);
	fclose(f);
}

int main(int argc, char **argv)
{
	pid_t pid, sid;
	struct nl_sock *monitor_socket = NULL;
	int result = EXIT_SUCCESS;

	log_open();

	/* turn into a daemon */
	pid = fork();
	if (pid < 0) {
		log_err("crdd: failed to spawn daemon\n");
		result = EXIT_FAILURE;
		goto out;
	}
	if (pid > 0) {
		/* we have a good child daemon, exit the parent */
		goto out;
	}

	/* create a new sid so we don't become an orphan */
	sid = setsid();
	if (sid < 0) {
		log_err("crdd: failed to create a daemon SID\n");
		result = EXIT_FAILURE;
		goto out;
	}

	umask(0);

	/* chage the current working dir to something valid for a daemon */
	if ((chdir("/")) < 0)
		log_err("crdd: failed to chdir\n");

	close_stdio();

	monitor_socket = monitor_init();
	if (monitor_socket == NULL) {
		result = EXIT_FAILURE;
		goto out;
	}

	adjust_oom_score();

	/* POSIX.1-2001: ignore SIGCHLD to prevent the creation of zombies */
	signal(SIGCHLD, SIG_IGN);

	while (1) {
		int err;
		char *msg;
		struct ucred *cred;
		struct sockaddr_nl nla = {0};

		err = nl_recv(monitor_socket, &nla, (unsigned char **)&msg, &cred);
		if (err < 0) {
			log_err("crdd: rx error %d\n", err);
			continue;
		}

		if (cred && (cred->uid == 0)) {
			/* we are only interested in regulatory domain messages:
			   e.g. 'change@/devices/platform/regulatory.0' */
			if ((strstr(msg, "change@") == msg) &&
			    (strstr(msg, "regulatory") != NULL)) {
				char *payload = strchr(msg, '@');
				if (payload) {
					/* skip delimiter */
					payload++;

					run_agent(payload);
				}
			}
		}

		free(msg);
		free(cred);
	}
	monitor_uninit(monitor_socket);

out:
	log_close();

	return result;
}

