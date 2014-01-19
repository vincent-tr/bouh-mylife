/*
 * comp_test.c
 *
 *  Created on: Jan 16, 2014
 *      Author: vincent
 */

#include <stddef.h>
#include <libconfig.h>

#include "components.h"
#include "net.h"
#include "tools.h"
#include "logger.h"

static void *creator(const char *id, config_setting_t *config);
static void destructor(void *handle);

static struct component_type type =
{
	.name = "test",
	.creator = creator,
	.destructor = destructor
};

static struct net_class *test_class;
static struct net_type *attr_type;

static void action_callback(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[]);

void comp_internal_test_init()
{
	attr_type = net_type_create_range(0, 100);
	test_class = net_class_create();
	net_class_create_attribute(test_class, "attribute", attr_type);
	net_class_create_action(test_class, "action", attr_type, NULL);

	component_register(&type);
}

void comp_internal_test_terminate()
{
	net_class_destroy(test_class);
	net_type_destroy(attr_type);
}

void *creator(const char *id, config_setting_t *config)
{
	struct net_object *object = net_object_create(test_class, id);
	struct net_value value = { .range_value = 0 };
	net_object_action_set_handler(object, "action", action_callback, NULL);
	net_object_attribute_change(object, "attribute", value);
	return net_repository_register(object, NET_CHANNEL_HARDWARE, 1);
}

void destructor(void *handle)
{
	struct net_container *container = handle;
	struct net_object *object = net_container_get_object(container);
	net_repository_unregister(container);
	net_object_destroy(object);
}

void action_callback(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[])
{
	log_debug("action %d", (*args)->range_value);
	net_object_attribute_change(object, "attribute", *args[0]);
}
