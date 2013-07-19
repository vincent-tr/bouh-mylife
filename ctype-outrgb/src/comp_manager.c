/*
 * comp_manager.c
 *
 *  Created on: 9 juil. 2013
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
#include "gpiodriver_api.h"
#include "component.h"
#include "comp_manager.h"
#include "tools.h"

static void ctypeoutrgb_create_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void ctypeoutrgb_delete_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

static void manager_load_startup_components();
static void manager_add_startup_component(const char *id, int rpin, int gpin, int bpin);
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

#define CONFIG_SECTION "ctype-outrgb"
#define CONFIG_ENTRY "startup_components"
#define CONFIG_ENTRY_SIZE 128

struct list comp_list;

struct comp_node
{
	struct list_node node;
	struct component *comp;
};

static char *cmd_ctypeoutrgb_create_desc[] =
{
	"create a component, args : id, R pin number, G pin number, B pin number",
	NULL
};

static struct irc_command_description cmd_ctypeoutrgb_create =
{
	.verb = "create",
	.description = cmd_ctypeoutrgb_create_desc,
	.children = NULL,
	.callback = ctypeoutrgb_create_handler,
	.ctx = NULL
};

static char *cmd_ctypeoutrgb_delete_desc[] =
{
	"delete a component, args : id",
	NULL
};

static struct irc_command_description cmd_ctypeoutrgb_delete =
{
	.verb = "delete",
	.description = cmd_ctypeoutrgb_delete_desc,
	.children = NULL,
	.callback = ctypeoutrgb_delete_handler,
	.ctx = NULL
};

static char *cmd_ctypeoutrgb_desc[] =
{
	"ctype-outrgb : manage out rgb component : 3 pins output to R, G, B (0 .. 255 each)",
	NULL
};

static struct irc_command_description *cmd_ctypeoutrgb_children[] =
{
	&cmd_ctypeoutrgb_create,
	&cmd_ctypeoutrgb_delete,
	NULL
};

static struct irc_command_description cmd_ctypeoutrgb =
{
	.verb = "ctype-outrgb",
	.description = cmd_ctypeoutrgb_desc,
	.children = cmd_ctypeoutrgb_children,
	.callback = NULL,
	.ctx = NULL
};

static struct irc_handler *cmd_ctypeoutrgb_handler;
static struct irc_bot *corebot;

void comp_manager_init()
{
	list_init(&comp_list);
	manager_load_startup_components();
	corebot = manager_get_bot();
	cmd_ctypeoutrgb_handler = irc_bot_add_message_handler(corebot, 0, &cmd_ctypeoutrgb);
}

void comp_manager_terminate()
{
	irc_bot_remove_handler(corebot, cmd_ctypeoutrgb_handler);
	list_clear(&comp_list, free_comp, NULL);
}

void ctypeoutrgb_create_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *id;
	const char *red_pin;
	const char *green_pin;
	const char *blue_pin;
	int r;
	int g;
	int b;
	struct component *c;

	if(!irc_bot_read_parameters(bot, from, args, argc, &id, &red_pin, &green_pin, &blue_pin))
		return;

	if(sscanf(red_pin, "%d", &r) != 1 || sscanf(green_pin, "%d", &g) != 1 || sscanf(blue_pin, "%d", &b) != 1)
	{
		error_last = ERROR_CORE_INVAL;
		irc_bot_send_reply_from_error(bot, from, "create");
		return;
	}

	if(!(c = component_create(id, r, g, b)))
		irc_bot_send_reply_from_error(bot, from, "create");

	list_add(&comp_list, c);
	manager_add_startup_component(id, r, g, b);
}

void ctypeoutrgb_delete_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
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
		int rpin, gpin, bpin;

		snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.red_pin", id);
		configentry[CONFIG_ENTRY_SIZE-1] = '\0';
		log_assert(config_read_int(ctype, configentry, &rpin));

		snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.green_pin", id);
		configentry[CONFIG_ENTRY_SIZE-1] = '\0';
		log_assert(config_read_int(ctype, configentry, &gpin));

		snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.blue_pin", id);
		configentry[CONFIG_ENTRY_SIZE-1] = '\0';
		log_assert(config_read_int(ctype, configentry, &bpin));

		log_assert(c = component_create(id, rpin, gpin, bpin));
		list_add(&comp_list, c);
	}
}

void manager_add_startup_component(const char *id, int rpin, int gpin, int bpin)
{
	char configentry[CONFIG_ENTRY_SIZE];

	// add startup entry
	log_assert(config_write_string_array_add_item(CONFIG_SECTION, CONFIG_ENTRY, id));

	// write red pin, green pin, blue pin
	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.red_pin", id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	log_assert(config_write_int(ctype, configentry, rpin));

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.green_pin", id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	log_assert(config_write_int(ctype, configentry, gpin));

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.blue_pin", id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	log_assert(config_write_int(ctype, configentry, bpin));
}

void manager_remove_startup_component(const char *id)
{
	char configentry[CONFIG_ENTRY_SIZE];

	// remove red pin, green pin, blue pin
	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.red_pin", id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	config_delete_entry(ctype, configentry);

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.green_pin", id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	config_delete_entry(ctype, configentry);

	snprintf(configentry, CONFIG_ENTRY_SIZE, "%s.blue_pin", id);
	configentry[CONFIG_ENTRY_SIZE-1] = '\0';
	config_delete_entry(ctype, configentry);

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
