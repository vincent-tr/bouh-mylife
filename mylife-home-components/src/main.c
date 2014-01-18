/*
 * main.c
 *
 *  Created on: Jan 10, 2014
 *      Author: vincent
 */

#include <stdio.h>
#include <stdlib.h>

#include "logger.h"
#include "config.h"
#include "components.h"
#include "net.h"
#include "loop.h"

static void init();
static void terminate();

int main()
{
	//char *line;

	init();

	puts("running");
	fflush(stdout);

	loop_run();

	terminate();
	return 0;
}

void init()
{
	log_init(1);
	conf_init();
	loop_init();
	net_init();
	components_init();
}

void terminate()
{
	components_terminate();
	net_terminate();
	loop_terminate();
	conf_terminate();
	log_terminate();
}
