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

static struct irc_command_description cmd_debug_complist =
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

static struct irc_command_description cmd_debug =
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

static struct irc_command_description cmd_module_files =
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

static struct irc_command_description cmd_module_create =
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

static struct irc_command_description cmd_module_delete =
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

static struct irc_command_description cmd_module_loaded =
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

static struct irc_command_description cmd_module_load =
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

static struct irc_command_description cmd_module_unload =
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

static struct irc_command_description cmd_module =
{
	.verb = "module",
	.description = cmd_module_desc,
	.children = cmd_module_children,
	.callback = NULL,
	.ctx = NULL
};

/*************************************************************************
 * config
 */

static void config_read_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_writechar_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);;
static void config_writeint_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_writeint64_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_writestring_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_writebuffer_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_writechararray_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_writeintarray_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_writeint64array_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_writestringarray_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_enumsections_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_enumentries_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_deletesection_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void config_deleteentry_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

static char *cmd_config_read_desc[] =
{
	"read config entry, args : section, name",
	NULL
};

struct irc_command_description cmd_config_read =
{
	.verb = "read",
	.description = cmd_config_read_desc,
	.children = NULL,
	.callback = config_read_handler,
	.ctx = NULL
};

static char *cmd_config_writechar_desc[] =
{
	"write config entry of type char, args : section, name, value",
	NULL
};

static struct irc_command_description cmd_config_writechar =
{
	.verb = "writechar",
	.description = cmd_config_writechar_desc,
	.children = NULL,
	.callback = config_writechar_handler,
	.ctx = NULL
};

static char *cmd_config_writeint_desc[] =
{
	"write config entry of type int, args : section, name, value",
	NULL
};

static struct irc_command_description cmd_config_writeint =
{
	.verb = "writeint",
	.description = cmd_config_writeint_desc,
	.children = NULL,
	.callback = config_writeint_handler,
	.ctx = NULL
};

static char *cmd_config_writeint64_desc[] =
{
	"write config entry of type int64, args : section, name, value",
	NULL
};

static struct irc_command_description cmd_config_writeint64 =
{
	.verb = "writeint",
	.description = cmd_config_writeint64_desc,
	.children = NULL,
	.callback = config_writeint64_handler,
	.ctx = NULL
};

static char *cmd_config_writestring_desc[] =
{
	"write config entry of type string, args : section, name, value",
	NULL
};

static struct irc_command_description cmd_config_writestring =
{
	.verb = "writeint",
	.description = cmd_config_writestring_desc,
	.children = NULL,
	.callback = config_writestring_handler,
	.ctx = NULL
};

static char *cmd_config_writebuffer_desc[] =
{
	"write config entry of type buffer, args : section, name, value (hex)",
	NULL
};

static struct irc_command_description cmd_config_writebuffer =
{
	.verb = "writebuffer",
	.description = cmd_config_writebuffer_desc,
	.children = NULL,
	.callback = config_writebuffer_handler,
	.ctx = NULL
};

static char *cmd_config_writechararray_desc[] =
{
	"write config entry of type char array, args : section, name, values (1 arg per value => space separated)",
	NULL
};

static struct irc_command_description cmd_config_writechararray =
{
	.verb = "writechararray",
	.description = cmd_config_writechararray_desc,
	.children = NULL,
	.callback = config_writechararray_handler,
	.ctx = NULL
};

static char *cmd_config_writeintarray_desc[] =
{
	"write config entry of type int array, args : section, name, values (1 arg per value => space separated)",
	NULL
};

static struct irc_command_description cmd_config_writeintarray =
{
	.verb = "writeintarray",
	.description = cmd_config_writeintarray_desc,
	.children = NULL,
	.callback = config_writeintarray_handler,
	.ctx = NULL
};

static char *cmd_config_writeint64array_desc[] =
{
	"write config entry of type int64 array, args : section, name, values (1 arg per value => space separated)",
	NULL
};

static struct irc_command_description cmd_config_writeint64array =
{
	.verb = "writeint64array",
	.description = cmd_config_writeint64array_desc,
	.children = NULL,
	.callback = config_writeint64array_handler,
	.ctx = NULL
};

static char *cmd_config_writestringarray_desc[] =
{
	"write config entry of type string array, args : section, name, values (values are semi-colon separated ; )",
	NULL
};

static struct irc_command_description cmd_config_writestringarray =
{
	.verb = "writestringarray",
	.description = cmd_config_writestringarray_desc,
	.children = NULL,
	.callback = config_writestringarray_handler,
	.ctx = NULL
};

static char *cmd_config_enumsections_desc[] =
{
	"enumerate all sections in the configuration manager",
	NULL
};

static struct irc_command_description cmd_config_enumsections =
{
	.verb = "enumsections",
	.description = cmd_config_enumsections_desc,
	.children = NULL,
	.callback = config_enumsections_handler,
	.ctx = NULL
};

static char *cmd_config_enumentries_desc[] =
{
	"enumerate all entries from a section, args : section name",
	NULL
};

static struct irc_command_description cmd_config_enumentries =
{
	.verb = "enumentries",
	.description = cmd_config_enumentries_desc,
	.children = NULL,
	.callback = config_enumentries_handler,
	.ctx = NULL
};

static char *cmd_config_deletesection_desc[] =
{
	"delete an entire section from the configuration manager, args : section name",
	NULL
};

static struct irc_command_description cmd_config_deletesection =
{
	.verb = "deletesection",
	.description = cmd_config_deletesection_desc,
	.children = NULL,
	.callback = config_deletesection_handler,
	.ctx = NULL
};

static char *cmd_config_deleteentry_desc[] =
{
	"delete an entry from a section, args : section name, entry name",
	NULL
};

static struct irc_command_description cmd_config_deleteentry =
{
	.verb = "deleteentry",
	.description = cmd_config_deleteentry_desc,
	.children = NULL,
	.callback = config_deleteentry_handler,
	.ctx = NULL
};

static char *cmd_config_desc[] =
{
	"config management commands",
	NULL
};

static struct irc_command_description *cmd_config_children[] =
{
	&cmd_config_read,
	&cmd_config_writechar,
	&cmd_config_writeint,
	&cmd_config_writeint64,
	&cmd_config_writestring,
	&cmd_config_writebuffer,
	&cmd_config_writechararray,
	&cmd_config_writeintarray,
	&cmd_config_writeint64array,
	&cmd_config_writestringarray,
	&cmd_config_enumsections,
	&cmd_config_enumentries,
	&cmd_config_deletesection,
	&cmd_config_deleteentry,
	NULL
};

static struct irc_command_description cmd_config =
{
	.verb = "config",
	.description = cmd_config_desc,
	.children = cmd_config_children,
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
	irc_bot_add_message_handler(bot, 0, &cmd_config);
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
	// TODO
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

	// TODO : add to config autoload ?

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

	// TODO : remove from config autoload ?

	irc_bot_send_notice_va(bot, from, 2, "reply", "module unloaded");
}

// TODO : int ret = readparameters(bot, from, paramcount, mandatorycount, "param1", &ptr1, "param2", &ptr2, ...);
// => auto send missing parameters and auto assign ptr to args, ret = 1 on success, 0 on failure
