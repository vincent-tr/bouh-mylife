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
#include <stdio.h>

#include "core_api.h"
#include "gpiodriver.h"

static void gpiodriver_status_all_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void gpiodriver_status_opened_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void gpiodriver_status(struct irc_bot *bot, struct irc_component *from, int all);
static int gpiodriver_status_item(int pin, struct gpio *gpio, void *ctx);

struct status_data
{
	struct irc_bot *bot;
	struct irc_component *target;
	int all;
};

static char *cmd_gpiodriver_status_all_desc[] =
{
	"status of all gpios",
	NULL
};

static struct irc_command_description cmd_gpiodriver_status_all =
{
	.verb = "all",
	.description = cmd_gpiodriver_status_all_desc,
	.children = NULL,
	.callback = gpiodriver_status_all_handler,
	.ctx = NULL
};

static char *cmd_gpiodriver_status_opened_desc[] =
{
	"status of opened gpios",
	NULL
};

static struct irc_command_description cmd_gpiodriver_status_opened =
{
	.verb = "opened",
	.description = cmd_gpiodriver_status_opened_desc,
	.children = NULL,
	.callback = gpiodriver_status_opened_handler,
	.ctx = NULL
};

static char *cmd_gpiodriver_status_desc[] =
{
	"status of gpios",
	NULL
};

static struct irc_command_description *cmd_gpiodriver_status_children[] =
{
	&cmd_gpiodriver_status_opened,
	&cmd_gpiodriver_status_all,
	NULL
};

static struct irc_command_description cmd_gpiodriver_status =
{
	.verb = "list",
	.description = cmd_gpiodriver_status_desc,
	.children = cmd_gpiodriver_status_children,
	.callback = NULL,
	.ctx = NULL
};

static char *cmd_gpiodriver_desc[] =
{
	"gpio driver overview",
	NULL
};

static struct irc_command_description *cmd_gpiodriver_children[] =
{
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

#define ITOA_LEN 15

static const char *types_string[] =
{
	"<invalid>",
	"io",
	"pwm"
};

void commands_init()
{
	corebot = manager_get_bot();
	cmd_gpiodriver_handler = irc_bot_add_message_handler(corebot, 0, &cmd_gpiodriver);
}

void commands_terminate()
{
	irc_bot_remove_handler(corebot, cmd_gpiodriver_handler);
}

void gpiodriver_status_all_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	gpiodriver_status(bot, from, 1);
}

void gpiodriver_status_opened_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	gpiodriver_status(bot, from, 0);
}

void gpiodriver_status(struct irc_bot *bot, struct irc_component *from, int all)
{
	struct status_data data;
	data.bot = bot;
	data.target = from;
	data.all = all;

	irc_bot_send_notice_va(bot, from, 3, "reply", "status", "listbegin");
	enum_all_gpios(gpiodriver_status_item, &data);
	irc_bot_send_notice_va(bot, from, 3, "reply", "status", "listend");
}

int gpiodriver_status_item(int pin, struct gpio *gpio, void *ctx)
{
	struct status_data *data = ctx;

	char apin[ITOA_LEN];
	snprintf(apin, ITOA_LEN, "%02d", pin);
	apin[ITOA_LEN-1] = '\0';

	if(gpio)
	{
		int gpionb;
		int type;
		char agpionb[ITOA_LEN];
		const char *atype;
		const char *usage;

		log_assert(gpio_ctl(gpio, GPIO_CTL_GET_GPIO_NUMBER, &gpionb));
		snprintf(agpionb, ITOA_LEN, "%02d", gpionb);
		agpionb[ITOA_LEN-1] = '\0';

		log_assert(gpio_ctl(gpio, GPIO_CTL_GET_TYPE, &type));
		atype = types_string[type];

		log_assert(gpio_ctl(gpio, GPIO_CTL_GET_USAGE, &usage));

		irc_bot_send_notice_va(data->bot, data->target, 9, "reply", "pin", apin, "gpio", agpionb, "type", atype, "usage", usage);
	}
	else if(data->all)
	{
		irc_bot_send_notice_va(data->bot, data->target, 3, "reply", "pin", apin);
	}

	return 1;
}
