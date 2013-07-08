/*
 * irc.h
 *
 *  Created on: 16 juin 2013
 *      Author: pumbawoman
 */

#ifndef IRC_H_
#define IRC_H_

struct irc_bot;
struct irc_component;
struct irc_handler;

typedef void (*irc_callback)(struct irc_bot *bot);
typedef void (*irc_comp_callback)(struct irc_bot *bot, struct irc_component *comp);
typedef void (*irc_handler_callback)(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

struct irc_bot_callbacks
{
	irc_callback on_connected;
	irc_callback on_disconnected;

	irc_comp_callback on_comp_new;
	irc_comp_callback on_comp_delete;
	irc_comp_callback on_comp_change_status;
};

struct irc_command_description
{
	const char *verb;
	char **description; // NULL terminated

	struct irc_command_description **children; // NULL terminated

	void *ctx;
	irc_handler_callback callback;
};

#ifdef CORE

extern void irc_init();
extern void irc_terminate();

extern struct irc_bot *irc_bot_create(const char *id, const char *type, struct irc_bot_callbacks *callbacks, void *ctx);
extern void irc_bot_delete(struct irc_bot *bot);
extern void *irc_bot_get_ctx(struct irc_bot *bot);
extern void irc_bot_set_ctx(struct irc_bot *bot, void *ctx);
extern int irc_bot_is_connected(struct irc_bot *bot);
extern void irc_bot_set_comp_status(struct irc_bot *bot, const char *status);

extern struct irc_component *irc_get_me(struct irc_bot *bot);
extern void irc_comp_list(struct irc_bot *bot, int (*callback)(struct irc_component *comp, void *ctx), void *ctx);
extern int irc_comp_is_me(struct irc_bot *bot, struct irc_component *comp);
extern const char *irc_comp_get_nick(struct irc_bot *bot, struct irc_component *comp);
extern const char *irc_comp_get_host(struct irc_bot *bot, struct irc_component *comp); // NULL if unrecognized nick format
extern const char *irc_comp_get_id(struct irc_bot *bot, struct irc_component *comp); // NULL if unrecognized nick format
extern const char *irc_comp_get_type(struct irc_bot *bot, struct irc_component *comp); // NULL if unrecognized nick format
extern const char *irc_comp_get_status(struct irc_bot *bot, struct irc_component *comp); // NULL if unrecognized nick format or if no status

/*
 * command format : !target verb arg1 arg2 :arg3
 * if verb == NULL => register all (get all messages) and children and description are ignored)
 * if broadcast register target = *
 */
extern struct irc_handler *irc_bot_add_message_handler(struct irc_bot *bot, int support_broadcast, struct irc_command_description *description);
extern struct irc_handler *irc_bot_add_notice_handler(struct irc_bot *bot, int support_broadcast, struct irc_command_description *description);
extern void irc_bot_remove_handler(struct irc_bot *bot, struct irc_handler *handler);

extern int irc_bot_send_message(struct irc_bot *bot, struct irc_component *comp, const char **args, int argc); // comp NULL = broadcast -- thread unsafe
extern int irc_bot_send_notice(struct irc_bot *bot, struct irc_component *comp, const char **args, int argc); // comp NULL = broadcast -- thread unsafe
extern int irc_bot_send_message_va(struct irc_bot *bot, struct irc_component *comp, int argc, ...); // comp NULL = broadcast -- thread unsafe
extern int irc_bot_send_notice_va(struct irc_bot *bot, struct irc_component *comp, int argc, ...); // comp NULL = broadcast -- thread unsafe
extern int irc_bot_send_reply(struct irc_bot *bot, struct irc_component *comp, const char *reply_fmt, ...); // thread unsafe
extern int irc_bot_send_reply_from_error(struct irc_bot *bot, struct irc_component *comp, const char *cmdname); // thread unsafe

#define irc_bot_read_parameters(bot, from, args, argc, ...) irc_bot_read_parameters_internal(bot, from, args, argc, INT_MAX, #__VA_ARGS__, __VA_ARGS__)
#define irc_bot_read_parameters_opt(bot, from, mandatory_count, args, argc, ...) irc_bot_read_parameters_internal(bot, from, args, argc, mandatory_count, #__VA_ARGS__, __VA_ARGS__)
extern int irc_bot_read_parameters_internal(struct irc_bot *bot, struct irc_component *from, const char **args, int argc, size_t mandatory_count, const char *va_names, ...); // no error set -- thread unsafe

#else // CORE

#include "core_api.h"

#define irc_bot_create(id, type, callbacks, ctx) (core_api->irc_bot_create(id, type, callbacks, ctx))
#define irc_bot_delete(bot) (core_api->irc_bot_delete(bot))
#define irc_bot_get_ctx(bot) (core_api->irc_bot_get_ctx(bot))
#define irc_bot_set_ctx(bot, ctx) (core_api->irc_bot_set_ctx(bot, ctx))
#define irc_bot_is_connected(bot) (core_api->irc_bot_is_connected(bot))
#define irc_bot_set_comp_status(bot, status) (core_api->irc_bot_set_comp_status(bot, status))

#define irc_get_me(bot) (core_api->irc_get_me(bot)
#define irc_comp_list(bot, callback, ctx) (core_api->irc_comp_list(bot, callback, ctx))
#define irc_comp_is_me(bot, comp) (core_api->irc_comp_is_me(bot, comp))
#define irc_comp_get_nick(bot, comp) (core_api->irc_comp_get_nick(bot, comp))
#define irc_comp_get_host(bot, comp) (core_api->irc_comp_get_host(bot, comp))
#define irc_comp_get_id(bot, comp) (core_api->irc_comp_get_id(bot, comp))
#define irc_comp_get_type(bot, comp) (core_api->irc_comp_get_type(bot, comp))
#define irc_comp_get_status(bot, comp) (core_api->irc_comp_get_status(bot, comp))

#define irc_bot_add_message_handler(bot, support_broadcast, description) (core_api->irc_bot_add_message_handler(bot, support_broadcast, description))
#define irc_bot_add_notice_handler(bot, support_broadcast, description) (core_api->irc_bot_add_notice_handler(bot, support_broadcast, description))
#define irc_bot_remove_handler(bot, handler) (core_api->irc_bot_remove_handler(bot, handler))

#define irc_bot_send_message(bot, comp, args, argc) (core_api->irc_bot_send_message(bot, comp, args, argc))
#define irc_bot_send_notice(bot, comp, args, argc) (core_api->irc_bot_send_notice(bot, comp, args, argc))
#define irc_bot_send_message_va(bot, comp, argc, ...) (core_api->irc_bot_send_message_va(bot, comp, argc, __VA_ARGS__))
#define irc_bot_send_notice_va(bot, comp, argc, ...) (core_api->irc_bot_send_notice_va(bot, comp, argc, __VA_ARGS__))
#define irc_bot_send_reply(bot, comp, reply_fmt, ...) (core_api->irc_bot_send_reply(bot, comp, reply_fmt, ##__VA_ARGS__))
#define irc_bot_send_reply_from_error(bot, comp, cmdname) (core_api->irc_bot_send_reply_from_error(bot, comp, cmdname))

#define irc_bot_read_parameters(bot, from, args, argc, ...) (core_api->irc_bot_read_parameters_internal(bot, from, args, argc, (INT_MAX, #__VA_ARGS__, __VA_ARGS__))
#define irc_bot_read_parameters_opt(bot, from, mandatory_count, args, argc, ...) (core_api->irc_bot_read_parameters_internal(bot, from, args, argc, mandatory_count, #__VA_ARGS__, __VA_ARGS__))

#endif // CORE

#endif /* IRC_H_ */
