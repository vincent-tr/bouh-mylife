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
#include <string.h>
#include <stdarg.h>

#include "libircclient.h"
#include "libirc_rfcnumeric.h"

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
 */

#define NICK_MAX 30
#define BUFFER_MAX 1024
#define ARGS_MAX 250

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
	struct loop_handle *listener;

	int connected; // means connected and on channel with names received

	struct irc_bot_callbacks callbacks;
	struct irc_component *me;
	struct list net;

	struct list handlers;
};

enum handler_type
{
	NOTICE,
	MESSAGE
};

struct irc_command
{
	struct list_node node;

	char *verb;
	char **description; // NULL terminated

	struct list children;

	void *ctx;
	irc_handler_callback callback;
};

struct irc_handler
{
	struct list_node node;

	enum handler_type type;
	int support_broadcast;
	struct irc_command *command;
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

struct handler_dispatch_data
{
	struct irc_bot *bot;
	enum handler_type type;
	struct irc_component *from;
	int is_broadcast;
	const char **args;
	int argc;
};

struct handler_help_data
{
	struct irc_bot *bot;
	struct irc_component *from;
};

struct handler_help_data_child
{
	struct irc_bot *bot;
	struct irc_component *from;
	const char *parent_verb;
};

static void connect(struct irc_bot *bot);
static void disconnected(struct irc_bot *bot);
static void comp_free(void *node, void *ctx);
static void handler_free(void *node, void *ctx);

static void nick_split(const char *nick, struct nick_split *split); // nick_split is not allocated => no free -- thread unsafe
static void nick_join(char *nick, const struct nick_split *split); // nick must be array of MAX_NICK+1 length -- thread unsafe

static int nick_new(struct irc_bot *bot, const char *nick); // return 1 if self else 0
static void nick_change(struct irc_bot *bot, const char *oldnick, const char *newnick);
static int nick_delete(struct irc_bot *bot, const char *nick); // return 1 if self else 0

static void comp_set_status(struct irc_component *comp, const char *status);
static struct irc_component *comp_lookup(struct irc_bot *bot, const char *host, const char *id, const char *type);
static int comp_lookup_callback(void *node, void *ctx);
static struct irc_component *comp_create(struct irc_bot *bot, const char *host, const char *id, const char *type, const char *nick, int add_to_list, int fire_callback); // nick or host/id/type can be null
static void comp_delete(struct irc_bot *bot, struct irc_component *comp, int remove_from_list, int fire_callback);
static struct irc_component *comp_lookup_by_origin(struct irc_bot *bot, const char *origin);

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

static void listener_callback_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
static void listener_callback_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);

static void on_message(struct irc_bot *bot, struct irc_component *from, const char *text);
static void on_notice(struct irc_bot *bot, struct irc_component *from, const char *text);
static void handler_dispatch(struct irc_bot *bot, enum handler_type type, struct irc_component *from, const char *target, const char **args, int argc);
static int handler_dispatch_item(void *node, void *ctx);
static void handler_dispatch_command(struct irc_bot *bot, struct irc_command *command, struct irc_component *from, int is_broadcast, const char **args, int argc);
static int handler_dispatch_command_help_child(void *node, void *ctx);
static int handler_dispatch_command_item(void *node, void *ctx);
static int handler_help_item(void *node, void *ctx);
static int handler_text_parse(const char *text, char **target, char ***args, int *argc); // thread unsafe
static struct irc_handler *handler_add(struct irc_bot *bot, int support_broadcast, struct irc_command_description *description, enum handler_type type);
static struct irc_command *handler_create_command(struct irc_command_description *description);
static void handler_delete(struct irc_handler *handler);
static void handler_delete_command(struct irc_command *command, void *useless);

static int irc_bot_send_va(struct irc_bot *bot, struct irc_component *comp, enum handler_type type, int argc, va_list list); // comp NULL = broadcast -- thread unsafe
static int irc_bot_send(struct irc_bot *bot, struct irc_component *comp, enum handler_type type, const char **args, int argc); // comp NULL = broadcast -- thread unsafe

