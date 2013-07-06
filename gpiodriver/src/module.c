/*
 * module.c
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include <sys/select.h>

#include "core_api.h"
#include "commands.h"
#include "gpio.h"

static void mod_init(void **apis);
static void mod_terminate();

struct module_api
{

};

static struct module_api myapi =
{

};

static const char *required_modules[] = { "core", NULL };
MODULE_DEFINE("gpiodriver", 1, 0, required_modules, &myapi, mod_init, mod_terminate);

struct core_api *core_api;

void mod_init(void **apis)
{
	core_api = apis[0];

	gpio_init();
	commands_init();
}

void mod_terminate()
{
	commands_terminate();
	gpio_terminate();
}
