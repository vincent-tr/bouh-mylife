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

extern void module_enum_files(int (*callback)(char *)); // ret 0 = stop enum
extern int module_create(char *file, void *content, size_t content_len);
extern int module_delete(char *file);

extern void module_enum_loaded(int (*callback)(struct module *)); // ret 0 = stop enum

extern char *module_get_file(struct module *module);
extern char *module_get_name(struct module *module);
extern char *module_get_version(struct module *module);
extern char *module_get_author(struct module *module);

extern struct module *module_load(char *file);
extern struct module *module_unload(char *name);


#endif /* MODULES_H_ */
