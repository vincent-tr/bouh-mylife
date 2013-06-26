/*
 * manager.c
 *
 *  Created on: 22 juin 2013
 *      Author: pumbawoman
 */

#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <strings.h>

#include "irc.h"

static struct irc_bot *bot;

static void debug_complist_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static int debug_complist_item(struct irc_component *comp, void *ctx);

static struct irc_bot_callbacks callbacks =
{
	.on_connected = NULL,
	.on_disconnected = NULL,
	.on_comp_new = NULL,
	.on_comp_delete = NULL,
	.on_comp_change_status = NULL
};

struct debug_complist_data
{
	struct irc_bot *bot;
	struct irc_component *target;
};

static char *cmd_debug_complist_desc[] =
{
	"shows all other known components",
	NULL
};

struct irc_command_description cmd_debug_complist =
{
	.verb = "complist",
	.description = cmd_debug_complist_desc,
	.children = NULL,
	.callback = debug_complist_handler,
	.ctx = NULL
};

static char *cmd_debug_desc[] =
{
	"debug and testing commands",
	NULL
};

static struct irc_command_description *cmd_debug_children[] =
{
	&cmd_debug_complist,
	NULL
};

struct irc_command_description cmd_debug =
{
	.verb = "debug",
	.description = cmd_debug_desc,
	.children = cmd_debug_children,
	.callback = NULL,
	.ctx = NULL
};

void manager_init()
{
	bot = irc_bot_create("core", "core", &callbacks, NULL);

	irc_bot_add_message_handler(bot, 0, &cmd_debug);
}

void manager_terminate()
{
	irc_bot_delete(bot);
}

struct irc_bot *manager_get_bot()
{
	return bot;
}

void debug_complist_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	struct debug_complist_data data;
	data.bot = bot;
	data.target = from;
	irc_bot_send_notice_va(bot, from, 3, "reply", "complist", "listbegin");
	irc_comp_list(bot, debug_complist_item, &data);
	irc_bot_send_notice_va(bot, from, 3, "reply", "complist", "listend");

}

int debug_complist_item(struct irc_component *comp, void *ctx)
{
	struct debug_complist_data *data = ctx;
	const char *status = irc_comp_get_status(data->bot, comp);
	irc_bot_send_notice_va(bot, data->target, 12, "reply", "complist",
			"nick", irc_comp_get_nick(data->bot, comp),
			"host", irc_comp_get_host(data->bot, comp),
			"id", irc_comp_get_id(data->bot, comp),
			"type", irc_comp_get_type(data->bot, comp),
			"status", status ? status : "<no status>");
	return 1;
}
