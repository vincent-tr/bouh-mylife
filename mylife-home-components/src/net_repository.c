/*
 * net_repository.c
 *
 *  Created on: Jan 11, 2014
 *      Author: vincent
 */

#include <stdio.h>
#include <stddef.h>
#include <stdarg.h>
#include <libircclient/libircclient.h>
#include <libircclient/libirc_rfcnumeric.h>

#include "net.h"
#include "logger.h"
#include "tools.h"
#include "list.h"
#include "config.h"
#include "loop.h"

struct net_container
{
	struct list_node node;
	struct net_object *object;
	int local;
	char *channel;

	int (*is_connected)(struct net_container *container);
	void (*unregister)(struct net_container *container);
	void *data;
};

struct internal_local_container
{
	struct loop_handle *loop_handle;
	irc_session_t * session;
	char *nick;
	int connected;

	struct list attribute_changed_handlers;
};

struct internal_remote_container
{

};

struct attribute_changed_handler
{
	struct list_node node;
	const char *name;
	void *handler;
};

struct local_make_nick_data
{
	char *local_nick;
	struct net_object *object;
};

struct local_call_param_data
{
	struct net_value **values;
	char *saveptr;
};

static struct list repository;
static irc_callbacks_t local_callbacks;
static const char *server_address;
static int server_port;

static void irc_assert(irc_session_t * session, int irc_ret);
static char *irc_channel(const char *channel);
static int irc_id_equals(const char *nick1, const char *nick2);
static void irc_error(irc_session_t *session, const char *target, const char *format, ...);

static void local_register(struct net_container *container);
static void local_unregister(struct net_container *container);
static void local_callback_select_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
static void local_callback_select_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
static int local_is_connected(struct net_container *container);
static int local_register_member(struct net_member *member, void *ctx);
static void local_unregister_member(void *node, void *ctx);
static void local_attribute_changed(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[]);
static void local_event_connect(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static void local_event_numeric(irc_session_t * session, unsigned int event, const char * origin, const char ** params, unsigned int count);
static void local_event_message(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count);
static int local_call_param(struct net_type *type, void *ctx);
static void local_make_nick(struct net_container *container);
static int local_make_nick_item(struct net_member *member, void *ctx);

static void remote_register(struct net_container *container);

void net_init()
{
	memset(&local_callbacks, 0, sizeof(local_callbacks));

	local_callbacks.event_connect = local_event_connect;
	local_callbacks.event_numeric = local_event_numeric;
	local_callbacks.event_channel = local_event_message;
	local_callbacks.event_privmsg = local_event_message;

	conf_assert(config_lookup_string(conf_get(), "net.server.address", &server_address));
	conf_assert(config_lookup_int(conf_get(), "net.server.port", &server_port));

	list_init(&repository);
}

void net_terminate()
{
	log_assert(list_is_empty(&repository));
}

struct net_container *net_repository_register(struct net_object *object, const char *channel, int local)
{
	struct net_container *container;
	malloc_nofail(container);
	container->object = object;
	strdup_nofail(container->channel, channel);
	container->local = local;

	if(local)
		local_register(container);
	else
		remote_register(container);

	list_add(&repository, container);
	return container;
}

void net_repository_unregister(struct net_container *container)
{
	list_remove(&repository, container);
	container->unregister(container);
	free(container->channel);
	free(container);
}

void net_repository_foreach(int (*callback)(struct net_container *container, void *ctx), void *ctx)
{
	list_foreach(&repository, (int (*)(void *node, void *ctx))callback, ctx);
}

struct net_object *net_container_get_object(struct net_container *container)
{
	return container->object;
}

const char *net_container_get_channel(struct net_container *container)
{
	return container->channel;
}

int net_container_is_local(struct net_container *container)
{
	return container->local;
}

int net_container_is_connected(struct net_container *container)
{
	return container->is_connected(container);
}

void irc_assert(irc_session_t * session, int irc_ret)
{
	// ret == 0 => success
	if(!irc_ret)
		return;

	int error = irc_errno(session);
	if(error == 0) // ??
		return;

	const char *serror = irc_strerror(error);

	log_fatal("irc error %d : %s", error, serror);
}

char *irc_channel(const char *channel)
{
	static char schannel[300]; // thread unsafe
	sprintf(schannel, "#%s", channel);
	return schannel;
}

int irc_id_equals(const char *nick1, const char *nick2)
{
	// compare before |
	char *ptr1 = strchr(nick1, '|');
	size_t len1 = ptr1 ? ptr1 - nick1 : strlen(nick1);

	char *ptr2 = strchr(nick2, '|');
	size_t len2 = ptr2 ? ptr2 - nick2 : strlen(nick2);

	if(len1 != len2)
		return 0;
	return !strncmp(nick1, nick2, len1);
}

void irc_error(irc_session_t *session, const char *target, const char *format, ...)
{
	static char buffer[512];

	va_list ap;
	va_start(ap, format);
	vsprintf(buffer, format, ap);
	va_end(ap);

	irc_cmd_notice(session, target, buffer);
}

// -------------------------------- local --------------------------------

void local_register(struct net_container *container)
{
	struct internal_local_container *data;
	malloc_nofail(data);
	container->data = data;

	container->unregister = local_unregister;
	container->is_connected = local_is_connected;

	data->connected = 0;
	data->nick = NULL;
	list_init(&(data->attribute_changed_handlers));

	log_assert((data->session = irc_create_session(&local_callbacks)));
	irc_set_ctx(data->session, container);
	irc_option_set(data->session, LIBIRC_OPTION_STRIPNICKS);

	const char *id = net_object_get_id(container->object);

	struct net_object *object = container->object;
	struct net_class *class = net_object_get_class(object);
	net_class_enum_members(class, local_register_member, container);

	// init nick
	data->nick = NULL;
	local_make_nick(container);

	irc_assert(data->session, irc_connect(data->session, server_address, server_port, 0, data->nick, id, id));
	data->loop_handle = loop_register_listener(local_callback_select_add, local_callback_select_process, container);
}

void local_unregister(struct net_container *container)
{
	struct internal_local_container *data = container->data;

	loop_unregister(data->loop_handle);
	irc_destroy_session(data->session);
	free(data->nick);
	list_clear(&(data->attribute_changed_handlers), local_unregister_member, container);
	free(data);
}

void local_callback_select_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	struct net_container *container = ctx;
	struct internal_local_container *data = container->data;

	irc_assert(data->session, irc_add_select_descriptors(data->session, readfds, writefds, nfds));
}

