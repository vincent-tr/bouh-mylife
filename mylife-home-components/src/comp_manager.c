/*
 * components.c
 *
 *  Created on: Jan 16, 2014
 *      Author: vincent
 */

#include <stdlib.h>

#include "components.h"
#include "logger.h"
#include "tools.h"
#include "config.h"

struct component
{
	struct list_node node;
	char *id;
	struct component_type *type;
	void *handle;
};

struct lookup_type_data
{
	const char *name;
	struct component_type *res;
};

static struct list types;
static struct list components;

static void sub_init();
static void sub_terminate();
static struct component *create_component(const char *type, const char *id, config_setting_t *config);
static void free_component(void *node, void *ctx);
static void free_type(void *node, void *ctx);
static int lookup_type_item(void *node, void *ctx);
static int lookup_component_id_item(void *node, void *ctx);

void sub_init()
{
	comp_internal_test_init();
	comp_internal_mpd_init();
}

void sub_terminate()
{
	comp_internal_mpd_terminate();
	comp_internal_test_terminate();
}

void components_init()
{
	list_init(&types);
	list_init(&components);

	sub_init();

	config_setting_t *list;
	log_assert((list = config_lookup(conf_get(), "components")));
	conf_assert(config_setting_is_list(list));
	size_t len = config_setting_length(list);
	for(size_t i=0; i<len; i++)
	{
		config_setting_t *compconf;
		const char *id;
		const char *type;
		config_setting_t *config;
		struct component *comp;

		log_assert((compconf = config_setting_get_elem(list, i)));

		log_assert((id = conf_get_string(compconf, "id")));
		log_assert((type = conf_get_string(compconf, "type")));
		log_assert((config = config_setting_get_member(compconf, "config")));

		log_assert((comp = create_component(type, id, config)));
		list_add(&components, comp);
	}
}

void components_terminate()
{
	list_clear(&components, free_component, NULL);

	sub_terminate();

	list_clear(&types, free_type, NULL);
}

void component_register(struct component_type *type)
{
	list_add(&types, type);
}

struct component *create_component(const char *type, const char *id, config_setting_t *config)
{
	struct component *comp;

	malloc_nofail(comp);

	struct lookup_type_data data;
	data.name = type;
	data.res = NULL;
	list_foreach(&types, lookup_type_item, &data);
	log_assert(data.res);
	comp->type = data.res;

	strdup_nofail(comp->id, id);
	list_foreach(&components, lookup_component_id_item, (void *)id);

	comp->handle = data.res->creator(id, config);

	return comp;
}

void free_component(void *node, void *ctx)
{
	struct component *component = node;
	component->type->destructor(component->handle);
	free(component);
}

void free_type(void *node, void *ctx)
{
	// nothing
}

int lookup_type_item(void *node, void *ctx)
{
	struct lookup_type_data *data = ctx;
	struct component_type *type = node;

	if(!strcmp(type->name, data->name))
	{
		data->res = type;
		return 0;
	}

	return 1;
}

int lookup_component_id_item(void *node, void *ctx)
{
	const char *id = ctx;
	struct component *comp = node;

	log_assert(strcmp(id, comp->id));

	return 1;
}
