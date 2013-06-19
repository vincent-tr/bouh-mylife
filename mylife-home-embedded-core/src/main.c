/*
 * main.c
 *
 *  Created on: 15 juin 2013
 *      Author: pumbawoman
 */

#define _BSD_SOURCE
#define _XOPEN_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

#include "config_base.h"
#include "logger.h"
#include "module.h"
#include "loop.h"

#define ARG_START 1
#define ARG_STOP 2
#define ARG_STATUS 3
#define ARG_INTERACTIVE 4

static int is_interactive;

static void init();
static void terminate();
static void run();

static void term_handler(int signum);
static void do_start();
static void do_stop();
static void do_status();
static void do_interactive();
static pid_t read_pid(); // -1 si non lu
static void write_pid();
static int get_status(); // 1 = running, 0 = stopped

void init()
{
	log_init(is_interactive);
	loop_init();
	module_init();
}

void terminate()
{
	module_terminate();
	loop_terminate();
	log_terminate();
}

void run()
{
	loop_run();
}

int main(int argc, char **argv)
{
	int start = 0;
	int stop = 0;
	int status = 0;
	int interactive = 0;
	int help = 0;
	int error = 0;

	// check des paramètres
	for(int idx = 1; idx < argc; idx++)
	{
		const char *arg = argv[idx];
		if(!strcmp(arg, "start"))
			start = 1;
		else if(!strcmp(arg, "stop"))
			stop = 1;
		else if(!strcmp(arg, "status"))
			status = 1;
		else if(!strcmp(arg, "interactive"))
			interactive = 1;
		else if(!strcmp(arg, "help"))
			help = 1;
		else
		{
			fprintf(stdout, "Unknown argument : '%s'\n", arg);
			help = 1;
			error = 1;
		}
	}

	// check des erreurs possibles
	if(!help)
	{
		int sum = interactive + start + stop + status;
		if(sum != 1)
		{
			help = 1;
			error = 1;
		}
	}

	// affichage de l'aide
	if(help)
	{
		puts("Usage : " CONFIG_BASE_DIRECTORY "/" CONFIG_BINARY " {start|stop|status|interactive|help}");
		puts("start : start as daemon");
		puts("stop : stop the daemon");
		puts("status : get the status of the daemon");
		puts("interactive : run as an interactive program for debugging purpose");
		puts("help : print this message and exit");

		exit(error ? EXIT_FAILURE : EXIT_SUCCESS);
	}

	// exécution des actions demandées
	if(start)
		do_start();
	else if(stop)
		do_stop();
	else if(status)
		do_status();
	else if(interactive)
		do_interactive();

	// démarrage
	init();

	// mise en place de l'arrêt
	signal(SIGTERM, term_handler);
	if(is_interactive)
		signal(SIGINT, term_handler);

	// exécution de la boucle principale qui ne retourne que sur fermeture
	run();

	// lorsqu'on revient, on doit s'arrêter
	terminate();

	return EXIT_SUCCESS;
}

void term_handler(int signum)
{
	loop_exit();
}

void do_start()
{
	if(get_status() == 1)
	{
		puts("already running");
		exit(EXIT_SUCCESS);
	}

	puts("starting");
	puts("started");

	if(daemon(0, 0) == -1)
	{
		fprintf(stderr, "daemon error : %s\n", strerror(errno));
		exit(EXIT_FAILURE);
	}

	write_pid();
}

void do_stop()
{
	pid_t pid = read_pid();
	if(pid == -1)
	{
		puts("not running");
		exit(EXIT_SUCCESS);
	}

	if(!kill(pid, SIGTERM))
	{
		puts("stopping");
	}
	else
	{
		fprintf(stderr, "error stopping : %s\n", strerror(errno));
		exit(EXIT_FAILURE);
	}

	int status;
	waitpid(pid, &status, 0);
	puts("stopped");

	exit(EXIT_SUCCESS);
}

void do_status()
{
	switch(get_status())
	{
		case 1:
			puts("running");
			break;

		case 0:
			puts("not running");
			break;
	}

	exit(EXIT_SUCCESS);
}

void do_interactive()
{
	is_interactive = 1;
}

pid_t read_pid()
{
	FILE *fd = fopen(CONFIG_PID_FILE, "r");
	if(!fd)
		return (pid_t)-1;

	pid_t pid;
	if(fscanf(fd, "%i", &pid) != 1)
		pid = (pid_t)-1;

	fclose(fd);

	return pid;
}

void write_pid()
{
	FILE *fd = fopen(CONFIG_PID_FILE, "w");
	if(!fd)
	{
		fprintf(stderr, "error writing pid : %s\n", strerror(errno));
		exit(EXIT_FAILURE);
	}

	fprintf(fd, "%i", getpid());
	fclose(fd);
}

int get_status() // 1 = running, 0 = stopped
{
	int pid = read_pid();
	if(pid == (pid_t)-1)
		return 0;

	if(!kill(pid, 0))
		return 1;

	if(errno == ESRCH)
		return 0;

	fprintf(stderr, "error getting status : %s\n", strerror(errno));
	exit(EXIT_FAILURE);
}
