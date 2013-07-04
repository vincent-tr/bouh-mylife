/*
 * manager.c
 *
 *  Created on: 22 juin 2013
 *      Author: pumbawoman
 */

#define _BSD_SOURCE
#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <strings.h>
#include <limits.h>

#include "irc.h"
#include "module.h"
#include "config.h"
#include "tools.h"

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
static int config_enum_section_item(const char *section, void *ctx);
static int config_enum_entry_item(const char *name, void *ctx);
static void manager_load_startup_modules();
static void manager_add_startup_module(const char *file);
static void manager_remove_startup_module(const char *file);

#define CONFIG_SECTION "manager"
#define CONFIG_ENTRY "startup_modules"

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
	"write config entry of type char, args : section, name, value (hex)",
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
	.verb = "writeint64",
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
	.verb = "writestring",
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
	"write config entry of type char array, args : section, name, values (Hex comma separated)",
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
	"write config entry of type int array, args : section, name, values (comma separated)",
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
	"write config entry of type int64 array, args : section, name, values (comma separated)",
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
	"write config entry of type string array, args : section, name, values (comma separated)",
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

	manager_load_startup_modules();
}

void manager_terminate()
{
	irc_bot_delete(bot);
}

struct irc_bot *manager_get_bot()
{
	return bot;
}

void manager_load_startup_modules()
{
	size_t count;
	char **array;

	if(!config_read_string_array(CONFIG_SECTION, CONFIG_ENTRY, &count, &array))
		return; // no config => no modules

	for(size_t i=0; i<count; i++)
	{
		const char *file = array[i];
		if(!module_load(file))
			log_warning("error loading module '%s'", file);
	}
}

void manager_add_startup_module(const char *file)
{
	size_t count;
	char **array_old;
	const char **array_new;

	if(!config_read_string_array(CONFIG_SECTION, CONFIG_ENTRY, &count, &array_old))
	{
		count = 0;
		array_old = NULL;
	}

	malloc_array_nofail(array_new, count+1);

	if(array_old)
		memcpy(array_new, array_old, count*sizeof(*array_new));
	array_new[count++] = file; // last item

	config_write_string_array(CONFIG_SECTION, CONFIG_ENTRY, count, array_new);

	if(array_old)
		free(array_old);
	free(array_new);
}

void manager_remove_startup_module(const char *file)
{
	size_t count;
	size_t idx = (size_t)(-1);
	char **array;

	if(!config_read_string_array(CONFIG_SECTION, CONFIG_ENTRY, &count, &array))
		return; // no config => nothing to remove

	for(size_t i=0; i<count; i++)
	{
		// find index
		if(!strcasecmp(array[i], file))
		{
			idx = i;
			break;
		}
	}

	if(idx == (size_t)(-1))
	{
		free(array);
		return; // not found
	}

	// moving the item after index
	for(size_t i=idx+1; i<count; i++)
		array[i-1] = array[i];

	config_write_string_array(CONFIG_SECTION, CONFIG_ENTRY, count-1, (const char **)array);

	free(array);
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
	irc_bot_send_reply(bot, from, "not implemented!");
}

void module_delete_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *file;
	if(!irc_bot_read_parameters(bot, from, args, argc, &file))
		return;

	if(!module_delete(file))
	{
		irc_bot_send_reply(bot, from, "error deleting file");
		return;
	}

	irc_bot_send_reply(bot, from, "module file deleted");
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
	const char *file;
	if(!irc_bot_read_parameters(bot, from, args, argc, &file))
		return;

	if(!module_load(file))
	{
		irc_bot_send_reply(bot, from, "error loading module");
		return;
	}

	manager_add_startup_module(file);

	irc_bot_send_reply(bot, from, "module loaded");
}

void module_unload_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *name;
	if(!irc_bot_read_parameters(bot, from, args, argc, &name))
		return;

	struct module *mod = module_find_by_name(name);
	if(!mod)
	{
		irc_bot_send_reply(bot, from, "error module not found");
		return;
	}

	const char *modfile = module_get_file(mod);
	char *file;
	strdup_nofail(file, modfile);

	if(!module_unload(mod))
	{
		irc_bot_send_reply(bot, from, "error unloading module");
		return;
	}

	manager_remove_startup_module(file);
	free(file);

	irc_bot_send_reply(bot, from, "module unloaded");
}

