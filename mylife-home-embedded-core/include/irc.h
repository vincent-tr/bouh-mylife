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

struct irc_bot_callbacks
{
	void (*on_connected)(struct irc_bot *bot);
	void (*on_disconnected)(struct irc_bot *bot);

	void (*on_comp_new)(struct irc_bot *bot, struct irc_component *comp);
	void (*on_comp_delete)(struct irc_bot *bot, struct irc_component *comp);
	void (*on_comp_change_status)(struct irc_bot *bot, struct irc_component *comp);

	void (*on_message)(struct irc_bot *bot, struct irc_component *from, const char *text); // on chan only
	void (*on_notice)(struct irc_bot *bot, struct irc_component *from, const char *text); // on chan only
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

#else // CORE

#endif // CORE

#endif /* IRC_H_ */