static char host[HOST_NAME_MAX+1];
static irc_callbacks_t irc_callbacks;

void irc_init()
{
	log_assert(gethostname(host, HOST_NAME_MAX+1) != -1);

	memset(&irc_callbacks, sizeof(irc_callbacks), 0);
	irc_callbacks.event_connect = event_connect;
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

struct irc_bot *irc_bot_create(const char *id, const char *type, struct irc_bot_callbacks *callbacks, void *ctx)
{
	struct irc_bot *bot;
	malloc_nofail(bot);

	log_assert(bot->session = irc_create_session(&irc_callbacks));
	irc_set_ctx(bot->session, bot);

	memcpy(&(bot->callbacks), callbacks, sizeof(*callbacks));
	bot->ctx = ctx;

	bot->listener = loop_register_listener(listener_callback_add, listener_callback_process, bot);

	list_init(&(bot->net));
	bot->me = comp_create(bot, host, id, type, NULL, 0, 0);

	list_init(&(bot->handlers));

	bot->connected = 0;
	connect(bot);

	return bot;
}

void irc_bot_delete(struct irc_bot *bot)
{
	if(irc_is_connected(bot->session))
		irc_disconnect(bot->session);
	irc_destroy_session(bot->session);

	disconnected(bot);

	loop_unregister(bot->listener);

	comp_delete(bot, bot->me, 0, 0);
	list_clear(&(bot->handlers), handler_free, bot);
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

	// new nick
	struct irc_component *me = bot->me;
	struct nick_split split;
	split.host = me->host;
	split.id = me->id;
	split.type = me->type;
	split.status = me->status;
	if(me->nick)
		free(me->nick);
	malloc_array_nofail(me->nick, NICK_MAX+1);
	nick_join(me->nick, &split);

	// nick change if connected
	if(irc_is_connected(bot->session))
		irc_cmd_nick(bot->session, me->nick);
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

void disconnected(struct irc_bot *bot)
{
	bot->connected = 0;

	struct irc_component *comp = irc_get_me(bot);
	list_remove(&(bot->net), comp);
	list_clear(&(bot->net), comp_free, bot);
}

void comp_free(void *node, void *ctx)
{
	struct irc_bot *bot = ctx;
	struct irc_component *comp = node;

	comp_delete(bot, comp, 0, 1);
}

void handler_free(void *node, void *ctx)
{
	//struct irc_bot *bot = ctx;
	struct irc_handler *handler = node;

	handler_delete(handler);
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
	if(!strcasecmp(bot->me->nick, nick))
	{
		list_add(&(bot->net), bot->me);
		return 1;
	}

	comp_create(bot, NULL, NULL, NULL, nick, 1, 1);

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
	int samebase = !strcasecmp(comp->host, split.host)
			&& !strcasecmp(comp->id, split.id)
			&& !strcasecmp(comp->type, split.type);
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
	comp_delete(bot, comp, 1, 1);

	// création
	comp = comp_create(bot, NULL, NULL, NULL, newnick, 1, 1);
}

int nick_delete(struct irc_bot *bot, const char *nick) // return 1 if self else 0
{
	if(!strcasecmp(bot->me->nick, nick))
	{
		list_remove(&(bot->net), bot->me);
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

	comp_delete(bot, comp, 1, 1);
	return 0;
}

void comp_set_status(struct irc_component *comp, const char *status)
{
	if(comp->status)
		free(comp->status);

	comp->status = NULL;
	if(status)
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

	if(strcasecmp(comp->host, data->host))
		return 1;
	if(strcasecmp(comp->id, data->id))
		return 1;
	if(strcasecmp(comp->type, data->type))
		return 1;

	// found
	data->result = comp;
	return 0;
}

struct irc_component *comp_create(struct irc_bot *bot, const char *host, const char *id, const char *type, const char *nick, int add_to_list, int fire_callback) // nick or host/id/type can be null
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
		// const-cast because in nick_join split is not modified
		split.host = (char *)host;
		split.id = (char *)id;
		split.type = (char *)type;
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

	if(add_to_list)
		list_add(&(bot->net), comp);

	if(fire_callback)
	{
		void (*on_comp_new)(struct irc_bot *bot, struct irc_component *comp) = bot->callbacks.on_comp_new;
		if(on_comp_new)
			on_comp_new(bot, comp);
	}

	return comp;
}

void comp_delete(struct irc_bot *bot, struct irc_component *comp, int remove_from_list, int fire_callback)
{
	if(fire_callback)
	{
		void (*on_comp_delete)(struct irc_bot *bot, struct irc_component *comp) = bot->callbacks.on_comp_delete;
		if(on_comp_delete)
			on_comp_delete(bot, comp);
	}

	if(remove_from_list)
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

struct irc_component *comp_lookup_by_origin(struct irc_bot *bot, const char *origin)
{
	char nick[NICK_MAX+1];
	irc_target_get_nick(origin, nick, NICK_MAX+1);

	struct nick_split split;
	nick_split(nick, &split);

	return comp_lookup(bot, split.host, split.id, split.type);
}

// --- events ---
void event_connect(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	struct irc_bot *bot = irc_get_ctx(session);

	// on connection we set nick appropriatly and join the rooms
	irc_cmd_nick(session, bot->me->nick);
	irc_cmd_join(session, CONFIG_IRC_CHANNEL, NULL);
}

void event_kick(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count) // + tracking
{
	struct irc_bot *bot = irc_get_ctx(session);

	const char *channel = params[0];
	if(strcasecmp(channel, CONFIG_IRC_CHANNEL))
		return;

	const char *nick = params[1];
	if(!strcasecmp(nick, bot->me->nick))
	{
		// it is us !
		disconnected(bot);

		// trying to re-join
		irc_cmd_join(session, CONFIG_IRC_CHANNEL, NULL);
	}
	else
	{
		nick_delete(bot, nick);
	}
}

void event_channel(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	struct irc_bot *bot = irc_get_ctx(session);

	const char *channel = params[0];
	if(strcasecmp(channel, CONFIG_IRC_CHANNEL))
		return;

	struct irc_component *comp = comp_lookup_by_origin(bot, origin);
	if(!comp) // message from unknown source, ignoring
		return;

	const char *text;
	if(count < 2 || !(text = params[1]))
		return;

	on_message(bot, comp, text);
}

void event_privmsg(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	/*
	 * not implemented for now
	 *
	struct irc_bot *bot = irc_get_ctx(session);

	struct irc_component *comp = comp_lookup_by_origin(origin);
	if(!comp) // message from unknown source, ignoring
		return;

	const char *text;
	if(count < 2 || !(text = params[1]))
		return;
	*/
}

void event_notice(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	/*
	 * not implemented for now
	 *
	struct irc_bot *bot = irc_get_ctx(session);

	struct irc_component *comp = comp_lookup_by_origin(origin);
	if(!comp) // message from unknown source, ignoring
		return;

	const char *text;
	if(count < 2 || !(text = params[1]))
		return;
	*/
}

void event_channel_notice(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	struct irc_bot *bot = irc_get_ctx(session);

	const char *channel = params[0];
	if(strcasecmp(channel, CONFIG_IRC_CHANNEL))
		return;

	struct irc_component *comp = comp_lookup_by_origin(bot, origin);
	if(!comp) // message from unknown source, ignoring
		return;

	const char *text;
	if(count < 2 || !(text = params[1]))
		return;

	on_notice(bot, comp, text);
}

// tracking
void event_nick(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	struct irc_bot *bot = irc_get_ctx(session);

	char oldnick[NICK_MAX+1];
	irc_target_get_nick(origin, oldnick, NICK_MAX+1);

	const char *newnick = params[0];

	nick_change(bot, oldnick, newnick);
}

void event_quit(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	struct irc_bot *bot = irc_get_ctx(session);

	char nick[NICK_MAX+1];
	irc_target_get_nick(origin, nick, NICK_MAX+1);

	nick_delete(bot, nick);
}

void event_join(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	struct irc_bot *bot = irc_get_ctx(session);

	const char *channel = params[0];
	if(strcasecmp(channel, CONFIG_IRC_CHANNEL))
		return;

	char nick[NICK_MAX+1];
	irc_target_get_nick(origin, nick, NICK_MAX+1);

	// if it is us we don't add because we will on names
	if(!strcasecmp(bot->me->nick, nick))
		return;

	nick_new(bot, nick);
}

void event_part(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	struct irc_bot *bot = irc_get_ctx(session);

	const char *channel = params[0];
	if(strcasecmp(channel, CONFIG_IRC_CHANNEL))
		return;

	char nick[NICK_MAX+1];
	irc_target_get_nick(origin, nick, NICK_MAX+1);

	nick_delete(bot, nick);
}

// misc
void event_unknown(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	/*
	 * not implemented
	 *
	struct irc_bot *bot = irc_get_ctx(session);

	 */
}

void event_numeric(irc_session_t * session, unsigned int event, const char * origin, const char ** params, unsigned int count)
{
	struct irc_bot *bot = irc_get_ctx(session);
	//int arg_idx;
	const char *nick;
	char *nick_list;
	const char *channel;
	char *tok_save;

	switch(event)
	{
	case LIBIRC_RFC_RPL_NAMREPLY:
		// http://tools.ietf.org/html/rfc2812#section-3.2.1
		// const char *self = params[0] ???
		// params[1] = "=" for public channels
		channel = params[2];
		if(strcasecmp(channel, CONFIG_IRC_CHANNEL))
			return;

		strdup_nofail(nick_list, params[3]);
		nick = strtok_r(nick_list, " ", &tok_save);
		while(nick)
		{
			// {~&@%+}nick
			switch(nick[0])
			{
				case '~':
				case '&':
				case '@':
				case '%':
				case '+':
					++nick;
					break;
			}
			nick_new(bot, nick);
			nick = strtok_r(NULL, " ", &tok_save);
		}
		break;

	case LIBIRC_RFC_RPL_ENDOFNAMES:
		// const char *self = params[0] ???
		channel = params[1];
		if(strcasecmp(channel, CONFIG_IRC_CHANNEL))
			return;
		// end of nick list => we are fully connected
		bot->connected = 1;
		break;
	}
}
// --- events ---

void listener_callback_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	struct irc_bot *bot = ctx;
	irc_add_select_descriptors(bot->session, readfds, writefds, nfds);
}

void listener_callback_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	struct irc_bot *bot = ctx;
	irc_process_select_descriptors(bot->session, readfds, writefds);
}

void on_message(struct irc_bot *bot, struct irc_component *from, const char *text)
{
	char *target;
	char **args;
	int argc;

	if(!handler_text_parse(text, &target, &args, &argc))
		return; // not a command

	handler_dispatch(bot, MESSAGE, from, target, (const char **)args, argc);
}

void on_notice(struct irc_bot *bot, struct irc_component *from, const char *text)
{
	char *target;
	char **args;
	int argc;

	if(!handler_text_parse(text, &target, &args, &argc))
		return; // not a command

	handler_dispatch(bot, NOTICE, from, target, (const char **)args, argc);

}

int handler_text_parse(const char *text, char **target, char ***args, int *argc) // thread unsafe
{
	static char buffer[BUFFER_MAX];
	static char *wargs[ARGS_MAX];
	char *ptr;
	int args_index;

	strcpy(buffer, text);
	memset(wargs, 0, sizeof(wargs));
	args_index = 0;

	ptr = buffer;
	if(*ptr != '!')
		return 0;
	++ptr;

	// target
	*target = ptr;
	while (*ptr && *ptr != ' ')
		ptr++;
	*ptr++ = '\0';

	while (*ptr &&  args_index < ARGS_MAX)
	{
		// beginning from ':', this is the last param
		if ( *ptr == ':' )
		{
			wargs[args_index++] = ptr + 1; // skip :
			break;
		}

		// Just a param
		for(wargs[args_index++] = ptr; *ptr && *ptr != ' '; ptr++);

		if ( !*ptr )
			break;

		*ptr++ = '\0';
	}

	*args = wargs;
	*argc = args_index;

	return 1;
}

void handler_dispatch(struct irc_bot *bot, enum handler_type type, struct irc_component *from, const char *target, const char **args, int argc)
{
	int is_broadcast = 0;
	if(!strcasecmp(target, "*"))
	{
		is_broadcast = 1;
	}
	else
	{
		struct nick_split split;
		nick_split(target, &split);
		struct irc_component *target_comp = comp_lookup(bot, split.host, split.id, split.type);
		if(target_comp != bot->me)
			return; // not a broadcast and not for us
	}

	// special command : 'help' on root
	if(argc > 0 && !strcasecmp("help", args[0]))
	{
		if(!is_broadcast)
		{
			struct handler_help_data data;
			data.bot = bot;
			data.from = from;
			list_foreach(&(bot->handlers), handler_help_item, &data);
		}
		return;
	}

	struct handler_dispatch_data data;
	data.bot = bot;
	data.type = type;
	data.from = from;
	data.is_broadcast = is_broadcast;
	data.args = args;
	data.argc = argc;

	list_foreach(&(bot->handlers), handler_dispatch_item, &data);
}

int handler_dispatch_item(void *node, void *ctx)
{
	struct handler_dispatch_data *data = ctx;
	struct irc_handler *handler = node;

	if(handler->type != data->type)
		return 1;
	if(!handler->support_broadcast && data->is_broadcast) // handler is not listening to broadcast but message is broadcast
		return 1;

	const char **args = data->args;
	int argc = data->argc;

	if(!handler->command->verb)
	{
		// no verb => spying, send with all args
		handler->command->callback(data->bot, data->from, data->is_broadcast, args, argc, handler->command->ctx);
		return 1;
	}

	// else check
	if(argc && !strcasecmp(handler->command->verb, args[0]))
	{
		handler_dispatch_command(data->bot, handler->command, data->from, data->is_broadcast, args + 1, argc - 1);
	}

	return 1;
}

void handler_dispatch_command(struct irc_bot *bot, struct irc_command *command, struct irc_component *from, int is_broadcast, const char **args, int argc)
{
	// special help
	if(argc > 0 && !strcasecmp("help", args[0]))
	{
		if(!is_broadcast)
		{
			irc_bot_send_notice_va(bot, from, 2, "help", command->verb);

			if(command->description)
			{
				char **desc;
				for(desc = command->description; *desc; ++desc)
					irc_bot_send_notice_va(bot, from, 4, "help", command->verb, "desc", *desc);
			}

			struct handler_help_data_child data;
			data.bot = bot;
			data.from = from;
			data.parent_verb = command->verb;
			list_foreach(&(command->children), handler_dispatch_command_help_child, &data);
		}

		return;
	}

	// if impl then execute it
	if(command->callback)
	{
		command->callback(bot, from, is_broadcast, args, argc, command->ctx);
		return;
	}

	// is no args anymore then nothing to do ..
	if(!argc)
		return;

	// else find sub commands
	struct handler_dispatch_data data;
	data.bot = bot;
	data.from = from;
	data.is_broadcast = is_broadcast;
	data.args = args;
	data.argc = argc;
	//data.type useless
	list_foreach(&(command->children), handler_dispatch_command_item, &data);
}

int handler_dispatch_command_help_child(void *node, void *ctx)
{
	struct handler_help_data_child *data = ctx;
	struct irc_command *command = node;

	irc_bot_send_notice_va(data->bot, data->from, 4, "help", data->parent_verb, "child", command->verb);

	return 1;
}

int handler_dispatch_command_item(void *node, void *ctx)
{
	struct handler_dispatch_data *data = ctx;
	struct irc_command *command = node;

	// check is the command fetch (argc is > 0, already checked)
	if(!strcasecmp(command->verb, data->args[0]))
	{
		handler_dispatch_command(data->bot, command, data->from, data->is_broadcast, data->args + 1, data->argc - 1);
	}

	return 1;
}

int handler_help_item(void *node, void *ctx)
{
	struct handler_help_data *data = ctx;
	struct irc_handler *handler = node;

	static char buffer[BUFFER_MAX];

	if(!handler->command->verb)
		return 1;

	snprintf(buffer, BUFFER_MAX, "%s : %s", handler->command->verb, handler->type == MESSAGE ? "message" : "notice");
	buffer[BUFFER_MAX - 1] = '\0';
	if(handler->support_broadcast)
	{
		strncat(buffer, ", support_broadcast", BUFFER_MAX);
		buffer[BUFFER_MAX - 1] = '\0';
	}
	irc_bot_send_notice_va(data->bot, data->from, 2, "help", buffer);

	if(handler->command->description)
	{
		char **desc;
		for(desc = handler->command->description; *desc; ++desc)
			irc_bot_send_notice_va(data->bot, data->from, 4, "help", handler->command->verb, "desc", *desc);
	}

	return 1;
}

struct irc_handler *irc_bot_add_message_handler(struct irc_bot *bot, int support_broadcast, struct irc_command_description *description)
{
	return handler_add(bot, support_broadcast, description, MESSAGE);
}

struct irc_handler *irc_bot_add_notice_handler(struct irc_bot *bot, int support_broadcast, struct irc_command_description *description)
{
	return handler_add(bot, support_broadcast, description, NOTICE);
}

struct irc_handler *handler_add(struct irc_bot *bot, int support_broadcast, struct irc_command_description *description, enum handler_type type)
{
	log_assert(description->verb || description->callback); // if no verb callback must be supplied

	struct irc_handler *handler;
	malloc_nofail(handler);

	handler->type = type;
	handler->support_broadcast = support_broadcast;
	handler->command = handler_create_command(description);

	list_add(&(bot->handlers), handler);
	return handler;
}

struct irc_command *handler_create_command(struct irc_command_description *description)
{
	struct irc_command *cmd;
	malloc_nofail(cmd);
	memset(cmd, 0, sizeof(*cmd));

	if(description->verb)
		strdup_nofail(cmd->verb, description->verb);

	if(description->description)
	{
		int count;
		char **ptr;
		char **dptr;
		for(ptr = description->description, count = 0; *ptr; ++ptr, ++count);
		malloc_array_nofail(cmd->description, count + 1);
		for(ptr = description->description, dptr = cmd->description; *ptr; ++ptr, ++dptr)
			strdup_nofail(*dptr, *ptr);
		*dptr = NULL;
	}

	list_init(&(cmd->children));
	if(description->children)
	{
		struct irc_command_description **child = description->children;
		while(*child)
		{
			struct irc_command *childcmd = handler_create_command(*(child++));
			list_add(&(cmd->children), childcmd);
		}
	}

	cmd->ctx = description->ctx;
	cmd->callback = description->callback;

	return cmd;
}

void handler_delete(struct irc_handler *handler)
{
	handler_delete_command(handler->command, NULL);
	free(handler);
}

void handler_delete_command(struct irc_command *command, void *useless)
{
	if(command->verb)
		free(command->verb);
	if(command->description)
	{
		char **ptr = command->description;
		while(*ptr)
			free(*(ptr++));
		free(command->description);
	}

	list_clear(&(command->children), (void (*)(void *, void*))handler_delete_command, NULL);
	free(command);
}

void irc_bot_remove_handler(struct irc_bot *bot, struct irc_handler *handler)
{
	list_remove(&(bot->handlers), handler);
	handler_delete(handler);
}

int irc_bot_send_message(struct irc_bot *bot, struct irc_component *comp, const char **args, int argc) // comp NULL = broadcast -- thread unsafe
{
	return irc_bot_send(bot, comp, MESSAGE, args, argc);
}

int irc_bot_send_notice(struct irc_bot *bot, struct irc_component *comp, const char **args, int argc) // comp NULL = broadcast -- thread unsafe
{
	return irc_bot_send(bot, comp, NOTICE, args, argc);
}

int irc_bot_send_message_va(struct irc_bot *bot, struct irc_component *comp, int argc, ...) // comp NULL = broadcast -- thread unsafe
{
	va_list list;
	va_start(list, argc);
	int ret = irc_bot_send_va(bot, comp, MESSAGE, argc, list);
	va_end(list);
	return ret;
}

int irc_bot_send_notice_va(struct irc_bot *bot, struct irc_component *comp, int argc, ...) // comp NULL = broadcast -- thread unsafe
{
	va_list list;
	va_start(list, argc);
	int ret = irc_bot_send_va(bot, comp, NOTICE, argc, list);
	va_end(list);
	return ret;
}

int irc_bot_send_va(struct irc_bot *bot, struct irc_component *comp, enum handler_type type, int argc, va_list list) // comp NULL = broadcast
{
	static char *args[ARGS_MAX];
	int i;

	for(i=0; i<argc; i++)
		args[i] = va_arg(list, char *);

	return irc_bot_send(bot, comp, type, (const char **)args, argc);
}

int irc_bot_send(struct irc_bot *bot, struct irc_component *comp, enum handler_type type, const char **args, int argc) // comp NULL = broadcast
{
	static char buffer[BUFFER_MAX];
	static char nick[NICK_MAX+1];
	int idx;

	if(!irc_bot_is_connected(bot))
		return 0;

	if(comp)
	{
		struct nick_split split;
		split.host = comp->host;
		split.id = comp->id;
		split.type = comp->type;
		split.status = NULL;
		nick_join(nick, &split);
	}
	else
	{
		// broadcast
		strcpy(nick, "*");
	}

	int ret = 0;
	irc_session_t *session = bot->session;

	snprintf(buffer, BUFFER_MAX, "!%s", nick);
	buffer[BUFFER_MAX-1] = '\0';

	for(idx = 0; idx < argc - 1; ++idx)
	{
		strncat(buffer, " ", BUFFER_MAX-1);
		strncat(buffer, args[idx], BUFFER_MAX-1);
	}

	if(argc > 0)
	{
		strncat(buffer, " :", BUFFER_MAX-1);
		strncat(buffer, args[argc-1], BUFFER_MAX-1);
	}

	switch(type)
	{
	case MESSAGE:
		ret = irc_cmd_msg(session, CONFIG_IRC_CHANNEL, buffer);
		break;

	case NOTICE:
		ret = irc_cmd_notice(session, CONFIG_IRC_CHANNEL, buffer);
		break;
	}

	return ret;
}

int irc_bot_send_reply(struct irc_bot *bot, struct irc_component *comp, const char *reply_fmt, ...)
{
	static char buffer[BUFFER_MAX];

	va_list ap;
	va_start(ap, reply_fmt);
	vsnprintf(buffer, BUFFER_MAX, reply_fmt, ap);
	buffer[BUFFER_MAX-1] = '\0';
	va_end(ap);

	return irc_bot_send_notice_va(bot, comp, 2, "reply", buffer);
}

int irc_bot_read_parameters_internal(struct irc_bot *bot, struct irc_component *from, const char **args, int argc, size_t mandatory_count, const char *va_names, ...)
{
	// http://stackoverflow.com/questions/5957679/is-there-a-way-to-use-c-preprocessor-stringification-on-variadic-macro-argumen
	static char buffer[ARGS_MAX * 10];
	strcpy(buffer, va_names);
	size_t idx = 0;
	va_list ap;

	va_start(ap, va_names);

	int ret = 1;

	for(char *token = strtok(buffer, ","); token; token = strtok(NULL, ","), ++idx)
	{
		// strip & and ' '
		while(*token == '&' || *token == ' ')
			++token;
		char *ptr = token + strlen(token) - 1; // pos on last char
		while(*ptr == '&' || *ptr == ' ')
			*(ptr++) = 0;

		char ** varg = va_arg(ap, char **);

		const char *arg = NULL;
		if(idx + 1 > argc)
		{
			// more data to read than available
			if(mandatory_count > idx)
			{
				irc_bot_send_reply(bot, from, "missing parameter : %s", token);
				ret = 0;
			}
		}
		else
		{
			arg = args[idx];
		}

		*varg = (char*)arg;
	}

	va_end(ap);
	return ret;
}