void local_callback_select_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	struct net_container *container = ctx;
	struct internal_local_container *data = container->data;

	irc_assert(data->session, irc_process_select_descriptors(data->session, readfds, writefds));
}

int local_is_connected(struct net_container *container)
{
	struct internal_local_container *data = container->data;
	return data->connected;
}

int local_register_member(struct net_member *member, void *ctx)
{
	struct net_container *container = ctx;
	struct internal_local_container *data = container->data;
	struct net_object *object = container->object;

	// on ne s'occupe que des attributs
	if(net_member_get_type(member) != NET_MEMBER_ATTRIBUTE)
		return 1;

	struct attribute_changed_handler *handler;
	malloc_nofail(handler);
	handler->name = net_member_get_name(member);
	handler->handler = net_object_attribute_listener_add(object, handler->name, local_attribute_changed, container);
	list_add(&(data->attribute_changed_handlers), handler);

	return 1;
}

void local_unregister_member(void *node, void *ctx)
{
	struct net_container *container = ctx;
	struct net_object *object = container->object;
	struct attribute_changed_handler *handler = node;

	net_object_attribute_listener_remove(object, handler->name, handler->handler);
}

void local_attribute_changed(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[])
{
	struct net_container *container = ctx;

	local_make_nick(container);
}

void local_event_connect(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	struct net_container *container = irc_get_ctx(session);

	irc_assert(session, irc_cmd_join(session, irc_channel(container->channel), NULL));
}

void local_event_numeric(irc_session_t * session, unsigned int event, const char * origin, const char ** params, unsigned int count)
{
	// nothing ?
}

