/*
 * modules.c
 *
 *  Created on: 16 juin 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include <dlfcn.h>
#include "module.h"
#include "list.h"
#include "core_api.h"

struct module
{
	struct node_list node;

	const char *file;
	void *lib;
	struct module_def *def;
	int (*init)(void **apis);
	void (*terminate)();
};

struct list modules;

// définit le module correspondant au core
static struct core_api api =
{
	.list_init = list_init,
	.list_add = list_add,
	.list_remove = list_remove,
	.list_foreach = list_foreach, // return 0 = break foreach
	.list_clear = list_clear
};

static struct module_def me_def =
{
	.name = "core",
	.major = 1,
	.minor = 0,
	.required = { NULL },
	.api = api
};

static struct module me =
{
	.file = NULL,
	.lib = NULL,
	.def = me_def,
	.init = NULL,
	.terminate = NULL
};

void module_init()
{
	list_init(&modules);
	list_add(&modules, &me);
}

void module_terminate()
{
}

void module_enum_files(int (*callback)(char *)) // ret 0 = stop enum
{

}

int module_create(char *file, void *content, size_t content_len)
{

}

int module_delete(char *file)
{

}

void module_enum_loaded(int (*callback)(struct module *)) // ret 0 = stop enum
{
	list_foreach(&modules, callback);
}

struct module *module_load(char *file)
{

}

struct module *module_unload(char *name)
{

}

const char *module_get_file(struct module *module)
{
	return module->file;
}

const struct module_name *module_get_name(struct module *module)
{
	return &(module->def->name);
}
