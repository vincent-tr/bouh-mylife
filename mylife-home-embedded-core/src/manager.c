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
	.on_comp_change_status = NULL,
	.on_message = NULL,
	.on_notice = NULL
};


void manager_init()
{
	bot = irc_bot_create("core", "core", &callbacks, NULL);
}

void manager_terminate()
{
	irc_bot_delete(bot);
}
