/*
 * commands.c
 *
 *  Created on: 4 juil. 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include <sys/select.h>
#include <unistd.h>
#include <sys/reboot.h>
#include <linux/reboot.h>
#include <errno.h>
#include <string.h>

#include "core_api.h"
#include "irc.h"
#include "manager.h"

static void sysadmin_reboot_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void sysadmin_shutdown_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void sysadmin_stopcore_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

static int system_reboot(int cmd);

static char *cmd_sysadmin_reboot_desc[] =
{
	"reboot the whole system",
	NULL
};

static struct irc_command_description cmd_sysadmin_reboot =
{
	.verb = "reboot",
	.description = cmd_sysadmin_reboot_desc,
	.children = NULL,
	.callback = sysadmin_reboot_handler,
	.ctx = NULL
};

static char *cmd_sysadmin_shutdown_desc[] =
{
	"shutdown the whole system",
	NULL
};

static struct irc_command_description cmd_sysadmin_shutdown =
{
	.verb = "shutdown",
	.description = cmd_sysadmin_shutdown_desc,
	.children = NULL,
	.callback = sysadmin_shutdown_handler,
	.ctx = NULL
};

static char *cmd_sysadmin_stopcore_desc[] =
{
	"stop the core",
	NULL
};

static struct irc_command_description cmd_sysadmin_stopcore =
{
	.verb = "stopcore",
	.description = cmd_sysadmin_stopcore_desc,
	.children = NULL,
	.callback = sysadmin_stopcore_handler,
	.ctx = NULL
};

static char *cmd_sysadmin_desc[] =
{
	"system administration and testing commands",
	NULL
};

static struct irc_command_description *cmd_sysadmin_children[] =
{
	&cmd_sysadmin_reboot,
	&cmd_sysadmin_shutdown,
	&cmd_sysadmin_stopcore,
	NULL
};

static struct irc_command_description cmd_sysadmin =
{
	.verb = "sysadmin",
	.description = cmd_sysadmin_desc,
	.children = cmd_sysadmin_children,
	.callback = NULL,
	.ctx = NULL
};

static struct irc_handler *cmd_sysadmin_handler;
static struct irc_bot *corebot;

void commands_init()
{
	corebot = manager_get_bot();
	cmd_sysadmin_handler = irc_bot_add_message_handler(corebot, 0, &cmd_sysadmin);
}

void commands_terminate()
{
	irc_bot_remove_handler(corebot, cmd_sysadmin_handler);
}

void sysadmin_reboot_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{

	if(system_reboot(LINUX_REBOOT_CMD_RESTART) == -1)
	{
		irc_bot_send_reply(bot, from, "error rebooting : %s", strerror(errno));
		return;
	}

	irc_bot_send_reply(bot, from, "rebooting");
}

void sysadmin_shutdown_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	if(system_reboot(LINUX_REBOOT_CMD_POWER_OFF) == -1)
	{
		irc_bot_send_reply(bot, from, "error shutting down : %s", strerror(errno));
		return;
	}

	irc_bot_send_reply(bot, from, "shutting down");
}

void sysadmin_stopcore_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	loop_exit();
	irc_bot_send_reply(bot, from, "stopping core");
}

int system_reboot(int cmd)
{
	sync();
	return reboot(cmd);
}
