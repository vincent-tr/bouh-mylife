/*
 * module.c
 *
 *  Created on: 21 juil. 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include <sys/select.h>
#include <stdarg.h>

#include "core_api.h"
#include "component.h"
#include "comp_manager.h"

static void mod_init(void **apis);
static void mod_terminate();

static const char *required_modules[] = { "core", NULL };
MODULE_DEFINE("ctype-mpd", 1, 0, required_modules, NULL, mod_init, mod_terminate);

struct core_api *core_api;

void mod_init(void **apis)
{
	core_api = apis[0];

	component_init();
	comp_manager_init();
}

void mod_terminate()
{
	comp_manager_terminate();
	component_terminate();
}
