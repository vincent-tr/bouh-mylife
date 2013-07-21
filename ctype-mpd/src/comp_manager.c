/*
 * comp_manager.c
 *
 *  Created on: 21 juil. 2013
 *      Author: pumbawoman
 */

#define _BSD_SOURCE
#include <stddef.h>
#include <sys/select.h>
#include <stdarg.h>
#include <stdlib.h>
#include <limits.h>
#include <stdio.h>
#include <string.h>
#include <strings.h>

#include "core_api.h"
#include "component.h"
#include "comp_manager.h"
#include "tools.h"

static void ctypempd_create_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void ctypempd_delete_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

static void manager_load_startup_components();
static void manager_add_startup_component(const char *id, const char *server_address);
static void manager_remove_startup_component(const char *id);

static void free_comp(void *node, void *ctx);
static int comp_id_lookup_item(void *node, void *ctx);
static int comp_node_lookup_item(void *node, void *ctx);

struct comp_id_lookup_data
{
	const char *id;
	struct component *result;
};

struct comp_node_lookup_data
{
	struct component *comp;
	struct comp_node *result;
};

#define CONFIG_ENTRY "startup_components"
#define CONFIG_ENTRY_SIZE 128

struct list comp_list;

struct comp_node
{
	struct list_node node;
	struct component *comp;
};

static char *cmd_ctypempd_create_desc[] =
{
	"create a component, args : id, server address",
	NULL
};

static struct irc_command_description cmd_ctypempd_create =
{
	.verb = "create",
	.description = cmd_ctypempd_create_desc,
	.children = NULL,
	.callback = ctypempd_create_handler,
	.ctx = NULL
};

static char *cmd_ctypempd_delete_desc[] =
{
	"delete a component, args : id",
	NULL
};

static struct irc_command_description cmd_ctypempd_delete =
{
	.verb = "delete",
	.description = cmd_ctypempd_delete_desc,
	.children = NULL,
	.callback = ctypempd_delete_handler,
	.ctx = NULL
};

static char *cmd_ctypempd_desc[] =
{
	"ctype-mpd : manage mpd component",
	NULL
};

static struct irc_command_description *cmd_ctypempd_children[] =
{
	&cmd_ctypempd_create,
	&cmd_ctypempd_delete,
	NULL
};

static struct irc_command_description cmd_ctypempd =
{
	.verb = "ctype-mpd",
	.description = cmd_ctypempd_desc,
	.children = cmd_ctypempd_children,
	.callback = NULL,
	.ctx = NULL
};

static struct irc_handler *cmd_ctypempd_handler;
static struct irc_bot *corebot;

void comp_manager_init()
{
	list_init(&comp_list);
	manager_load_startup_components();
	corebot = manager_get_bot();
	cmd_ctypempd_handler = irc_bot_add_message_handler(corebot, 0, &cmd_ctypempd);
}

void comp_manager_terminate()
{
	irc_bot_remove_handler(corebot, cmd_ctypempd_handler);
	list_clear(&comp_list, free_comp, NULL);
}

void ctypempd_create_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *id;
	const char *server_address;
	struct component *c;

	if(!irc_bot_read_parameters(bot, from, args, argc, &id, &server_address))
		return;

	if(!(c = component_create(id, server_address)))
	{
		irc_bot_send_reply_from_error(bot, from, "create");
		return;
	}

	struct comp_node *cn;
	malloc_nofail(cn);
	cn->comp = c;
	list_add(&comp_list, cn);

	manager_add_startup_component(id, server_address);
	irc_bot_send_reply_from_error(bot, from, "create");
}

void ctypempd_delete_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *id;

	if(!irc_bot_read_parameters(bot, from, args, argc, &id))
		return;

	struct component *comp = NULL;
	{
		struct comp_id_lookup_data data;
		data.id = id;
		data.result = NULL;
		list_foreach(&comp_list, comp_id_lookup_item, &data);
		comp = data.result;
	}

	if(!comp)
	{
		error_last = ERROR_CORE_INVAL;
		irc_bot_send_reply_from_error(bot, from, "delete");
		return;
	}

	component_delete(comp, 1);

	struct comp_node *node;
	{
		struct comp_node_lookup_data data;
		data.comp = comp;
		data.result = NULL;
		list_foreach(&comp_list, comp_node_lookup_item, &data);
		node = data.result;
	}

	list_remove(&comp_list, node);
	manager_remove_startup_component(id);
	irc_bot_send_reply_from_error(bot, from, "delete");
}

void manager_load_startup_components()
{
	size_t count;
	char **array;
	char configentry[CONFIG_ENTRY_SIZE];
	struct component *c;

	if(!config_read_string_array(CONFIG_SECTION, CONFIG_ENTRY, &count, &array))
		return; // no config => no components

	for(size_t i=0; i<count; i++)
	{
		const char *id = array[i];
		char *server_address;

		snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.server_address", id);
		configentry[CONFIG_ENTRY_SIZE-1] = '\0';
		log_assert(config_read_string(CONFIG_SECTION, configentry, &server_address));

		log_assert(c = component_create(id, server_address));

		free(server_address);

		struct comp_node *cn;
		malloc_nofail(cn);
		cn->comp = c;
		list_add(&comp_list, cn);
	}
}

void manager_add_startup_component(const char *id, const char *server_address)
{
	char configentry[CONFIG_ENTRY_SIZE];

	// add startup entry
	log_assert(config_write_string_array_add_item(CONFIG_SECTION, CONFIG_ENTRY, id));

	// write server_address
	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.server_address", id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	log_assert(config_write_string(CONFIG_SECTION, configentry, server_address));
}

void manager_remove_startup_component(const char *id)
{
	char configentry[CONFIG_ENTRY_SIZE];

	// remove server_address
	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.server_address", id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	config_delete_entry(CONFIG_SECTION, configentry);

	log_assert(config_write_string_array_remove_item(CONFIG_SECTION, CONFIG_ENTRY, id));
}

void free_comp(void *node, void *ctx)
{
	struct comp_node *cn = node;
	component_delete(cn->comp, 0);
	free(cn);
}

int comp_id_lookup_item(void *node, void *ctx)
{
	struct comp_id_lookup_data *data = ctx;
	struct comp_node *cn = node;
	if(!strcasecmp(data->id, component_get_id(cn->comp)))
	{
		data->result = cn->comp;
		return 0;
	}

	return 1;
}

int comp_node_lookup_item(void *node, void *ctx)
{
	struct comp_node_lookup_data *data = ctx;
	struct comp_node *cn = node;
	if(cn->comp == data->comp)
	{
		data->result = cn;
		return 0;
	}

	return 1;
}