void local_event_message(irc_session_t * session, const char * event, const char * origin, const char ** params, unsigned int count)
{
	struct net_container *container = irc_get_ctx(session);

	char *saveptr;
	char *msg;
	const char *target;
	const char *cmd;
	size_t value_count;
	struct net_value **values;

	// pas de message ?
	if(count < 2)
		return;

	strdup_nofail(msg, params[1]);

	if(params[0][0] == '#')
	{
		target = strtok_r(msg, " ", &saveptr);
		cmd = strtok_r(NULL, " ", &saveptr);
	}
	else
	{
		target = params[0];
		cmd = strtok_r(msg, " ", &saveptr);
	}

	if(!irc_id_equals(target, net_object_get_id(container->object)))
	{
		free(msg);
		return;
	}

	struct net_class *class = net_object_get_class(container->object);
	struct net_member *action = net_class_get_member_by_name(class, cmd);
	if(!action || net_member_get_type(action) != NET_MEMBER_ACTION)
	{
		log_warning("action '%s' does not exists", cmd);
		irc_error(session, origin, "action '%s' does not exists", cmd);
		free(msg);
		return;
	}

	value_count = net_member_get_arguments_count(action);
	malloc_array_nofail(values, value_count + 1);
	memset(values, 0, sizeof(*values) * (value_count + 1));

	struct local_call_param_data data;
	data.values = values;
	data.saveptr = saveptr;
	net_member_enum_arguments(action, local_call_param, &data);

	// check params
	for(size_t i=0; i<value_count; i++)
	{
		if(!values[i])
		{
			log_warning("action '%s' : invalid arguments count or type", cmd);
			irc_error(session, origin, "action '%s' : invalid arguments count or type", cmd);

			// arg null = read error
			for(size_t j=0; j<value_count; j++)
				free(values[j]);
			free(values);
			free(msg);
			return;
		}
	}

	net_object_action_execute_array(container->object, cmd, values);

	for(size_t i=0; i<value_count; i++)
		free(values[i]);
	free(values);
	free(msg);
}

int local_call_param(struct net_type *type, void *ctx)
{
	struct local_call_param_data *data = ctx;
	char *tail;
	int min;
	int max;

	char *svalue = strtok_r(NULL, " ", &(data->saveptr));
	if(!svalue)
		return 0;

	struct net_value *value;
	malloc_nofail(value);

	switch(net_type_get_type(type))
	{
	case NET_TYPE_ENUM:
		if(!net_type_enum_exists(type, svalue))
		{
			// bad enum value
			free(value);
			return 0;
		}

		strdup_nofail(value->enum_value, svalue);
		break;

	case NET_TYPE_RANGE:
		value->range_value = strtol(svalue, &tail, 10);
		if(*tail)
		{
			// parse error
			free(value);
			return 0;
		}

		min = net_type_get_range_min(type);
		max = net_type_get_range_max(type);
		if(value->range_value < min || value->range_value > max)
		{
			// range error
			free(value);
			return 0;
		}

		break;
	}

	*((data->values)++) = value;
	return 1;
}

void local_make_nick(struct net_container *container)
{
	struct internal_local_container *data = container->data;
	irc_session_t *session = data->session;
	static char local_nick[300];
	struct local_make_nick_data enum_data;
	enum_data.local_nick = local_nick;
	enum_data.object = container->object;

	strcpy(local_nick, net_object_get_id(container->object));
	struct net_class *class = net_object_get_class(container->object);
	net_class_enum_members(class, local_make_nick_item, &enum_data);

	free(data->nick);
	strdup_nofail(data->nick, local_nick);

	if(irc_is_connected(session))
		irc_cmd_nick(session, data->nick);
}

int local_make_nick_item(struct net_member *member, void *ctx)
{
	struct local_make_nick_data *data = ctx;
	char *local_nick = data->local_nick;
	char buffer[20];

	int member_type = net_member_get_type(member);
	if(member_type != NET_MEMBER_ATTRIBUTE)
		return 1;

	const struct net_value *value = net_object_attribute_get(data->object, net_member_get_name(member));

	strcat(local_nick, "|");

	if(!value)
	{
		strcat(local_nick, "null");
		return 1;
	}

	int type = net_type_get_type(net_member_get_argument(member));
	switch(type)
	{
	case NET_TYPE_ENUM:
		strcat(local_nick, value->enum_value);
		break;

	case NET_TYPE_RANGE:
		sprintf(buffer, "%d", value->range_value);
		strcat(local_nick, buffer);
		break;
	}

	return 1;
}

// -------------------------------- remote --------------------------------

void remote_register(struct net_container *container)
{
	// TODO
	log_fatal("%s", "not implemented");
}
