/*
 * module.h
 *
 *  Created on: 16 juin 2013
 *      Author: pumbawoman
 */

#ifndef MODULE_H_
#define MODULE_H_

struct module;

struct module_name
{
	const char *name;
	unsigned int major;
	unsigned int minor;
};

struct module_def
{
	struct module_name name;
	const char **required;
	void *api; // can be NULL if no api provided
	void (*init)(void **apis);
	void (*terminate)();
};

// required = NULL terminated array
#define MODULE_DEFINE (name, version_major, version_minor, required, api, init, terminate) \
	struct module_def module_def = { .name = name, .major = major, .minor = minor, .required = required, .api = api, .init = init, .terminate = terminate }

#ifdef CORE

extern void module_init();
extern void module_terminate();

extern void module_enum_files(int (*callback)(const char *file, void *ctx), void *ctx); // ret 0 = stop enum
extern int module_create(const char *file, const void *content, size_t content_len);
extern int module_delete(const char *file);

extern void module_enum_loaded(int (*callback)(struct module *module, void *ctx), void *ctx); // ret 0 = stop enum, ne pas charger ou décharger le module et continuer l'enum !!!
extern struct module *module_find_by_file(const char *file);
extern struct module *module_find_by_name(const char *name);

extern struct module *module_load(const char *file);
extern int module_unload(struct module *module);

extern const char *module_get_file(struct module *module);
extern const struct module_name *module_get_name(struct module *module);
extern void module_enum_ref(struct module *module, int (*callback)(struct module *ref, void *ctx), void *ctx); // ret 0 = stop enum
extern void module_enum_refby(struct module *module, int (*callback)(struct module *ref, void *ctx), void *ctx); // ret 0 = stop enum

#else // CORE

#include "core_api.h"

#define module_enum_files(callback, ctx) (core_api->module_enum_files(callback, ctx))
#define module_enum_files(callback, ctx) (core_api->module_enum_files(callback, ctx)) // ret 0 = stop enum
#define module_create(file, content, content_len) (core_api->module_create(file, content, content_len))
#define module_delete(file) (core_api->module_delete(file))

#define module_enum_loaded(callback, ctx) (core_api->module_enum_loaded(callback, ctx)) // ret 0 = stop enum, ne pas charger ou décharger le module et continuer l'enum !!!
#define module_find_by_file(file) (core_api->module_find_by_file(file))
#define module_find_by_name(name) (core_api->module_find_by_name(name))

#define module_load(file) (core_api->module_load(file))
#define module_unload(module) (core_api->module_unload(module))

#define module_get_file(module) (core_api->module_get_file(module))
#define module_get_name(module) (core_api->module_get_name(module))
#define module_enum_ref(module, callback, ctx) (core_api->module_enum_ref(module, callback, ctx)) // ret 0 = stop enum
#define module_enum_refby(module, callback, ctx) (core_api->module_enum_refby(module, callback, ctx)) // ret 0 = stop enum


#endif // CORE

#endif /* MODULE_H_ */
