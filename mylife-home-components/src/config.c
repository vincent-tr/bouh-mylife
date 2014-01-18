/*
 * config.c
 *
 *  Created on: Jan 17, 2014
 *      Author: vincent
 */

#include <stddef.h>
#include <string.h>
#include <linux/limits.h>
#include <unistd.h>

#include "config.h"
#include "logger.h"

static config_t conf;

static void conf_api_assert(int api_ret);

void conf_init()
{
	char path[PATH_MAX];
	log_assert(readlink("/proc/self/exe", path, PATH_MAX) != -1);
	strcat(path, ".conf");

	log_debug("config file : %s", path);
	config_init(&conf);
	conf_api_assert(config_read_file(&conf, path));
}

void conf_terminate()
{
	config_destroy(&conf);
}

config_t *conf_get()
{
	return &conf;
}

void conf_api_assert(int api_ret)
{
	if(api_ret == CONFIG_TRUE)
		return;

	log_fatal("config error : %s:%d : %s",
		config_error_file(&conf),
		config_error_line(&conf),
		config_error_text(&conf));
}
