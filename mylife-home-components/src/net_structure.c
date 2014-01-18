/*
 * net_structure.c
 *
 *  Created on: Jan 11, 2014
 *      Author: vincent
 */

#include <stdlib.h>
#include <stddef.h>
#include <stdarg.h>

#include "net.h"
#include "tools.h"
#include "list.h"

struct net_type
{
	int type;
	union
	{
		struct
		{
			int min;
			int max;
		} range_values;

		struct
		{
			char *names[1]; // NULL terminated
		} enum_values;
	};
};

struct net_type_node
{
	struct list_node node;
	struct net_type *type;
};

struct net_member
{
	struct list_node node;

	int type;
	char *name;
	int order;

	union
	{
		struct net_type *argument;
		struct net_type *argument_list[1];
	};
};

struct net_class
{
	struct list members;
};

struct net_type_enum_exists_data
{
	const char *value;
	int found;
};

struct members_count_data
{
	int type;
	size_t count;
};

struct member_by_name_data
{
	const char *name;
	struct net_member *res;
};

static int net_type_enum_exists_item(const char *enum_value, void *ctx);
static void free_member(void *node, void *ctx);
static void check_member_name(struct net_class *class, const char *name);
static int check_member_name_item(void *node, void *ctx);
static int members_count_item(struct net_member *member, void *ctx);
static int member_by_name_item(struct net_member *member, void *ctx);

// type automatiquement détruit dans net_class_destroy
struct net_type *net_type_create_enum(const char *first, ...) // NULL terminated
{
	struct net_type *type = NULL;
	// petit gaspillage mais bon ..
	size_t size = sizeof(*type);
	const char *ptr = first;
	char **walker;
	va_list ap;

	va_start(ap, first);

	realloc_nofail(type, size);
	type->type = NET_TYPE_ENUM;
	walker = type->enum_values.names;

	while(ptr)
	{
		size += sizeof(ptr);
		realloc_nofail(type, size);
		strdup_nofail(*walker, ptr);
		++walker;

		ptr = va_arg(ap, const char *);
	}
	*walker = NULL;

	va_end(ap);
	return type;
}

struct net_type *net_type_create_range(int min, int max)
{
	struct net_type *type;
	malloc_nofail(type);
	type->type = NET_TYPE_RANGE;
	type->range_values.min = min;
	type->range_values.max = max;
	return type;
}

void net_type_destroy(struct net_type *type)
{
	if(type->type == NET_TYPE_ENUM)
	{
		for(char **walker = type->enum_values.names; *walker; ++walker)
			free(*walker);
	}
	free(type);
}

int net_type_get_type(struct net_type *type)
{
	return type->type;
}

unsigned int net_type_get_range_min(struct net_type *type)
{
	return type->range_values.min;
}

unsigned int net_type_get_range_max(struct net_type *type)
{
	return type->range_values.max;
}

void net_type_enum_foreach(struct net_type *type, int (*callback)(const char *enum_value, void *ctx), void *ctx)
{
	for(char **walker = type->enum_values.names; *walker; ++walker)
	{
		if(!callback(*walker, ctx))
			return;
	}
}

int net_type_enum_exists(struct net_type *type, const char *value)
{
	struct net_type_enum_exists_data data;
	data.found = 0;
	data.value = value;
	net_type_enum_foreach(type, net_type_enum_exists_item, &data);
	return data.found;
}

int net_type_enum_exists_item(const char *enum_value, void *ctx)
{
	struct net_type_enum_exists_data *data = ctx;
	if(!strcmp(data->value, enum_value))
	{
		data->found = 1;
		return 0;
	}

	return 1;
}

struct net_class *net_class_create()
{
	struct net_class *class;
	malloc_nofail(class);
	list_init(&(class->members));
	return class;
}

void net_class_destroy(struct net_class *class)
{
	list_clear(&(class->members), free_member, NULL);
}

