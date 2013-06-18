/*
 * core_api.h
 *
 *  Created on: 17 juin 2013
 *      Author: pumbawoman
 */

#ifndef CORE_API_H_
#define CORE_API_H_

struct core_api
{
	void (*list_init)(struct list *list);
	void (*list_add)(struct list *list, void *node);
	void (*list_remove)(struct list *list, void *node);
	void (*list_foreach)(struct list *list, int (*callback)(void *node, void *ctx), void *ctx); // return 0 = break foreach
	void (*list_clear)(struct list *list, void (*free_node)(void *node, void *ctx), void *ctx);
	int (*list_is_empty)(struct list *list);

	void (*log_write)(const char *file, int line, int level, const char *format, ...);

	void (*module_enum_files)(int (*callback)(const char *file, void *ctx), void *ctx); // ret 0 = stop enum
	int (*module_create)(char *file, void *content, size_t content_len);
	int (*module_delete)(char *file);
	void (*module_enum_loaded)(int (*callback)(struct module *module, void *ctx), void *ctx); // ret 0 = stop enum, ne pas charger ou décharger le module et continuer l'enum !!!
	struct module *(*module_find_by_file)(const char *file);
	struct module *(*module_find_by_name)(const char *name);
	struct module *(*module_load)(const char *file);
	int (*module_unload)(struct module *module);
	const char *(*module_get_file)(struct module *module);
	const struct module_name *(*module_get_name)(struct module *module);

	// TODO
};

#ifndef CORE

// les modules qui utilisent core_api doivent déclarer une variable nommée core_api où ils assignent l'api dans init
extern struct core_api *core_api;

#endif

#endif /* CORE_API_H_ */