void config_read_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name))
		return;

	enum config_type type;
	if(!config_get_entry_type(section, name, &type))
	{
		irc_bot_send_reply(bot, from, "error reading entry");
		return;
	}

	char cval;
	int ival;
	long long llval;
	char *sval;
	void *bval;
	size_t blen;
	char *acval;
	int *aival;
	long long *allval;
	char **asval;
	size_t alen;

	size_t i;
	size_t off;

#define BUFFER_SIZE 100
	static char buffer[BUFFER_SIZE];

	switch(type)
	{
	case CHAR:
		if(!config_read_char(section, name, &cval))
		{
			irc_bot_send_reply(bot, from, "error reading entry");
			return;
		}

		snprintf(buffer, BUFFER_SIZE, "%c", cval);
		buffer[BUFFER_SIZE - 1] = '\0';

		irc_bot_send_notice_va(bot, from, 3, "reply", "type", "char");
		irc_bot_send_notice_va(bot, from, 3, "reply", "value", buffer);

		break;

	case INT:
		if(!config_read_int(section, name, &ival))
		{
			irc_bot_send_reply(bot, from, "error reading entry");
			return;
		}

		snprintf(buffer, BUFFER_SIZE, "%i", ival);
		buffer[BUFFER_SIZE - 1] = '\0';

		irc_bot_send_notice_va(bot, from, 3, "reply", "type", "int");
		irc_bot_send_notice_va(bot, from, 3, "reply", "value", buffer);

		break;

	case INT64:
		if(!config_read_int64(section, name, &llval))
		{
			irc_bot_send_reply(bot, from, "error reading entry");
			return;
		}

		snprintf(buffer, BUFFER_SIZE, "%lld", llval);
		buffer[BUFFER_SIZE - 1] = '\0';

		irc_bot_send_notice_va(bot, from, 3, "reply", "type", "int64");
		irc_bot_send_notice_va(bot, from, 3, "reply", "value", buffer);

		break;

	case STRING:
		if(!config_read_string(section, name, &sval))
		{
			irc_bot_send_reply(bot, from, "error reading entry");
			return;
		}

		snprintf(buffer, BUFFER_SIZE, "%s", sval);
		buffer[BUFFER_SIZE - 1] = '\0';

		free(sval);

		irc_bot_send_notice_va(bot, from, 3, "reply", "type", "string");
		irc_bot_send_notice_va(bot, from, 3, "reply", "value", buffer);
		break;

	case BUFFER:
		if(!config_read_buffer(section, name, &bval, &blen))
		{
			irc_bot_send_reply(bot, from, "error reading entry");
			return;
		}

		snprintf(buffer, BUFFER_SIZE, "%i", blen);
		buffer[BUFFER_SIZE - 1] = '\0';

		irc_bot_send_notice_va(bot, from, 3, "reply", "type", "buffer");
		irc_bot_send_notice_va(bot, from, 3, "reply", "len", buffer);

		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listbegin");

		// 20/line
		off = 0;
		buffer[0] = '\0';
		for(i=0; i<blen; i++)
		{
			char part[3];
			sprintf(part, "%hhX", ((char*)bval)[i]);

			if(*buffer != '\0')
				strcat(buffer, " ");
			strcat(buffer, part);
			++off;

			if(off == 20)
			{
				irc_bot_send_notice_va(bot, from, 3, "reply", "value", buffer);
				off = 0;
				*buffer = '\0' ;
			}
		}

		if(off > 0)
			irc_bot_send_notice_va(bot, from, 3, "reply", "value", buffer);

		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listend");

		free(bval);

		break;

	case CHAR_ARRAY:
		if(!config_read_char_array(section, name, &alen, &acval))
		{
			irc_bot_send_reply(bot, from, "error reading entry");
			return;
		}

		snprintf(buffer, BUFFER_SIZE, "%i", alen);
		buffer[BUFFER_SIZE - 1] = '\0';

		irc_bot_send_notice_va(bot, from, 3, "reply", "type", "char_array");
		irc_bot_send_notice_va(bot, from, 3, "reply", "len", buffer);

		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listbegin");

		for(i=0; i<alen; i++)
		{
			snprintf(buffer, BUFFER_SIZE, "%c", acval[i]);
			buffer[BUFFER_SIZE - 1] = '\0';
			irc_bot_send_notice_va(bot, from, 3, "reply", "value", buffer);
		}
		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listend");

		free(acval);

		break;

	case INT_ARRAY:
		if(!config_read_int_array(section, name, &alen, &aival))
		{
			irc_bot_send_reply(bot, from, "error reading entry");
			return;
		}

		snprintf(buffer, BUFFER_SIZE, "%i", alen);
		buffer[BUFFER_SIZE - 1] = '\0';

		irc_bot_send_notice_va(bot, from, 3, "reply", "type", "int_array");
		irc_bot_send_notice_va(bot, from, 3, "reply", "len", buffer);

		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listbegin");

		for(i=0; i<alen; i++)
		{
			snprintf(buffer, BUFFER_SIZE, "%i", aival[i]);
			buffer[BUFFER_SIZE - 1] = '\0';
			irc_bot_send_notice_va(bot, from, 3, "reply", "value", buffer);
		}
		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listend");

		free(aival);
		break;

	case INT64_ARRAY:
		if(!config_read_int64_array(section, name, &alen, &allval))
		{
			irc_bot_send_reply(bot, from, "error reading entry");
			return;
		}

		snprintf(buffer, BUFFER_SIZE, "%i", alen);
		buffer[BUFFER_SIZE - 1] = '\0';

		irc_bot_send_notice_va(bot, from, 3, "reply", "type", "int64_array");
		irc_bot_send_notice_va(bot, from, 3, "reply", "len", buffer);

		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listbegin");

		for(i=0; i<alen; i++)
		{
			snprintf(buffer, BUFFER_SIZE, "%lld", allval[i]);
			buffer[BUFFER_SIZE - 1] = '\0';
			irc_bot_send_notice_va(bot, from, 3, "reply", "value", buffer);
		}
		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listend");

		free(allval);
		break;

	case STRING_ARRAY:
		if(!config_read_string_array(section, name, &alen, &asval))
		{
			irc_bot_send_reply(bot, from, "error reading entry");
			return;
		}

		snprintf(buffer, BUFFER_SIZE, "%i", alen);
		buffer[BUFFER_SIZE - 1] = '\0';

		irc_bot_send_notice_va(bot, from, 3, "reply", "type", "string_array");
		irc_bot_send_notice_va(bot, from, 3, "reply", "len", buffer);

		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listbegin");

		for(i=0; i<alen; i++)
		{
			const char *sval = asval[i];
			if(!sval)
				sval = "(null)";
			irc_bot_send_notice_va(bot, from, 3, "reply", "value", sval);
		}
		irc_bot_send_notice_va(bot, from, 3, "reply", "value", "listend");

		free(asval);
		break;

	default:
		irc_bot_send_reply(bot, from, "error reading entry : entry type unknown");
		return;
	}
