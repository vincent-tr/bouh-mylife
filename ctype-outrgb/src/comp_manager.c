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

struct comp_id_lookup_data
{
	const char *id;
	struct component *result;
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

	struct comp_id_lookup_data data;
	data.id = id;
	data.result = NULL;
	list_foreach(&comp_list, comp_id_lookup_item, &data);
	struct component *comp = data.result;

	if(!comp)
	{
		error_last = ERROR_CORE_INVAL;
		irc_bot_send_reply_from_error(bot, from, "delete");
		return;
	}

	component_delete(comp, 1);

#error TODO : retrouver comp dans comp_list et le supprimer

	manager_remove_startup_component(id);
}

void manager_load_startup_components()
{
	size_t count;
	char **array;

	if(!config_read_string_array(CONFIG_SECTION, CONFIG_ENTRY, &count, &array))
		return; // no config => no components

	for(size_t i=0; i<count; i++)
	{
		const char *id = array[i];
#error TODO
	}
}

void manager_add_startup_component(const char *id, int rpin, int gpin, int bpin)
{
	char configentry[CONFIG_ENTRY_SIZE];
	size_t count;
	char **array_old;
	const char **array_new;

	// add startup entry
	if(!config_read_string_array(CONFIG_SECTION, CONFIG_ENTRY, &count, &array_old))
	{
		count = 0;
		array_old = NULL;
	}

	malloc_array_nofail(array_new, count+1);

	if(array_old)
		memcpy(array_new, array_old, count*sizeof(*array_new));
	array_new[count++] = id; // last item

	config_write_string_array(CONFIG_SECTION, CONFIG_ENTRY, count, array_new);

	if(array_old)
		free(array_old);
	free(array_new);

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
	size_t count;
	size_t idx = (size_t)(-1);
	char **array;

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

	// remove startup entry
	if(!config_read_string_array(CONFIG_SECTION, CONFIG_ENTRY, &count, &array))
		return; // no config => nothing to remove

	for(size_t i=0; i<count; i++)
	{
		// find index
		if(!strcasecmp(array[i], id))
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

void free_comp(void *node, void *ctx)
{
	struct comp_node *cn = node;
	component_delete(cn->comp, 0);
	free(cn);
}

int comp_id_lookup_item(void *node, void *ctx)
{
	struct comp_id_lookup_data *data = ctx;
	struct component *comp = node;
	if(!strcasecmp(data->id, component_get_id(comp)))
	{
		data->result = comp;
		return 0;
	}

	return 1;
}
