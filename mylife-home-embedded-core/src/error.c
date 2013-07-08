/*
 * error.c
 *
 *  Created on: 8 juil. 2013
 *      Author: pumbawoman
 */

#define _BSD_SOURCE
#include <stddef.h>
#include <malloc.h>
#include <string.h>

#include "error.h"
#include "list.h"
#include "logger.h"
#include "tools.h"

struct value
{
	struct list_node node;

	unsigned short errid;
	char *description;
};

struct factory
{
	struct list_node node;
	unsigned short factoryid;
	char *name;

	struct list values;
};

struct factory_lookup_data
{
	int factoryid;
	struct factory *result;
};

struct value_lookup_data
{
	int errid;
	struct value *result;
};

static struct list factories;

static struct factory *factory_lookup(int factoryid);
static int factory_lookup_item(void *node, void *ctx);
static struct value *value_lookup(struct factory *factory, int errid);
static int value_lookup_item(void *node, void *ctx);
static void free_value(void *node, void *ctx);

static __thread int error_value;

void error_init()
{
	list_init(&factories);

	error_register_factory(ERROR_FACTORY_CORE, "core");
	error_register_value(ERROR_CORE_INVAL, "Invalid argument");
	error_register_value(ERROR_CORE_NOTFOUND, "The requested object is not found");
	error_register_value(ERROR_CORE_EXISTS, "The requested object already exists");
	error_register_value(ERROR_CORE_IOERROR, "An I/O error occured");
	error_register_value(ERROR_CORE_BADMOD, "Bad module format");
	error_register_value(ERROR_CORE_UNRESOLVEDDEP, "Unresolved module dependency");
	error_register_value(ERROR_CORE_EXISTINGDEP, "The module is style referenced");
	error_register_value(ERROR_CORE_DISCONNECTED, "The link is disconnected");
}

void error_terminate()
{
	error_unregister_factory(ERROR_FACTORY_CORE);

	log_assert(list_is_empty(&factories));
}

void error_register_factory(unsigned short factoryid, const char *name)
{
	log_assert(!factory_lookup(factoryid));

	struct factory *factory;
	malloc_nofail(factory);
	factory->factoryid = factoryid;
	strdup_nofail(factory->name, name);
	list_init(&(factory->values));

	list_add(&factories, factory);
}

void error_register_value(unsigned int err, const char *description)
{
	struct factory *factory;
	log_assert(factory = factory_lookup(error_factoryid(err)));
	log_assert(!value_lookup(factory, error_errid(err)));

	struct value *value;
	malloc_nofail(value);
	value->errid =  error_errid(err);
	strdup_nofail(value->description, description);
	list_add(&(factory->values), value);
}

void error_unregister_factory(unsigned short factoryid) // unregister all descriptions
{
	struct factory *factory;
	log_assert(factory = factory_lookup(factoryid));

	list_remove(&factories, factory);
	free(factory->name);
	list_clear(&(factory->values), free_value, NULL);
	free(factory);
}

const char *error_description(unsigned int err)
{
	struct factory *factory = factory_lookup(error_factoryid(err));
	if(!factory)
		return NULL;
	struct value *value = value_lookup(factory, error_errid(err));
	if(!value)
		return NULL;
	return value->description;
}

const char *error_factory_name(unsigned int err)
{
	struct factory *factory = factory_lookup(error_factoryid(err));
	if(!factory)
		return NULL;
	return factory->name;
}

int *error_internal_get_ptr()
{
	return &error_value;
}

int error_internal_failed(int err)
{
	error_last = err;
	return 0;
}

void *error_internal_failed_ptr(int err)
{
	return NULL;
}

int error_internal_success()
{
	error_last = ERROR_SUCCESS;
	return 1;
}

struct factory *factory_lookup(int factoryid)
{
	struct factory_lookup_data data;
	data.factoryid = factoryid;
	data.result = NULL;
	list_foreach(&factories, factory_lookup_item, &data);
	return data.result;
}

int factory_lookup_item(void *node, void *ctx)
{
	struct factory *factory = node;
	struct factory_lookup_data *data = ctx;
	if(factory->factoryid == data->factoryid)
	{
		data->result = factory;
		return 0;
	}
	return 1;
}

struct value *value_lookup(struct factory *factory, int errid)
{
	struct value_lookup_data data;
	data.errid = errid;
	data.result = NULL;
	list_foreach(&(factory->values), value_lookup_item, &data);
	return data.result;
}

int value_lookup_item(void *node, void *ctx)
{
	struct value *value = node;
	struct value_lookup_data *data = ctx;
	if(value->errid == data->errid)
	{
		data->result = value;
		return 0;
	}
	return 1;
}

void free_value(void *node, void *ctx)
{
	struct value *value = node;
	free(value->description);
	free(value);
}
