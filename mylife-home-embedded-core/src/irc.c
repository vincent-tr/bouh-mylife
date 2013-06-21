/*
 * irc.c
 *
 *  Created on: 16 juin 2013
 *      Author: pumbawoman
 */

#define _BSD_SOURCE
#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>
#include <unistd.h>

#include "libircclient.h"

#include "irc.h"
#include "loop.h"
#include "logger.h"
#include "tools.h"
#include "config_base.h"

#ifndef HOST_NAME_MAX
//#define HOST_NAME_MAX (sysconf(_SC_HOST_NAME_MAX))
#define HOST_NAME_MAX 255
#endif

/*
 * nick irc : host|id|type|status
 *
 */

#define NICK_MAX 30

struct irc_component
{
	struct list_node node;

	char *nick;

	char *host;
	char *id;
	char *type;
	char *status;
};

struct irc_bot
{
	void *ctx;
	irc_session_t *session; // session.ctx = self
	int connected; // means connected and on channel with names received

	struct irc_bot_callbacks callbacks;
	struct irc_component *me;
	struct list net;
};

static void nick_new(struct irc_bot *bot, const char *nick);
static void nick_change(struct irc_bot *bot, const char *oldnick, const char *newnick);
static void nick_delete(struct irc_bot *bot, const char *nick);

static void comp_set_status(struct irc_component *comp, const char *status);
static struct irc_component *comp_lookup(struct irc_bot *bot, const char *host, const char *id, const char *type);

// --- events ---
static void event_connect(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static void event_kick(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count); // + tracking
static void event_channel(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static void event_privmsg(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static void event_notice(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static void event_channel_notice(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
// tracking
static void event_nick(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static void event_quit(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static void event_join(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static void event_part(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
// misc
static void event_unknown(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static void event_numeric(irc_session_t * session, unsigned int event, const char * origin, const char ** params, unsigned int count);
// --- events ---

static char host[HOST_NAME_MAX+1];
static irc_callbacks_t irc_callbacks;

void irc_init()
{
	log_assert(gethostname(host, HOST_NAME_MAX+1) != -1);

	memset(&irc_callbacks, sizeof(irc_callbacks), 0);
	irc_callbacks.event_channel = event_connect;
	irc_callbacks.event_kick = event_kick; // + tracking
	irc_callbacks.event_channel = event_channel;
	irc_callbacks.event_privmsg = event_privmsg;
	irc_callbacks.event_notice = event_notice;
	irc_callbacks.event_channel_notice = event_channel_notice;
	//tracking
	irc_callbacks.event_nick = event_nick;
	irc_callbacks.event_quit = event_quit;
	irc_callbacks.event_join = event_join;
	irc_callbacks.event_part = event_part;
	// misc
	irc_callbacks.event_unknown = event_unknown;
	irc_callbacks.event_numeric = event_numeric;
}

void irc_terminate()
{
}

struct irc_bot *irc_create(const char *id, const char *type, struct irc_bot_callbacks *callbacks, void *ctx)
{
	struct irc_bot *bot;
	malloc_nofail(bot);

	log_assert(bot->session = irc_create_session(&irc_callbacks));
	irc_set_ctx(bot->session, bot);
	memcpy(&(bot->callbacks), callbacks, sizeof(*callbacks));
	list_init(&(bot->net));

	strdup_nofail(bot->id, id);
	strdup_nofail(bot->type, type);
	bot->status = NULL;
	bot->connected = 0;

	bot->nick = NULL;
	make_nick(bot);
	const char *nick = bot->nick;
	log_assert(irc_connect(bot->session, CONFIG_IRC_SERVER, CONFIG_IRC_PORT, NULL, nick, nick, nick) == 0);

	return bot;
}

void irc_delete(struct irc_bot *bot)
{
	if(irc_is_connected(bot->session))
		irc_disconnect(bot->session);
	irc_destroy_session(bot->session);

	free(bot->id);
	free(bot->type);
	if(bot->status)
		free(bot->status);
	if(bot->nick)
		free(bot->nick);
	free(bot);
}

void *irc_bot_get_ctx(struct irc_bot *bot)
{
	return bot->ctx;
}

void irc_bot_set_ctx(struct irc_bot *bot, void *ctx)
{
	bot->ctx = ctx;
}

int irc_bot_is_connected(struct irc_bot *bot)
{
	return bot->connected;
}

void irc_bot_set_comp_status(struct irc_bot *bot, const char *status)
{
	if(bot->status)
		free(bot->status);
	bot->status = NULL;
	if(status)
		strdup_nofail(bot->status, status);
	// TODO : changement nick
}

struct irc_component *irc_get_me(struct irc_bot *bot)
{
	return bot->me;
}

void irc_comp_list(struct irc_bot *bot, int (*callback)(struct irc_component *comp, void *ctx), void *ctx)
{
	list_foreach(&(bot->net), (int (*)(void *node, void *ctx))callback, ctx);
}

int irc_comp_is_me(struct irc_bot *bot, struct irc_component *comp)
{
	return bot->me == comp;
}

const char *irc_comp_get_nick(struct irc_bot *bot, struct irc_component *comp)
{
	return comp->nick;
}

const char *irc_comp_get_host(struct irc_bot *bot, struct irc_component *comp) // NULL if unrecognized nick format
{
	return comp->host;
}

const char *irc_comp_get_id(struct irc_bot *bot, struct irc_component *comp) // NULL if unrecognized nick format
{
	return comp->id;
}

const char *irc_comp_get_type(struct irc_bot *bot, struct irc_component *comp) // NULL if unrecognized nick format
{
	return comp->type;
}

const char *irc_comp_get_status(struct irc_bot *bot, struct irc_component *comp) // NULL if unrecognized nick format or if no status
{
	return comp->status;
}

void make_nick(struct irc_bot *bot)
{
	char *nick = bot->nick;
	if(nick)
		free(nick);
	malloc_array_nofail(nick, NICK_MAX+1);
	bot->nick = nick;

	if(bot->status)
		snprintf(nick, NICK_MAX, "%s|%s|%s|%s", host, bot->id, bot->type, bot->status);
	else
		snprintf(nick, NICK_MAX, "%s|%s|%s", host, bot->id, bot->type);

	nick[NICK_MAX] = '\0';
}

void nick_new(struct irc_bot *bot, const char *nick)
{

}

void nick_change(struct irc_bot *bot, const char *oldnick, const char *newnick)
{

}

void nick_delete(struct irc_bot *bot, const char *nick)
{

}

// --- events ---
void event_connect(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	// TODO
}

void event_kick(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count) // + tracking
{

}

void event_channel(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{

}

void event_privmsg(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{

}

void event_notice(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{

}

void event_channel_notice(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{

}

// tracking
void event_nick(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{

}

void event_quit(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{

}

void event_join(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{

}

void event_part(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{

}

// misc
void event_unknown(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{

}

void event_numeric(irc_session_t * session, unsigned int event, const char * origin, const char ** params, unsigned int count)
{

}
// --- events ---
