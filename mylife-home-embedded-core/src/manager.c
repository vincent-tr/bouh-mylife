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
#include "module.h"

static struct irc_bot *bot;

static struct irc_bot_callbacks callbacks =
{
	.on_connected = NULL,
	.on_disconnected = NULL,
	.on_comp_new = NULL,
	.on_comp_delete = NULL,
	.on_comp_change_status = NULL
};

struct list_data
{
	struct irc_bot *bot;
	struct irc_component *target;
	void *ctxdata;
};

static int debug_complist_item(struct irc_component *comp, void *ctx);
static int module_files_item(const char *file, void *ctx);
static int module_loaded_item(struct module *mod, void *ctx);
static int module_loaded_ref_item(struct module *mod, void *ctx);
static int module_loaded_refby_item(struct module *mod, void *ctx);

/*************************************************************************
 * debug
 */

static void debug_complist_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

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

/*************************************************************************
 * module
 */

static void module_files_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void module_create_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void module_delete_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void module_loaded_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void module_load_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void module_unload_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

static char *cmd_module_files_desc[] =
{
	"show all files modules : they can be loaded or not but they are available",
	NULL
};

struct irc_command_description cmd_module_files =
{
	.verb = "files",
	.description = cmd_module_files_desc,
	.children = NULL,
	.callback = module_files_handler,
	.ctx = NULL
};

static char *cmd_module_create_desc[] =
{
	"create a module file", // TODO args
	NULL
};

struct irc_command_description cmd_module_create =
{
	.verb = "create",
	.description = cmd_module_create_desc,
	.children = NULL,
	.callback = module_create_handler,
	.ctx = NULL
};

static char *cmd_module_delete_desc[] =
{
	"delete a module file, arg : file",
	NULL
};

struct irc_command_description cmd_module_delete =
{
	.verb = "delete",
	.description = cmd_module_delete_desc,
	.children = NULL,
	.callback = module_delete_handler,
	.ctx = NULL
};

static char *cmd_module_loaded_desc[] =
{
	"show all loaded modules",
	NULL
};

struct irc_command_description cmd_module_loaded =
{
	.verb = "loaded",
	.description = cmd_module_loaded_desc,
	.children = NULL,
	.callback = module_loaded_handler,
	.ctx = NULL
};

static char *cmd_module_load_desc[] =
{
	"load a module, arg : file",
	NULL
};

struct irc_command_description cmd_module_load =
{
	.verb = "load",
	.description = cmd_module_load_desc,
	.children = NULL,
	.callback = module_load_handler,
	.ctx = NULL
};

static char *cmd_module_unload_desc[] =
{
	"unload a module, arg : name",
	NULL
};

struct irc_command_description cmd_module_unload =
{
	.verb = "unload",
	.description = cmd_module_unload_desc,
	.children = NULL,
	.callback = module_unload_handler,
	.ctx = NULL
};

static char *cmd_module_desc[] =
{
	"module management commands",
	NULL
};

static struct irc_command_description *cmd_module_children[] =
{
	&cmd_module_files,
	&cmd_module_create,
	&cmd_module_delete,
	&cmd_module_loaded,
	&cmd_module_load,
	&cmd_module_unload,
	NULL
};

struct irc_command_description cmd_module =
{
	.verb = "module",
	.description = cmd_module_desc,
	.children = cmd_module_children,
	.callback = NULL,
	.ctx = NULL
};

/*************************************************************************
 */

void manager_init()
{
	bot = irc_bot_create("core", "core", &callbacks, NULL);

	irc_bot_add_message_handler(bot, 0, &cmd_debug);
	irc_bot_add_message_handler(bot, 0, &cmd_module);
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
	struct list_data data;
	data.bot = bot;
	data.target = from;
	data.ctxdata = NULL;
	irc_bot_send_notice_va(bot, from, 3, "reply", "complist", "listbegin");
	irc_comp_list(bot, debug_complist_item, &data);
	irc_bot_send_notice_va(bot, from, 3, "reply", "complist", "listend");

}

int debug_complist_item(struct irc_component *comp, void *ctx)
{
	struct list_data *data = ctx;
	const char *status = irc_comp_get_status(data->bot, comp);
	irc_bot_send_notice_va(bot, data->target, 12, "reply", "complist",
			"nick", irc_comp_get_nick(data->bot, comp),
			"host", irc_comp_get_host(data->bot, comp),
			"id", irc_comp_get_id(data->bot, comp),
			"type", irc_comp_get_type(data->bot, comp),
			"status", status ? status : "<no status>");
	return 1;
}

