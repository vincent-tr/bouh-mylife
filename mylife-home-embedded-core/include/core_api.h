/*
 * core_api.h
 *
 *  Created on: 17 juin 2013
 *      Author: pumbawoman
 */

#ifndef CORE_API_H_
#define CORE_API_H_

#include "list.h"
#include "logger.h"
#include "module.h"
#include "loop.h"
#include "irc.h"
#include "manager.h"

struct core_api
{
	void (*list_init)(struct list *list);
	void (*list_add)(struct list *list, void *node);
	void (*list_remove)(struct list *list, void *node);
	void (*list_foreach)(struct list *list, int (*callback)(void *node, void *ctx), void *ctx); // return 0 = break foreach
	void (*list_clear)(struct list *list, void (*free_node)(void *node, void *ctx), void *ctx);
	int (*list_is_empty)(struct list *list);
	int (*list_count)(struct list *list);

	void (*log_write)(const char *file, int line, int level, const char *format, ...);

	void (*module_enum_files)(int (*callback)(const char *file, void *ctx), void *ctx); // ret 0 = stop enum
	int (*module_create)(const char *file, const void *content, size_t content_len);
	int (*module_delete)(const char *file);
	void (*module_enum_loaded)(int (*callback)(struct module *module, void *ctx), void *ctx); // ret 0 = stop enum, ne pas charger ou décharger le module et continuer l'enum !!!
	struct module *(*module_find_by_file)(const char *file);
	struct module *(*module_find_by_name)(const char *name);
	struct module *(*module_load)(const char *file);
	int (*module_unload)(struct module *module);
	const char *(*module_get_file)(struct module *module);
	const struct module_name *(*module_get_name)(struct module *module);
	void (*module_enum_ref)(struct module *module, int (*callback)(struct module *ref, void *ctx), void *ctx); // ret 0 = stop enum
	void (*module_enum_refby)(struct module *module, int (*callback)(struct module *ref, void *ctx), void *ctx); // ret 0 = stop enum

	struct loop_handle *(*loop_register_tick)(void (*callback)(void *ctx), void *ctx);
	struct loop_handle *(*loop_register_timer)(void (*callback)(void *ctx), void *ctx, int period_ms);
	struct loop_handle *(*loop_register_listener)(callback_select_add callback_add, callback_select_process callback_process, void *ctx);
	void (*loop_unregister)(struct loop_handle *handle);
	void *(*loop_get_ctx)(struct loop_handle *handle);
	void (*loop_set_ctx)(struct loop_handle *handle, void *ctx);

	struct irc_bot *(*manager_get_bot)();

	struct irc_bot *(*irc_bot_create)(const char *id, const char *type, struct irc_bot_callbacks *callbacks, void *ctx);
	void (*irc_bot_delete)(struct irc_bot *bot);
	void *(*irc_bot_get_ctx)(struct irc_bot *bot);
	void (*irc_bot_set_ctx)(struct irc_bot *bot, void *ctx);
	int (*irc_bot_is_connected)(struct irc_bot *bot);
	void (*irc_bot_set_comp_status)(struct irc_bot *bot, const char *status);
	struct irc_component *(*irc_get_me)(struct irc_bot *bot);
	void (*irc_comp_list)(struct irc_bot *bot, int (*callback)(struct irc_component *comp, void *ctx), void *ctx);
	int (*irc_comp_is_me)(struct irc_bot *bot, struct irc_component *comp);
	const char *(*irc_comp_get_nick)(struct irc_bot *bot, struct irc_component *comp);
	const char *(*irc_comp_get_host)(struct irc_bot *bot, struct irc_component *comp); // NULL if unrecognized nick format
	const char *(*irc_comp_get_id)(struct irc_bot *bot, struct irc_component *comp); // NULL if unrecognized nick format
	const char *(*irc_comp_get_type)(struct irc_bot *bot, struct irc_component *comp); // NULL if unrecognized nick format
	const char *(*irc_comp_get_status)(struct irc_bot *bot, struct irc_component *comp); // NULL if unrecognized nick format or if no status
	struct irc_handler *(*irc_bot_add_message_handler)(struct irc_bot *bot, int support_broadcast, struct irc_command_description *description);
	struct irc_handler *(*irc_bot_add_notice_handler)(struct irc_bot *bot, int support_broadcast, struct irc_command_description *description);
	void (*irc_bot_remove_handler)(struct irc_bot *bot, struct irc_handler *handler);
	int (*irc_bot_send_message)(struct irc_bot *bot, struct irc_component *comp, const char **args, int argc); // comp NULL = broadcast -- thread unsafe
	int (*irc_bot_send_notice)(struct irc_bot *bot, struct irc_component *comp, const char **args, int argc); // comp NULL = broadcast -- thread unsafe
	int (*irc_bot_send_message_va)(struct irc_bot *bot, struct irc_component *comp, int argc, ...); // comp NULL = broadcast -- thread unsafe
	int (*irc_bot_send_notice_va)(struct irc_bot *bot, struct irc_component *comp, int argc, ...); // comp NULL = broadcast -- thread unsafe

	// TODO
};

#ifndef CORE

// les modules qui utilisent core_api doivent déclarer une variable nommée core_api où ils assignent l'api dans init
extern struct core_api *core_api;

#endif

#endif /* CORE_API_H_ */