#undef BUFFER_SIZE
}

void config_writechar_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	const char *value;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name, &value))
		return;

	char val;
	if(sscanf(value, "%c", &val) != 1)
	{
		irc_bot_send_reply(bot, from, "bad value : '%s'", value);
		return;
	}

	if(!config_write_char(section, name, val))
	{
		irc_bot_send_reply(bot, from, "error writing value");
		return;
	}

	irc_bot_send_reply(bot, from, "value written");
}

void config_writeint_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	const char *value;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name, &value))
		return;

	int val;
	if(sscanf(value, "%i", &val) != 1)
	{
		irc_bot_send_reply(bot, from, "bad value : '%s'", value);
		return;
	}

	if(!config_write_int(section, name, val))
	{
		irc_bot_send_reply(bot, from, "error writing value");
		return;
	}

	irc_bot_send_reply(bot, from, "value written");
}

void config_writeint64_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	const char *value;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name, &value))
		return;

	long long val;
	if(sscanf(value, "%lld", &val) != 1)
	{
		irc_bot_send_reply(bot, from, "bad value : '%s'", value);
		return;
	}

	if(!config_write_int64(section, name, val))
	{
		irc_bot_send_reply(bot, from, "error writing value");
		return;
	}

	irc_bot_send_reply(bot, from, "value written");
}

