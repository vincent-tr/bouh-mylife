/*
 * module.c
 *
 *  Created on: 4 juil. 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include <sys/select.h>

#include "core_api.h"
#include "commands.h"

static void mod_init(void **apis);
static void mod_terminate();

static const char *required_modules[] = { "core", NULL };
MODULE_DEFINE("sysadmin", 1, 0, required_modules, NULL, mod_init, mod_terminate);

struct core_api *core_api;

void mod_init(void **apis)
{
	core_api = apis[0];

	commands_init();
}

void mod_terminate()
{
	commands_terminate();
}
