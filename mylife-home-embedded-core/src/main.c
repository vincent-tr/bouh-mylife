/*
 * main.c
 *
 *  Created on: 15 juin 2013
 *      Author: pumbawoman
 */

#include <stdio.h>
#include <stdlib.h>
//#include "libircclient.h"

#include "logger.h"
#include "module.h"


static void init(int interactive);
static void terminate();

int main(void) {
	init(1);

	terminate();
}

void init(int interactive)
{
	log_init(interactive);
	module_init();
}

void terminate()
{
	module_terminate();
	log_terminate();
}
