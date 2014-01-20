/*
 * comp_gpioout.c
 *
 *  Created on: Jan 20, 2014
 *      Author: vincent
 */

#include <stddef.h>
#include <libconfig.h>

#include "components.h"
#include "net.h"
#include "tools.h"
#include "logger.h"
#include "config.h"
#include "gpio.h"

struct component
{
	struct net_object *object;
	struct net_container *container;
	struct gpio *gpio;
};

static void *creator(const char *id, config_setting_t *config);
static void destructor(void *handle);

static struct component_type type =
{
	.name = "gpioout",
	.creator = creator,
	.destructor = destructor
};

static struct net_class *net_class;
static struct net_type *net_type_value;

static void action_callback(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[]);

void comp_internal_gpioout_init()
{
	net_type_value = net_type_create_enum("off", "on", NULL);
	net_class = net_class_create();
	net_class_create_attribute(net_class, "value", net_type_value);
	net_class_create_action(net_class, "setvalue", net_type_value, NULL);

	component_register(&type);
}

void comp_internal_gpioout_terminate()
{
	net_class_destroy(net_class);
	net_type_destroy(net_type_value);
}

void *creator(const char *id, config_setting_t *config)
{
	struct component *comp;
	malloc_nofail(comp);

	int pin = conf_get_int(config, "pin");
	log_assert((comp->gpio = gpio_open(pin, id, GPIO_TYPE_IO)));
	gpio_io_set_direction(comp->gpio, out);
	gpio_io_set_value(comp->gpio, 0);

	log_assert((comp->object = net_object_create(net_class, id)));
	struct net_value value = { .enum_value = "off" };
	net_object_action_set_handler(comp->object, "setvalue", action_callback, comp);
	net_object_attribute_change(comp->object, "value", value);
	log_assert((comp->container = net_repository_register(comp->object, NET_CHANNEL_HARDWARE, 1)));
	return comp;
}

void destructor(void *handle)
{
	struct component *comp = handle;
	net_repository_unregister(comp->container);
	net_object_destroy(comp->object);
	gpio_io_set_value(comp->gpio, 0);
	gpio_close(comp->gpio);
	free(comp);
}

void action_callback(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[])
{
	struct component *comp = ctx;
	struct net_value value = *args[0];
	const char *sval = value.enum_value;

	if(!strcmp(sval, "off"))
		gpio_io_set_value(comp->gpio, 0);
	else if(!strcmp(sval, "on"))
		gpio_io_set_value(comp->gpio, 0);
	else
		log_fatal("unknown value : %s", sval);

	net_object_attribute_change(object, "value", value);
}
