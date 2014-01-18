/*
 * net_object.c
 *
 *  Created on: Jan 11, 2014
 *      Author: vincent
 */

#include <stddef.h>
#include <stdarg.h>

#include "net.h"
#include "tools.h"
#include "list.h"

struct internal_callback
{
	struct list_node node;
	net_callback callback;
	void *ctx;
};

struct internal_member
{
	struct list_node node;

	struct net_member *def;

	union
	{
		struct
		{
			struct internal_callback handler;
		} action;

		struct
		{
			struct net_value *value;
			struct list handlers;
		} attribute;
	};
};

struct net_object
{
	struct net_class *class;
	char *id;
	struct list members;
};

struct lookup_member_data
{
	const char *name;
	struct internal_member *result;
};

struct run_handler_data
{
	struct net_object *object;
	struct net_member *member;
	struct net_value **args;
};

static int create_member(struct net_member *member, void *ctx);
static void free_member(void *node, void *ctx);
static void free_value(struct internal_member *member);
static struct net_value *clone_value(struct net_value value, int type);
static void free_handler(void *node, void *ctx);
static struct internal_member *lookup_member(struct net_object *object, const char *name);
static int lookup_member_item(void *node, void *ctx);
static int run_handler(void *node, void *ctx);

struct net_object *net_object_create(struct net_class *class, const char *id)
{
	struct net_object *object;
	malloc_nofail(object);

	object->class = class;
	strdup_nofail(object->id, id);
	list_init(&(object->members));
	net_class_enum_members(class, create_member, object);

	return object;
}

void net_object_destroy(struct net_object *object)
{
	list_clear(&(object->members), free_member, NULL);
	free(object->id);
	free(object);
}

struct net_class *net_object_get_class(struct net_object *object)
{
	return object->class;
}

const char *net_object_get_id(struct net_object *object)
{
	return object->id;
}

int create_member(struct net_member *member, void *ctx)
{
	struct net_object *object = ctx;
	struct internal_member *im;

	malloc_nofail(im);
	list_add(&(object->members), im);

	im->def = member;

	switch(net_member_get_type(im->def))
	{
	case NET_MEMBER_ACTION:
		meminit(&(im->action.handler));
		break;

	case NET_MEMBER_ATTRIBUTE:
		list_init(&(im->attribute.handlers));
		im->attribute.value = NULL;
		break;
	}

	return 1;
}

void free_member(void *node, void *ctx)
{
	struct internal_member *member = node;

	switch(net_member_get_type(member->def))
	{
	case NET_MEMBER_ACTION:
		break;

	case NET_MEMBER_ATTRIBUTE:
		list_clear(&(member->attribute.handlers), free_handler, NULL);
		free_value(member);
		break;
	}

	free(member);
}

void free_value(struct internal_member *member)
{
	struct net_value *value = member->attribute.value;
	if(value)
	{
		if(net_member_get_type(member->def) == NET_TYPE_RANGE)
			free(value->enum_value);
		free(value);
	}
}

struct net_value *clone_value(struct net_value value, int type)
{
	struct net_value *ret;
	malloc_nofail(ret);

	switch(type)
	{
	case NET_TYPE_ENUM:
		strdup_nofail(ret->enum_value, value.enum_value);
		break;

	case NET_TYPE_RANGE:
		ret->range_value = value.range_value;
		break;
	}

	return ret;
}

void free_handler(void *node, void *ctx)
{
	struct internal_callback *handler = node;
	free(handler);
}

struct internal_member *lookup_member(struct net_object *object, const char *name)
{
	struct lookup_member_data data;
	data.name = name;
	data.result = NULL;
	list_foreach(&(object->members), lookup_member_item, &data);
	return data.result;
}

int lookup_member_item(void *node, void *ctx)
{
	struct lookup_member_data *data = ctx;
	struct internal_member *member = node;

	if(!strcmp(net_member_get_name(member->def), data->name))
	{
		data->result = member;
		return 0;
	}

	return 1;
}

void net_object_attribute_change(struct net_object *object, const char *name, struct net_value value)
{
	struct internal_member *member;
	struct run_handler_data data;

	log_assert((member = lookup_member(object, name)));

	free_value(member);
	member->attribute.value = clone_value(value, net_type_get_type(net_member_get_argument(member->def)));

	struct net_value *args[] = {
		member->attribute.value,
		NULL
	};

	data.object = object;
	data.member = member->def;
	data.args = args;

	list_foreach(&(member->attribute.handlers), run_handler, &data);
}

int run_handler(void *node, void *ctx)
{
	struct internal_callback *handler = node;
	struct run_handler_data *data = ctx;
	handler->callback(handler->ctx, data->object, data->member, data->args);
	return 1;
}

const struct net_value *net_object_attribute_get(struct net_object *object, const char *name)
{
	struct internal_member *member;

	log_assert((member = lookup_member(object, name)));

	return member->attribute.value;
}

void *net_object_attribute_listener_add(struct net_object *object, const char *name, net_callback callback, void *ctx)
{
	struct internal_callback *handler;
	struct internal_member *member;

	log_assert((member = lookup_member(object, name)));
	log_assert(callback);

	malloc_nofail(handler);
	list_add(&(member->attribute.handlers), handler);
	handler->callback = callback;
	handler->ctx = ctx;

	return handler;
}

void net_object_attribute_listener_remove(struct net_object *object, const char *name, void *handler)
{
	struct internal_callback *ihandler = handler;
	struct internal_member *member;

	log_assert((member = lookup_member(object, name)));
	log_assert(ihandler);

	list_remove(&(member->attribute.handlers), ihandler);
	free(ihandler);
}

void net_object_action_execute(struct net_object *object, const char *name, ...) // struct net_value, NULL terminated
{
	struct internal_member *member;
	struct net_value **args;

	log_assert((member = lookup_member(object, name)));

	size_t len = net_member_get_arguments_count(member->def);
	log_assert((args = malloc((sizeof(*args) * (len+1)) + (sizeof(**args) * len))));
	args[len] = NULL;
	struct net_value *walker = (struct net_value *)(args+(len+1));

	va_list ap;
	va_start(ap, name);
	for(size_t i = 0; i<len; i++)
	{
		*walker = va_arg(ap, struct net_value);
		args[i] = walker;
		++walker;
	}
	va_end(ap);

	net_object_action_execute_array(object, name, args);

	free(args);
}

void net_object_action_execute_array(struct net_object *object, const char *name, struct net_value *args[])
{
	struct internal_member *member;
	struct internal_callback *handler;

	log_assert((member = lookup_member(object, name)));

	handler = &(member->action.handler);
	if(!handler->callback)
	{
		log_warning(
			"object '%s' action '%s' : executed but no callback registered",
			net_object_get_id(object), name);
		return;
	}

	handler->callback(handler->ctx, object, member->def, args);
}

void net_object_action_set_handler(struct net_object *object, const char *name, net_callback callback, void *ctx)
{
	struct internal_member *member;
	struct internal_callback *handler;

	log_assert((member = lookup_member(object, name)));
	handler = &(member->action.handler);
	handler->callback = callback;
	handler->ctx = ctx;
}
