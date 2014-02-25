/*
 * comp_gpioin.c
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
	.name = "gpioin",
	.creator = creator,
	.destructor = destructor
};

static void change_callback(struct gpio *gpio, int value, void *ctx);

static struct net_class *net_class;
static struct net_type *net_type_value;

void comp_internal_gpioin_init()
{
	net_type_value = net_type_create_enum("off", "on", NULL);
	net_class = net_class_create();
	net_class_create_attribute(net_class, "value", net_type_value);

	component_register(&type);
}

void comp_internal_gpioin_terminate()
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
	gpio_io_set_direction(comp->gpio, in);
	gpio_io_set_change_callback(comp->gpio, change_callback, comp);

	log_assert((comp->object = net_object_create(net_class, id)));
	int ivalue = gpio_io_get_value(comp->gpio);
	change_callback(comp->gpio, ivalue, comp);
	log_assert((comp->container = net_repository_register(comp->object, NET_CHANNEL_HARDWARE, 1)));
	return comp;
}

void destructor(void *handle)
{
	struct component *comp = handle;
	net_repository_unregister(comp->container);
	net_object_destroy(comp->object);
	gpio_close(comp->gpio);
	free(comp);
}

void change_callback(struct gpio *gpio, int value, void *ctx)
{
	struct component *comp = ctx;
	struct net_value net_value = { .enum_value = value ? "on" : "off" };
	net_object_attribute_change(comp->object, "value", net_value);
}
