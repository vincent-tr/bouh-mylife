/*
 * modules.c
 *
 *  Created on: 16 juin 2013
 *      Author: pumbawoman
 */

#define _BSD_SOURCE
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dlfcn.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dirent.h>
#include <unistd.h>

#include "module.h"
#include "list.h"
#include "core_api.h"
#include "config_base.h"
#include "logger.h"
#include "tools.h"

struct module_ref
{
	struct list_node node;

	struct module *ref;
};

struct module
{
	struct list_node node;

	// module_ref
	struct list references;
	struct list referenced_by;

	char *file;
	void *lib;
	struct module_def *def;
};

static struct list modules;

// d�finit le module correspondant au core
static struct core_api api =
{
	.list_init = list_init,
	.list_add = list_add,
	.list_remove = list_remove,
	.list_foreach = list_foreach, // return 0 = break foreach
	.list_clear = list_clear,
	.list_is_empty = list_is_empty,

	.log_write = log_write,

	.module_enum_files = module_enum_files, // ret 0 = stop enum
	.module_create = module_create,
	.module_delete = module_delete,
	.module_enum_loaded = module_enum_loaded, // ret 0 = stop enum
	.module_find_by_file = module_find_by_file,
	.module_find_by_name = module_find_by_name,
	.module_load = module_load,
	.module_unload = module_unload,
	.module_get_file = module_get_file,
	.module_get_name = module_get_name,

	.loop_register_tick = loop_register_tick,
	.loop_register_timer = loop_register_timer,
	.loop_register_listener = loop_register_listener,
	.loop_unregister = loop_unregister,
	.loop_get_ctx = loop_get_ctx,
	.loop_set_ctx = loop_set_ctx

	// TODO
};

static const char *required[] =
{
	NULL
};

static struct module_def me_def =
{
	.name.name = "core",
	.name.major = 1,
	.name.minor = 0,
	.required = required,
	.api = &api,
	.init = NULL,
	.terminate = NULL
};

static struct module me =
{
	.file = NULL,
	.lib = NULL,
	.def = (struct module_def *)&me_def
};

struct lookup_data
{
	const void *criteria;
	struct module *result;
};

static int module_find_by_file_callback(struct module *module, void *ctx);
static int module_find_by_name_callback(struct module *module, void *ctx);

void module_init()
{
	list_init(&modules);
	list_add(&modules, &me);
}

void module_terminate()
{
	// TODO
}

void module_enum_files(int (*callback)(const char *file, void *ctx), void *ctx) // ret 0 = stop enum
{
	DIR *dir;
	struct dirent *item;
	log_assert(dir = opendir(CONFIG_MODULES_DIRECTORY));
	char name[256];

	while((item = readdir(dir)))
	{
		// on ne s�lectionne que les fichiers
		if(item->d_type != DT_REG)
			continue;

		strcpy(name, item->d_name);

		// avec extension .so
		char *ext = strrchr(name, '.');
		if (!ext || strcmp(ext, ".so"))
			continue;

		// on consid�re comme un module, on ne note que le nom avant le .
		*ext = '\0';
		if(!callback(name, ctx))
			break;
	}

	closedir(dir);
}

int module_create(char *file, void *content, size_t content_len)
{
	char path[PATH_MAX];

	if(!file || !content || !content_len)
		return 0;

	snprintf(path, PATH_MAX, "%s/%s.so", CONFIG_MODULES_DIRECTORY, file);
	path[PATH_MAX-1] = '\0';

	// check si d�j� existant
	int fd = open(path, O_WRONLY | O_CREAT | O_EXCL);
	if(fd == -1)
		return 0;

	if(write(fd, content, content_len) == -1)
	{
		close(fd);
		return 0;
	}

	close(fd);
	return 1;
}

int module_delete(char *file)
{
	char path[PATH_MAX];

	if(!file)
		return 0;

	snprintf(path, PATH_MAX, "%s/%s.so", CONFIG_MODULES_DIRECTORY, file);
	path[PATH_MAX-1] = '\0';

	if(unlink(path) == -1)
		return 0;

	return 1;
}