void free_member(void *node, void *ctx)
{
	struct net_member *member = node;
	free(member);
}

void net_class_enum_members(struct net_class *class, int (*callback)(struct net_member *member, void *ctx), void *ctx)
{
	list_foreach(&(class->members), (int (*)(void *, void *))callback, ctx);
}

struct net_member *net_class_get_member_by_name(struct net_class *class, const char *name)
{
	struct member_by_name_data data;
	data.name = name;
	data.res = NULL;
	net_class_enum_members(class, member_by_name_item, &data);
	return data.res;
}

int member_by_name_item(struct net_member *member, void *ctx)
{
	struct member_by_name_data *data = ctx;

	if(!strcmp(member->name, data->name))
	{
		data->res = member;
		return 0;
	}

	return 1;
}

size_t net_class_get_members_count(struct net_class *class)
{
	return list_count(&(class->members));
}

size_t net_class_get_attributes_count(struct net_class *class)
{
	struct members_count_data data;
	data.type = NET_MEMBER_ATTRIBUTE;
	data.count = 0;

	net_class_enum_members(class, members_count_item, &data);

	return data.count;
}

size_t net_class_get_actions_count(struct net_class *class)
{
	struct members_count_data data;
	data.type = NET_MEMBER_ACTION;
	data.count = 0;

	net_class_enum_members(class, members_count_item, &data);

	return data.count;
}

int members_count_item(struct net_member *member, void *ctx)
{
	struct members_count_data *data = ctx;

	if(member->type == data->type)
		++(data->count);

	return 1;
}

// membre automatiquement détruit dans net_class_destroy
struct net_member *net_class_create_attribute(struct net_class *class, const char *name, struct net_type *type)
{
	struct net_member *member;

	check_member_name(class, name);

	malloc_nofail(member);
	member->type = NET_MEMBER_ATTRIBUTE;
	strdup_nofail(member->name, name);
	member->order = list_count(&(class->members));

	member->argument = type;

	list_add(&(class->members), member);

	return member;
}

struct net_member *net_class_create_action(struct net_class *class, const char *name, ...) // liste de types, terminer par NULL
{
	struct net_member *member = NULL;
	// petit gaspillage mais bon ..
	size_t size = sizeof(*member);
	struct net_type *type;
	struct net_type **walker;
	va_list ap;

	check_member_name(class, name);

	va_start(ap, name);

	realloc_nofail(member, size);
	member->type = NET_MEMBER_ACTION;
	strdup_nofail(member->name, name);
	member->order = list_count(&(class->members));
	walker = member->argument_list;

	while((type = va_arg(ap, struct net_type *)))
	{
		size += sizeof(type);
		member = realloc(member, size);
		*(walker++) = type;
	}
	*walker = NULL;

	va_end(ap);

	list_add(&(class->members), member);

	return member;
}

void check_member_name(struct net_class *class, const char *name)
{
	list_foreach(&(class->members), check_member_name_item, (char *)name);
}

int check_member_name_item(void *node, void *ctx)
{
	struct net_member *member = node;
	const char *name = ctx;
	log_assert(strcmp(member->name, name));
	return 1;
}

int net_member_get_type(struct net_member *member)
{
	return member->type;
}

const char *net_member_get_name(struct net_member *member)
{
	return member->name;
}

unsigned int net_member_get_order(struct net_member *member)
{
	return member->order;
}

struct net_type *net_member_get_argument(struct net_member *member)
{
	return member->argument;
}

void net_member_enum_arguments(struct net_member *member, int (*callback)(struct net_type *type, void *ctx), void *ctx)
{
	for(struct net_type **walker = member->argument_list; *walker; ++walker)
	{
		if(!callback(*walker, ctx))
			return;
	}
}

size_t net_member_get_arguments_count(struct net_member *member)
{
	size_t count = 0;
	for(struct net_type **walker = member->argument_list; *walker; ++walker, ++count);
	return count;
}
