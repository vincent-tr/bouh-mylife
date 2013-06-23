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

#include "irc.h"

struct irc_bot *bot;

struct irc_bot_callbacks callbacks =
{
	.on_connected = NULL,
	.on_disconnected = NULL,
	.on_comp_new = NULL,
	.on_comp_delete = NULL,
	.on_comp_change_status = NULL
};

static void debug_handler(struct irc_bot *bot, struct irc_component *from, const char *verb, int broadcast, const char **args, int argc);

void manager_init()
{
	bot = irc_bot_create("core", "core", &callbacks, NULL);

	irc_bot_add_message_handler(bot, "debug", 0, debug_handler);
}

void manager_terminate()
{
	irc_bot_delete(bot);
}

void debug_handler(struct irc_bot *bot, struct irc_component *from, const char *verb, int broadcast, const char **args, int argc)
{
	if(argc < 1)
	{
		const char *args[] = {"not enough arguments"};
		irc_bot_send_notice(bot, from, "reply", args, sizeof(args));
	}
}
