/*
 * irc.h
 *
 *  Created on: 16 juin 2013
 *      Author: pumbawoman
 */

#ifndef IRC_H_
#define IRC_H_

struct irc_bot;

#ifdef CORE

extern void irc_init();
extern void irc_terminate();

extern struct irc_bot *irc_create(const char *id, const char *type);
extern void irc_delete(struct irc_bot *bot);

extern void irc_set_status(struct irc_bot *bot, const char *status);
extern const char *irc_get_status(struct irc_bot *bot);

#else // CORE

#endif // CORE

#endif /* IRC_H_ */
