/*
 * component.c
 *
 *  Created on: 9 juil. 2013
 *      Author: pumbawoman
 */

#define _BSD_SOURCE
#include <stddef.h>
#include <stdio.h>
#include <sys/select.h>
#include <stdarg.h>
#include <limits.h>
#include <string.h>

#include "core_api.h"
#include "tools.h"
#include "gpiodriver_api.h"
#include "component.h"

struct component
{
	char *id;
	struct irc_bot *bot;

	struct gpio *gpio_red;
	struct gpio *gpio_green;
	struct gpio *gpio_blue;

	int value_red; // 0 .. 255
	int value_green; // 0 .. 255
	int value_blue; // 0 .. 255
};

const char *ctype = "ctype.outrgb";

static struct irc_bot_callbacks callbacks =
{
	.on_connected = NULL,
	.on_disconnected = NULL,
	.on_comp_new = NULL,
	.on_comp_delete = NULL,
	.on_comp_change_status = NULL
};

static int change_status(struct component *comp, int r, int g, int b);
static void setvalue_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

#define GPIO_PERIOD 10000
#define VALUE_MAX 255
#define CONFIG_ENTRY_SIZE 128

static char *cmd_setvalue_desc[] =
{
	"set the rgb value, args : red, green, blue (0 .. 255 each)",
	NULL
};

static struct irc_command_description cmd_setvalue =
{
	.verb = "setvalue",
	.description = cmd_setvalue_desc,
	.children = NULL,
	.callback = setvalue_handler,
	.ctx = NULL
};

void component_init()
{

}

void component_terminate()
{

}

struct component *component_create(const char *id, int pin_red, int pin_green, int pin_blue)
{
	char configentry[CONFIG_ENTRY_SIZE];

	if(!id)
		return error_failed_ptr(ERROR_CORE_INVAL);

	struct component *comp;
	malloc_nofail(comp);
	strdup_nofail(comp->id, id);

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.%s.red", ctype, comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	if(!(comp->gpio_red = gpio_open(pin_red, ctype, GPIO_TYPE_PWM)))
	{
		free(comp->id);
		free(comp);
		return NULL;
	}

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.%s.green", ctype, comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	if(!(comp->gpio_green = gpio_open(pin_green, ctype, GPIO_TYPE_PWM)))
	{
		gpio_close(comp->gpio_red);
		free(comp->id);
		free(comp);
		return NULL;
	}

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.%s.blue", ctype, comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	if(!(comp->gpio_blue = gpio_open(pin_blue, ctype, GPIO_TYPE_PWM)))
	{
		gpio_close(comp->gpio_red);
		gpio_close(comp->gpio_green);
		free(comp->id);
		free(comp);
		return NULL;
	}

	log_assert(gpio_ctl(comp->gpio_red, GPIO_CTL_SET_PERIOD, GPIO_PERIOD));
	log_assert(gpio_ctl(comp->gpio_green, GPIO_CTL_SET_PERIOD, GPIO_PERIOD));
	log_assert(gpio_ctl(comp->gpio_blue, GPIO_CTL_SET_PERIOD, GPIO_PERIOD));

	int r, g, b;

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.red", comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	if(!config_read_int(ctype, configentry, &r))
		r = 0;

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.green", comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	if(!config_read_int(ctype, configentry, &g))
		g = 0;

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.blue", comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	if(!config_read_int(ctype, configentry, &b))
		b = 0;

	log_assert(comp->bot = irc_bot_create(comp->id, ctype, &callbacks, comp));
	log_assert(irc_bot_add_message_handler(comp->bot, 0, &cmd_setvalue));
	log_assert(change_status(comp, r, g, b));

	error_success();
	return comp;
}

void component_delete(struct component *comp, int delete_config)
{
	irc_bot_delete(comp->bot);

	char configentry[CONFIG_ENTRY_SIZE];
	if(delete_config)
	{
		snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.red", comp->id);
		configentry[CONFIG_ENTRY_SIZE-1] = '\0';
		config_delete_entry(ctype, configentry);

		snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.green", comp->id);
		configentry[CONFIG_ENTRY_SIZE-1] = '\0';
		config_delete_entry(ctype, configentry);

		snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.blue", comp->id);
		configentry[CONFIG_ENTRY_SIZE-1] = '\0';
		config_delete_entry(ctype, configentry);
	}

	log_assert(gpio_ctl(comp->gpio_red, GPIO_CTL_SET_PULSE, 0));
	log_assert(gpio_ctl(comp->gpio_green, GPIO_CTL_SET_PULSE, 0));
	log_assert(gpio_ctl(comp->gpio_blue, GPIO_CTL_SET_PULSE, 0));

	log_assert(gpio_ctl(comp->gpio_red, GPIO_CTL_SET_PERIOD, 0));
	log_assert(gpio_ctl(comp->gpio_green, GPIO_CTL_SET_PERIOD, 0));
	log_assert(gpio_ctl(comp->gpio_blue, GPIO_CTL_SET_PERIOD, 0));

	gpio_close(comp->gpio_red);
	gpio_close(comp->gpio_green);
	gpio_close(comp->gpio_blue);

	free(comp->id);
	free(comp);
}

const char *component_get_id(struct component *comp)
{
	return comp->id;
}

int change_status(struct component *comp, int r, int g, int b)
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
	log_assert(config_write_int(ctype, configentry, comp->value_red));

	comp->value_green = g;
	log_assert(gpio_ctl(comp->gpio_green, GPIO_CTL_SET_PULSE, (comp->value_green * GPIO_PERIOD / VALUE_MAX)));
	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.green", comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	log_assert(config_write_int(ctype, configentry, comp->value_green));

	comp->value_blue = b;
	log_assert(gpio_ctl(comp->gpio_blue, GPIO_CTL_SET_PULSE, (comp->value_blue * GPIO_PERIOD / VALUE_MAX)));
	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.blue", comp->id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	log_assert(config_write_int(ctype, configentry, comp->value_blue));

	snprintf(status, 20, "%03d-%03d-%03d", comp->value_red, comp->value_green, comp->value_blue);
	irc_bot_set_comp_status(comp->bot, status);

	return error_success();
}

void setvalue_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
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
