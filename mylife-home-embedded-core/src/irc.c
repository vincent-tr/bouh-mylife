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
#include "list.h"
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

struct comp_lookup_data
{
	struct irc_component *result;

	const char *host;
	const char *id;
	const char *type;
};

struct nick_split
{
	char *host;
	char *id;
	char *type;
	char *status;
};

static void connect(struct irc_bot *bot);

static void nick_split(const char *nick, struct nick_split *split); // nick_split is not allocated => no free -- thread unsafe
static void nick_join(char *nick, const struct nick_split *split); // nick must be array of MAX_NICK+1 length -- thread unsafe

static void nick_new(struct irc_bot *bot, const char *nick); // return 1 if self else 0
static void nick_change(struct irc_bot *bot, const char *oldnick, const char *newnick);
static void nick_delete(struct irc_bot *bot, const char *nick); // return 1 if self else 0

static void comp_set_status(struct irc_component *comp, const char *status);
static struct irc_component *comp_lookup(struct irc_bot *bot, const char *host, const char *id, const char *type);
static int comp_lookup_callback(void *node, void *ctx);
static struct irc_component *comp_create(struct irc_bot *bot, const char *host, const char *id, const char *type, const char *nick); // nick or host/id/type can be null
static void comp_delete(struct irc_bot *bot, struct irc_component *comp);

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
	bot->ctx = ctx;

	list_init(&(bot->net));

	malloc_nofail(bot->me);
	strdup_nofail(bot->me->host, host);
	strdup_nofail(bot->me->id, id);
	strdup_nofail(bot->me->type, type);
	bot->me->status = NULL;

	struct nick_split split;
	split.host = bot->me->host;
	split.id = bot->me->id;
	split.type = bot->me->type;
	split.status = NULL;

	malloc_array_nofail(bot->me->nick, NICK_MAX+1);
	nick_join(bot->me->nick, &split);

	bot->connected = 0;
	connect(bot);

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
	comp_set_status(bot->me, status);
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

void connect(struct irc_bot *bot)
{
	const char *nick = bot->me->nick;
	log_assert(irc_connect(bot->session, CONFIG_IRC_SERVER, CONFIG_IRC_PORT, NULL, nick, nick, nick) == 0);
}

void nick_split(const char *nick, struct nick_split *split) // nick_split is not allocated => no free -- thread unsafe
{
	static char wnick[NICK_MAX+1];
	char *saveptr;

	strcpy(wnick, nick);
	memset(split, 0, sizeof(*split));

	if(!(split->host = strtok_r(wnick, "|", &saveptr)))
		return;
	if(!(split->id = strtok_r(NULL, "|", &saveptr)))
		return;
	if(!(split->type = strtok_r(NULL, "|", &saveptr)))
		return;
	split->status = strtok_r(NULL, "", &saveptr);
}

void nick_join(char *nick, const struct nick_split *split)
{
	if(split->status)
		snprintf(nick, NICK_MAX, "%s|%s|%s|%s", split->host, split->id, split->type, split->status);
	else
		snprintf(nick, NICK_MAX, "%s|%s|%s", split->host, split->id, split->type);
}

int nick_new(struct irc_bot *bot, const char *nick) // return 1 if self else 0
{
	if(!stricmp(bot->me->nick, nick))
	{
		list_add(&(bot->net), bot->me);
		return 1;
	}

	struct irc_component *comp = comp_create(bot, NULL, NULL, NULL, nick);

	void (*on_comp_new)(struct irc_bot *bot, struct irc_component *comp) = bot->callbacks.on_comp_new;
	if(on_comp_new)
		on_comp_new(bot, comp);

	return 0;
}