void module_enum_loaded(int (*callback)(struct module *module, void *ctx), void *ctx) // ret 0 = stop enum
{
	list_foreach(&modules, (int (*)(void *, void *))callback, ctx);
}

struct module *module_find_by_file(const char *file)
{
	struct lookup_data data;
	data.criteria = file;
	data.result = NULL;
	module_enum_loaded(module_find_by_file_callback, &data);
	return data.result;
}

int module_find_by_file_callback(struct module *module, void *ctx)
{
	struct lookup_data *data = ctx;

	if(!strcmp(module->file, data->criteria))
	{
		data->result = module;
		return 0;
	}

	return 1;
}

struct module *module_find_by_name(const char *name)
{
	struct lookup_data data;
	data.criteria = name;
	data.result = NULL;
	module_enum_loaded(module_find_by_name_callback, &data);
	return data.result;
}

int module_find_by_name_callback(struct module *module, void *ctx)
{
	struct lookup_data *data = ctx;

	if(!strcmp(module->def->name.name, data->criteria))
	{
		data->result = module;
		return 0;
	}

	return 1;
}

struct module *module_load(const char *file)
{
	char path[PATH_MAX];

	if(!file)
		return 0;

	// si un module avec le fichier existe d�j�
	if(module_find_by_file(file))
		return 0;

	// chargement du .so
	snprintf(path, PATH_MAX, "%s/%s.so", CONFIG_MODULES_DIRECTORY, file);
	path[PATH_MAX-1] = '\0';

	void *lib = dlopen(path, RTLD_NOW | RTLD_LOCAL);
	if(!lib)
		return 0;

	// obtention des donn�es du module
	struct module_def *def = dlsym(lib, "module_def");
	if(!def)
	{
		dlclose(lib);
		return 0;
	}

	// si un module avec le nom existe d�j�
	if(module_find_by_name(def->name.name))
	{
		dlclose(lib);
		return 0;
	}

	// on regarde si toutes les d�pendances sont charg�es
	const char **req;
	size_t refcount = 0;
	for(req = def->required; *req; ++req)
	{
		++refcount;
		if(!module_find_by_name(*req))
		{
			dlclose(lib);
			return 0;
		}
	}

	// tout est bon, chargement
	struct module *module;
	malloc_nofail(module);
	strdup_nofail(module->file, file);
	module->lib = lib;
	module->def = def;

	list_init(&(module->references));
	list_init(&(module->referenced_by));

	// ajout � la liste de modules
	list_add(&modules, module);

	// r�f�rences
	void **apis;
	malloc_array_nofail(apis, refcount);
	refcount = 0;
	for(req = def->required; *req; ++req)
	{
		struct module *refmod;
		// refmod doit �tre renseign� on l'a test� juste au dessus
		log_assert(refmod = module_find_by_name(*req));
		log_assert(refmod != module);

		apis[refcount++] = refmod->def->api;

		struct module_ref *ref;
		malloc_nofail(ref);
		ref->ref = refmod;
		list_add(&(module->references), ref);

		struct module_ref *refby;
		malloc_nofail(refby);
		refby->ref = module;
		list_add(&(refmod->references), refby);
	}

	// ex�cution de init
	void (*init)(void **apis) = module->def->init;
	if(init)
		init(apis);

	return module;
}

int module_unload(struct module *module)
{
	if(!module)
		return 0;

	// si le module est encore r�f�renc� on ne peut pas le d�charger
	if(!list_is_empty(&(module->referenced_by)))
		return 0;

	// ex�cution de terminate
	void (*terminate)() = module->def->terminate;
	if(terminate)
		terminate();

	// suppression des donn�es du module
	list_remove(&modules, module);
	free(module->file);
	dlclose(module->lib);
	free(module);

	return 1;
}

const char *module_get_file(struct module *module)
{
	return module->file;
}

const struct module_name *module_get_name(struct module *module)
{
	return &(module->def->name);
}
