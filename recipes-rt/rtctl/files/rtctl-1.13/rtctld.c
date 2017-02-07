/* rtctld
 * Copyright (C) 2015 National Instruments
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

#include <errno.h>
#include <signal.h>
#include <unistd.h>
#include <syslog.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <poll.h>
#include <syslog.h>

#define BUFSIZE 256

#define PROGRAM_NAME "rtctld"

#define SYSLOG_ERROR(formatStr, ...) \
    syslog(LOG_DAEMON | LOG_ERR,  ("ERROR: " formatStr), ## __VA_ARGS__)

static const char* const interruptsFilePath = "/proc/interrupts";
/* Hide standard stdout messages and show stderr as stdout
 * By design don't redirect both stderr and stdout to null
 */
static const char* const configCommand =
    "/usr/sbin/rtctl reset 2>&1 >/dev/null";

static int doDaemonize = 1;
static int pollingFd = -1;

static int isRunning = 1;

static void stop_signal_handler(int sig)
{
    isRunning = 0;
}

static void setupSignalHandler(void)
{
    struct sigaction act = {0};

    act.sa_handler = &stop_signal_handler;

    if (sigaction(SIGTERM, &act, NULL)) {
        SYSLOG_ERROR("Failed to register stop signal handler: %s", strerror(errno));
        exit(errno);
    }
}

static void processArgs(const int argc, const char** argv)
{
    int i;
    const char* argi;

    for (i = 1; i < argc; ++i) {
        argi = argv[i];

        if (strcmp("-x", argi)) {
            SYSLOG_ERROR("Invalid arg #%d \"%s\"", i, argi);
            exit(EINVAL);
        }
        else {
            doDaemonize = 0;
        }
    }
}

/*
 * Fork a child process
 * Child returns
 * Parent exits to declare successful initialization
 */
static void daemonize(void)
{
    pid_t pid = fork();
    if ((pid_t)-1 == pid) {
        SYSLOG_ERROR("Failed to fork worker; %s", strerror(errno));
        exit(errno);
    }

    if (pid) {
        exit(0);
    }
}

/*
 * Polls for a change to the file handle
 * Returns
 *  >0 on successful event poll
 *  EINTR on interrupt
 *  kills process otherwise
 */
static int pollChange(void)
{
    int res;
    struct pollfd pfd = {0};

    pfd.fd = pollingFd;
    pfd.events = POLLPRI | POLLERR;

    res = poll(&pfd, 1, -1);
    if (res < 0 && EINTR != errno) {
        SYSLOG_ERROR("Poll failed; %s", strerror(errno));
        exit(errno);
    }
    else if (!res) {
        SYSLOG_ERROR("Poll timed out, but an infinite timeout was configured");
        exit(ETIMEDOUT);
    }
    /* else, got an event or got interrupted */

    return res;
}

/* Runs the configure script
 * Returns exit code of script or EINTR if interrupted
 * Logs error when config script fails, except interruptions
 */
static int runConfig(void)
{
    char line[BUFSIZE];
    int res;
    FILE *fp = popen(configCommand, "r");

    if (!fp) {
        SYSLOG_ERROR("Failed to open pipe: %s", strerror(errno));
    }

    while (fgets(line, sizeof(line), fp)) {
        SYSLOG_ERROR("%s", line);
    }

    res = pclose(fp);
    if (res) {
        if (-1 == res) {
            SYSLOG_ERROR("Call to pclose() failed: %s", strerror(errno));
        }
        else if (WIFEXITED(res)) {
            SYSLOG_ERROR("Config script failed with return value: %d", WEXITSTATUS(res));
        }
        else if (WIFSIGNALED(res) &&
            (WTERMSIG(res) == SIGINT || WTERMSIG(res) == SIGQUIT)) {
            /* child was interrupted, pass that along */
            errno = res = EINTR;
       }
    }

    return res;
}

int main(int argc, const char** argv)
{
    openlog(PROGRAM_NAME, LOG_PERROR|LOG_CONS|LOG_NDELAY, LOG_DAEMON);
    setupSignalHandler();

    processArgs(argc, argv);

    pollingFd = open(interruptsFilePath, O_RDONLY);
    if (pollingFd < 0) {
        SYSLOG_ERROR("Failed to open polling file \"%s\"; %s",
            interruptsFilePath, strerror(errno));
        exit(errno);
    }

    if (doDaemonize) {
        daemonize();
    }

    /* Configure tasks that started before this daemon
     * All others will be caught in the polling loop
     */
    runConfig();

    while (isRunning) {
        if (pollChange() > 0 && runConfig() != EINTR) {
            /* just in case */
            sleep(1);
        }
    }

    return 0;
}
