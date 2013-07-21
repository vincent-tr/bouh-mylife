/*
 * component.c
 *
 *  Created on: 21 juil. 2013
 *      Author: pumbawoman
 */


#define _BSD_SOURCE
#include <stddef.h>
#include <stdio.h>
#include <sys/select.h>
#include <stdarg.h>
#include <limits.h>
#include <string.h>

#include <mpd/client.h>
#include <mpd/status.h>

#include "core_api.h"
#include "tools.h"
#include "component.h"

struct component
{
	char *id;
	struct irc_bot *bot;

	struct mpd_connection *mpd_con;
	struct mpd_async *mpd_async;

	enum mpd_state state;
	int volume;
};

static const char *ctype_bot = "mpd";

static struct irc_bot_callbacks callbacks =
{
	.on_connected = NULL,
	.on_disconnected = NULL,
	.on_comp_new = NULL,
	.on_comp_delete = NULL,
	.on_comp_change_status = NULL
};

static int change_status(struct component *comp, enum mpd_state state, int volume);
static void setstate_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void setvolume_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

static char *cmd_setstate_desc[] =
{
	"set the state, args : [play, stop, pause]",
	NULL
};

static struct irc_command_description cmd_setstate =
{
	.verb = "setstate",
	.description = cmd_setstate_desc,
	.children = NULL,
	.callback = setstate_handler,
	.ctx = NULL
};

static char *cmd_setvolume_desc[] =
{
	"set the volume, args : volume (0 .. 100)",
	NULL
};

static struct irc_command_description cmd_setvolume =
{
	.verb = "setvolume",
	.description = cmd_setvolume_desc,
	.children = NULL,
	.callback = setvolume_handler,
	.ctx = NULL
};

void component_init()
{

}

void component_terminate()
{

}

struct component *component_create(const char *id, const char *server_address)
{
	if(!id)
		return error_failed_ptr(ERROR_CORE_INVAL);

	struct component *comp;
	malloc_nofail(comp);
	strdup_nofail(comp->id, id);

	// TODO

	log_assert(comp->bot = irc_bot_create(comp->id, ctype_bot, &callbacks, comp));
	log_assert(irc_bot_add_message_handler(comp->bot, 0, &cmd_setstate));
	log_assert(irc_bot_add_message_handler(comp->bot, 0, &cmd_setvolume));
	log_assert(change_status(comp, state, volume));

	error_success();
	return comp;
}

void component_delete(struct component *comp, int delete_config)
{
	irc_bot_delete(comp->bot);

	mpd_connection_free(comp->mpd_con);

	free(comp->id);
	free(comp);
}

const char *component_get_id(struct component *comp)
{
	return comp->id;
}

int change_status(struct component *comp, enum mpd_state state, int volume)
{
	char configentry[CONFIG_ENTRY_SIZE];
	char status[20];

	if(r < 0 || r > 255)
		return error_failed(ERROR_CORE_INVAL);
	if(g < 0 || g > 255)
		return error_failed(ERROR_CORE_INVAL);
	if(b < 0 || b > 255)
		return error_failed(ERROR_CORE_INVAL);

	comp->value_red = r;
	log_assert(gpio_ctl(comp->gpio_red, GPIO_CTL_SET_PULSE, (comp->value_red * GPIO_PERIOD / VALUE_MAX)));
	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.red", comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	log_assert(config_write_int(CONFIG_SECTION, configentry, comp->value_red));

	comp->value_green = g;
	log_assert(gpio_ctl(comp->gpio_green, GPIO_CTL_SET_PULSE, (comp->value_green * GPIO_PERIOD / VALUE_MAX)));
	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.green", comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	log_assert(config_write_int(CONFIG_SECTION, configentry, comp->value_green));

	comp->value_blue = b;
	log_assert(gpio_ctl(comp->gpio_blue, GPIO_CTL_SET_PULSE, (comp->value_blue * GPIO_PERIOD / VALUE_MAX)));
	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.blue", comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	log_assert(config_write_int(CONFIG_SECTION, configentry, comp->value_blue));

	snprintf(status, 20, "%s-%03d", comp->value_red, comp->volume);
	irc_bot_set_comp_status(comp->bot, status);

	return error_success();
}

void setstate_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *red;
	const char *green;
	const char *blue;
	int r;
	int g;
	int b;

	if(!irc_bot_read_parameters(bot, from, args, argc, &red, &green, &blue))
		return;

	if(sscanf(red, "%d", &r) != 1 || sscanf(green, "%d", &g) != 1 || sscanf(blue, "%d", &b) != 1)
	{
		error_last = ERROR_CORE_INVAL;
		irc_bot_send_reply_from_error(bot, from, "setvalue");
		return;
	}

	struct component *comp = irc_bot_get_ctx(bot);
	change_status(comp, r, g, b);
	irc_bot_send_reply_from_error(bot, from, "setvalue");

}

void setvolume_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *red;
	const char *green;
	const char *blue;
	int r;
	int g;
	int b;

	if(!irc_bot_read_parameters(bot, from, args, argc, &red, &green, &blue))
		return;

	if(sscanf(red, "%d", &r) != 1 || sscanf(green, "%d", &g) != 1 || sscanf(blue, "%d", &b) != 1)
	{
		error_last = ERROR_CORE_INVAL;
		irc_bot_send_reply_from_error(bot, from, "setvalue");
		return;
	}

	struct component *comp = irc_bot_get_ctx(bot);
	change_status(comp, r, g, b);
	irc_bot_send_reply_from_error(bot, from, "setvalue");

}
