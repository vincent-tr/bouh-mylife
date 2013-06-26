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

#else // CORE

#endif // CORE

#endif /* IRC_H_ */
