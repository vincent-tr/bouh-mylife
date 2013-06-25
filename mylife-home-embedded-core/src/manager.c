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

static struct irc_bot_callbacks callbacks =
{
	.on_connected = NULL,
	.on_disconnected = NULL,
	.on_comp_new = NULL,
	.on_comp_delete = NULL,
	.on_comp_change_status = NULL
};

struct subverb_data
{

};

struct debug_complist_data
{
	struct irc_bot *bot;
	struct irc_component *target;
};

static void debug_handler(struct irc_bot *bot, struct irc_component *from, const char *verb, int broadcast, const char **args, int argc);
static int debug_complist(struct irc_component *comp, void *ctx);

static void module_handler(struct irc_bot *bot, struct irc_component *from, const char *verb, int broadcast, const char **args, int argc);

void manager_init()
{
	bot = irc_bot_create("core", "core", &callbacks, NULL);

	irc_bot_add_message_handler(bot, "debug", 0, debug_handler);
	irc_bot_add_message_handler(bot, "module", 0, module_handler);
}

void manager_terminate()
{
	irc_bot_delete(bot);
}

struct irc_bot *manager_get_bot()
{
	return bot;
}

void debug_handler(struct irc_bot *bot, struct irc_component *from, const char *verb, int broadcast, const char **args, int argc)
{
	if(argc < 1)
	{
		const char *args[] = {"not enough arguments"};
		irc_bot_send_notice(bot, from, "reply", args, sizeof(args) / sizeof(*args));
		return;
	}

	const char *subverb = args[0];

	if(!strcasecmp(subverb, "complist"))
	{
		struct debug_complist_data data;
		data.bot = bot;
		data.target = from;
		const char *args_begin[] = {"list begin"};
		irc_bot_send_notice(bot, from, "reply", args_begin, sizeof(args_begin) / sizeof(*args_begin));
		irc_comp_list(bot, debug_complist, &data);
		const char *args_end[] = {"list end"};
		irc_bot_send_notice(bot, from, "reply", args_end, sizeof(args_end) / sizeof(*args_end));
	}
	else
	{
		const char *args[] = {"unknown subverb"};
		irc_bot_send_notice(bot, from, "reply", args, sizeof(args) / sizeof(*args));
		return;
	}
}

int debug_complist(struct irc_component *comp, void *ctx)
{
	struct debug_complist_data *data = ctx;
	const char *args[] = {"nick", irc_comp_get_nick(data->bot, comp)};
	irc_bot_send_notice(bot, data->target, "reply", args, sizeof(args) / sizeof(*args));
	return 1;
}

void module_handler(struct irc_bot *bot, struct irc_component *from, const char *verb, int broadcast, const char **args, int argc)
{

}