void config_writestring_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	const char *value;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name, &value))
		return;

	if(!config_write_string(section, name, value))
	{
		irc_bot_send_reply(bot, from, "error writing value");
		return;
	}

	irc_bot_send_reply(bot, from, "value written");
}

void config_writebuffer_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	const char *value;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name, &value))
		return;

	// TODO
	irc_bot_send_reply(bot, from, "not implemented!");
}

void config_writechararray_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	const char *value;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name, &value))
		return;

	// TODO
	irc_bot_send_reply(bot, from, "not implemented!");
}

void config_writeintarray_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	const char *value;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name, &value))
		return;

	// TODO
	irc_bot_send_reply(bot, from, "not implemented!");
}

void config_writeint64array_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	const char *value;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name, &value))
		return;

	// TODO
	irc_bot_send_reply(bot, from, "not implemented!");
}

void config_writestringarray_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	const char *name;
	const char *value;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section, &name, &value))
		return;

	// TODO
	irc_bot_send_reply(bot, from, "not implemented!");
}

void config_enumsections_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	struct list_data data;
	data.bot = bot;
	data.target = from;
	data.ctxdata = NULL;
	irc_bot_send_notice_va(bot, from, 3, "reply", "configsections", "listbegin");
	config_enum_sections(config_enum_section_item, &data);
	irc_bot_send_notice_va(bot, from, 3, "reply", "configsections", "listend");
}

int config_enum_section_item(const char *section, void *ctx)
{
	struct list_data *data = ctx;
	irc_bot_send_notice_va(data->bot, data->target, 3, "reply", "configsections", section);
	return 1;
}

void config_enumentries_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section))
		return;

	struct list_data data;
	data.bot = bot;
	data.target = from;
	data.ctxdata = (char*)section;
	irc_bot_send_notice_va(bot, from, 3, "reply", "configentries", "listbegin");
	config_enum_entries(section, config_enum_entry_item, &data);
	irc_bot_send_notice_va(bot, from, 3, "reply", "configentries", "listend");
}

int config_enum_entry_item(const char *name, void *ctx)
{
	struct list_data *data = ctx;

	enum config_type type;
	const char *stype = "unknown";
	if(config_get_entry_type(data->ctxdata, name, &type))
	{
		switch(type)
		{
		case CHAR: stype = "char"; break;
		case INT: stype = "int"; break;
		case INT64: stype = "int64"; break;
		case STRING: stype = "string"; break;
		case BUFFER: stype = "buffer"; break;
		case CHAR_ARRAY: stype = "char_array"; break;
		case INT_ARRAY: stype = "int_array"; break;
		case INT64_ARRAY: stype = "int64_array"; break;
		case STRING_ARRAY: stype = "string_array"; break;
		default: stype = "unknown"; break;
		}
	}

	irc_bot_send_notice_va(data->bot, data->target, 5, "reply", "configentries", name, "type", stype);
	return 1;
}

void config_deletesection_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section))
		return;

	if(!config_delete_section(section))
	{
		irc_bot_send_reply(bot, from, "error deleting section");
		return;
	}

	irc_bot_send_reply(bot, from, "section deleted");
}

void config_deleteentry_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *section_name;
	const char *entry_name;
	if(!irc_bot_read_parameters(bot, from, args, argc, &section_name, &entry_name))
		return;

	if(!config_delete_entry(section_name, entry_name))
	{
		irc_bot_send_reply(bot, from, "error deleting entry");
		return;
	}

	irc_bot_send_reply(bot, from, "entry deleted");
}