void nick_change(struct irc_bot *bot, const char *oldnick, const char *newnick)
{
	struct nick_split split;
	nick_split(oldnick, &split);

	struct irc_component *comp = comp_lookup(bot, split.host, split.id, split.type);
	if(!comp)
	{
		log_warning("component for nick '%s' not found", oldnick);
		return;
	}

	nick_split(newnick, &split);
	int samebase = !stricmp(comp->host, split.host)
			&& !stricmp(comp->id, split.id)
			&& !stricmp(comp->type, split.type);
	if(samebase)
	{
		comp_set_status(comp, split.status);

		void (*on_comp_change_status)(struct irc_bot *bot, struct irc_component *comp) = bot->callbacks.on_comp_change_status;
		if(on_comp_change_status)
			on_comp_change_status(bot, comp);

		return;
	}

	// la base a changé, on va faire un changement de composant mais ca ne devrait pas exister
	log_warning("nick change '%s' -> '%s' results in component delete and create", oldnick, newnick);

	// suppression
	void (*on_comp_delete)(struct irc_bot *bot, struct irc_component *comp) = bot->callbacks.on_comp_delete;
	if(on_comp_delete)
		on_comp_delete(bot, comp);

	comp_delete(bot, comp);

	// création
	comp = comp_create(bot, NULL, NULL, NULL, newnick);

	void (*on_comp_new)(struct irc_bot *bot, struct irc_component *comp) = bot->callbacks.on_comp_new;
	if(on_comp_new)
		on_comp_new(bot, comp);
}

int nick_delete(struct irc_bot *bot, const char *nick) // return 1 if self else 0
{
	if(!stricmp(bot->me->nick, nick))
	{
		list_remove(&(bot->me), bot->me);
		return 1;
	}

	struct nick_split split;
	nick_split(nick, &split);

	struct irc_component *comp = comp_lookup(bot, split.host, split.id, split.type);
	if(!comp)
	{
		log_warning("component for nick '%s' not found", nick);
		return 0;
	}

	void (*on_comp_delete)(struct irc_bot *bot, struct irc_component *comp) = bot->callbacks.on_comp_delete;
	if(on_comp_delete)
		on_comp_delete(bot, comp);

	comp_delete(bot, comp);
	return 0;
}

void comp_set_status(struct irc_component *comp, const char *status)
{
	if(comp->status)
		free(comp->status);
	comp->status = NULL;
	strdup_nofail(comp->status, status);
}

struct irc_component *comp_lookup(struct irc_bot *bot, const char *host, const char *id, const char *type)
{
	struct comp_lookup_data data;
	data.result = NULL;
	data.host = host;
	data.id = id;
	data.type = type;

	list_foreach(&(bot->net), comp_lookup_callback, &data);

	return data.result;
}

int comp_lookup_callback(void *node, void *ctx)
{
	struct irc_component *comp = node;
	struct comp_lookup_data *data = ctx;

	if(stricmp(comp->host, data->host))
		return 1;
	if(stricmp(comp->id, data->id))
		return 1;
	if(stricmp(comp->type, data->type))
		return 1;

	// found
	data->result = comp;
	return 0;
}

struct irc_component *comp_create(struct irc_bot *bot, const char *host, const char *id, const char *type, const char *nick) // nick or host/id/type can be null
{
	struct irc_component *comp;
	malloc_nofail(comp);
	memset(comp, 0, sizeof(*comp));

	if(nick)
		strdup_nofail(comp->nick, nick);
	if(host)
		strdup_nofail(comp->host, host);
	if(id)
		strdup_nofail(comp->id, id);
	if(type)
		strdup_nofail(comp->type, type);

	if(!nick && comp->host && comp->id && comp->type)
	{
		malloc_array_nofail(comp->nick, NICK_MAX+1);
		struct nick_split split;
		split.host = host;
		split.id = id;
		split.type = type;
		split.status = NULL;
		nick_join(comp->nick, &split);
	}

	if(!host && !id && !type && comp->nick)
	{
		struct nick_split split;
		nick_split(nick, &split);

		if(split.host)
			strdup_nofail(comp->host, split.host);
		if(split.id)
			strdup_nofail(comp->id, split.id);
		if(split.type)
			strdup_nofail(comp->type, split.type);
		if(split.status)
			strdup_nofail(comp->status, split.status);
	}

	list_add(&(bot->net), comp);
	return comp;
}

void comp_delete(struct irc_bot *bot, struct irc_component *comp)
{
	list_remove(&(bot->net), comp);
	if(comp->nick)
		free(comp->nick);
	if(comp->host)
		free(comp->host);
	if(comp->id)
		free(comp->id);
	if(comp->type)
		free(comp->type);
	if(comp->status)
		free(comp->status);
	free(comp);
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