void module_files_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	struct list_data data;
	data.bot = bot;
	data.target = from;
	data.ctxdata = NULL;
	irc_bot_send_notice_va(bot, from, 3, "reply", "modulefiles", "listbegin");
	module_enum_files(module_files_item, &data);
	irc_bot_send_notice_va(bot, from, 3, "reply", "modulefiles", "listend");
}

int module_files_item(const char *file, void *ctx)
{
	struct list_data *data = ctx;
	irc_bot_send_notice_va(data->bot, data->target, 3, "reply", "modulefiles", file);
	return 1;
}

void module_create_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	irc_bot_send_notice_va(bot, from, 2, "reply", "not implemented!");
}

void module_delete_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	if(!argc)
	{
		irc_bot_send_notice_va(bot, from, 2, "reply", "missing parameter : file");
		return;
	}

	const char *file = args[0];

	if(!module_delete(file))
	{
		irc_bot_send_notice_va(bot, from, 2, "reply", "error deleting file");
		return;
	}

	irc_bot_send_notice_va(bot, from, 2, "reply", "module file deleted");
}

void module_loaded_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	struct list_data data;
	data.bot = bot;
	data.target = from;
	data.ctxdata = NULL;
	irc_bot_send_notice_va(bot, from, 3, "reply", "moduleloaded", "listbegin");
	module_enum_loaded(module_loaded_item, &data);
	irc_bot_send_notice_va(bot, from, 3, "reply", "moduleloaded", "listend");
}

int module_loaded_item(struct module *mod, void *ctx)
{
	struct list_data *data = ctx;

	const char *file = module_get_file(mod);
	const struct module_name *name = module_get_name(mod);

	const int version_max = 30;
	char version[version_max];
	snprintf(version, version_max, "v%d.%d", name->major, name->minor);
	version[version_max-1] = '\0';

	irc_bot_send_notice_va(data->bot, data->target, 3, "reply", "moduleloaded", name->name);
	irc_bot_send_notice_va(data->bot, data->target, 5, "reply", "moduleloaded", name->name, "version", version);
	if(file)
		irc_bot_send_notice_va(data->bot, data->target, 5, "reply", "moduleloaded", name->name, "file", file);

	struct list_data subdata;
	subdata.bot = data->bot;
	subdata.target = data->target;
	subdata.ctxdata = (void *)name;

	module_enum_ref(mod, module_loaded_ref_item, &subdata);
	module_enum_refby(mod, module_loaded_refby_item, &subdata);

	return 1;
}

int module_loaded_ref_item(struct module *mod, void *ctx)
{
	struct list_data *data = ctx;
	const struct module_name *name = data->ctxdata;
	const struct module_name *refname = module_get_name(mod);

	irc_bot_send_notice_va(data->bot, data->target, 5, "reply", "moduleloaded", name->name, "ref", refname->name);

	return 1;
}

int module_loaded_refby_item(struct module *mod, void *ctx)
{
	struct list_data *data = ctx;
	const struct module_name *name = data->ctxdata;
	const struct module_name *refbyname = module_get_name(mod);

	irc_bot_send_notice_va(data->bot, data->target, 5, "reply", "moduleloaded", name->name, "refby", refbyname->name);

	return 1;
}

void module_load_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	if(!argc)
	{
		irc_bot_send_notice_va(bot, from, 2, "reply", "missing parameter : file");
		return;
	}

	const char *file = args[0];

	if(!module_load(file))
	{
		irc_bot_send_notice_va(bot, from, 2, "reply", "error loading module");
		return;
	}

	irc_bot_send_notice_va(bot, from, 2, "reply", "module loaded");
}

void module_unload_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	if(!argc)
	{
		irc_bot_send_notice_va(bot, from, 2, "reply", "missing parameter : name");
		return;
	}

	const char *name = args[0];

	struct module *mod = module_find_by_name(name);
	if(!mod)
	{
		irc_bot_send_notice_va(bot, from, 2, "reply", "error module not found");
		return;
	}

	if(!module_unload(mod))
	{
		irc_bot_send_notice_va(bot, from, 2, "reply", "error unloading module");
		return;
	}

	irc_bot_send_notice_va(bot, from, 2, "reply", "module unloaded");
}
