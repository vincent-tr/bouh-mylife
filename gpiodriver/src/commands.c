/*
 * commands.c
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */


#include <stddef.h>
#include <sys/select.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdarg.h>

#include "core_api.h"
#include "gpiodriver.h"

static void gpiodriver_list_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void gpiodriver_status_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

static char *cmd_gpiodriver_list_desc[] =
{
	"list all known gpio",
	NULL
};

static struct irc_command_description cmd_gpiodriver_list =
{
	.verb = "list",
	.description = cmd_gpiodriver_list_desc,
	.children = NULL,
	.callback = gpiodriver_list_handler,
	.ctx = NULL
};

static char *cmd_gpiodriver_status_desc[] =
{
	"status of a gpio, args : pin_number",
	NULL
};

static struct irc_command_description cmd_gpiodriver_status =
{
	.verb = "status",
	.description = cmd_gpiodriver_status_desc,
	.children = NULL,
	.callback = gpiodriver_status_handler,
	.ctx = NULL
};

static char *cmd_gpiodriver_desc[] =
{
	"gpio driver overview",
	NULL
};

static struct irc_command_description *cmd_gpiodriver_children[] =
{
	&cmd_gpiodriver_list,
	&cmd_gpiodriver_status,
	NULL
};

static struct irc_command_description cmd_gpiodriver =
{
	.verb = "gpiodriver",
	.description = cmd_gpiodriver_desc,
	.children = cmd_gpiodriver_children,
	.callback = NULL,
	.ctx = NULL
};

static struct irc_handler *cmd_gpiodriver_handler;
static struct irc_bot *corebot;

void commands_init()
{
	corebot = manager_get_bot();
	cmd_gpiodriver_handler = irc_bot_add_message_handler(corebot, 0, &cmd_gpiodriver);
}

void commands_terminate()
{
	irc_bot_remove_handler(corebot, cmd_gpiodriver_handler);
}

void gpiodriver_list_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	irc_bot_send_reply(bot, from, "not implemented");
}

void gpiodriver_status_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	irc_bot_send_reply(bot, from, "not implemented");
}
