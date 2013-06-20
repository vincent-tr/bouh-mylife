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

static char host[HOST_NAME_MAX+1];

struct irc_bot
{
	irc_session_t *session;
	int connected;
	char *nick;

	char *id;
	char *type;
	char *status;

	// TODO
};

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

void make_nick(struct irc_bot *bot);

void irc_init()
{
	log_assert(gethostname(host, HOST_NAME_MAX+1) != -1);
}

void irc_terminate()
{
}

struct irc_bot *irc_create(const char *id, const char *type)
{
	irc_callbacks_t *callbacks;
	malloc_nofail(callbacks);
	memset(callbacks, sizeof(*callbacks), 0);

	callbacks->event_connect = event_connect;
	callbacks->event_kick = event_kick; // + tracking
	callbacks->event_channel = event_channel;
	callbacks->event_privmsg = event_privmsg;
	callbacks->event_notice = event_notice;
	callbacks->event_channel_notice = event_channel_notice;

	//tracking
	callbacks->event_nick = event_nick;
	callbacks->event_quit = event_quit;
	callbacks->event_join = event_join;
	callbacks->event_part = event_part;

	// misc
	callbacks->event_unknown = event_unknown;
	callbacks->event_numeric = event_numeric;

	struct irc_bot *bot;
	malloc_nofail(bot);

	log_assert(bot->session = irc_create_session(callbacks));
	irc_set_ctx(bot->session, bot);

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

void irc_set_status(struct irc_bot *bot, const char *status)
{
	if(bot->status)
		free(bot->status);
	bot->status = NULL;
	if(status)
		strdup_nofail(bot->status, status);
	// TODO : changement nick
}

const char *irc_get_status(struct irc_bot *bot)
{
	return bot->status;
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
