/*
 * modules.h
 *
 *  Created on: 16 juin 2013
 *      Author: pumbawoman
 */

#ifndef MODULES_H_
#define MODULES_H_

extern void modules_init();
extern void modules_terminate();

struct module;

struct module_name
{
	const char *name;
	unsigned int major;
	unsigned int minor;
};

extern void module_enum_files(int (*callback)(char *)); // ret 0 = stop enum
extern int module_create(char *file, void *content, size_t content_len);
extern int module_delete(char *file);

extern void module_enum_loaded(int (*callback)(struct module *)); // ret 0 = stop enum

extern struct module *module_load(char *file);
extern struct module *module_unload(char *name);

extern const char *module_get_file(struct module *module);
extern const struct module_name *module_get_name(struct module *module);

struct module_def
{
	struct module_name name;
	const char **required;
	void *api;
};

// required = NULL terminated array
#define MODULE_DEFINE (name, version_major, version_minor, required) \
	struct module_def module_def = { .name = name, .major = major, .minor = minor, .required = required }

struct core_api
{

};

#endif /* MODULES_H_ */
