/*
 * rcswitch.c
 *
 *  Created on: Apr 17, 2014
 *      Author: vincent
 */

#include <stddef.h>
#include <stdio.h>
#include <stdarg.h>
#include <limits.h>
#include <string.h>
#include <unistd.h>
#include <libconfig.h>

#include "components.h"
#include "net.h"
#include "tools.h"
#include "logger.h"
#include "config.h"
#include "gpio.h"
#include "gpio_internal.h"

// always same gpio
static struct gpio *gpio;
static int gpio_usage;
static const char *command;

struct component
{
	struct net_object *object;
	struct net_container *container;

	const char *system_code;
	const char *unit_code;
};

static void *creator(const char *id, config_setting_t *config);
static void destructor(void *handle);

static void action_callback(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[]);

static void change_value(struct component *comp, int value);

static struct component_type type =
{
	.name = "rcswitch",
	.creator = creator,
	.destructor = destructor
};

static struct net_class *net_class;
static struct net_type *net_type_value;

void comp_internal_rcswitch_init()
{
	net_type_value = net_type_create_enum("off", "on", NULL);
	net_class = net_class_create();
	net_class_create_attribute(net_class, "value", net_type_value);
	net_class_create_action(net_class, "setvalue", net_type_value, NULL);

	component_register(&type);
}

void comp_internal_rcswitch_terminate()
{
	net_class_destroy(net_class);
	net_type_destroy(net_type_value);
}

void *creator(const char *id, config_setting_t *config)
{
	struct component *comp;
	malloc_nofail(comp);

	if(!gpio)
	{
		int pin = conf_get_int(config, "pin");
		log_assert((gpio = gpio_open(pin, "rcswitch", GPIO_TYPE_LOCK)));
		log_assert((command = conf_get_string(config, "command")));
	}
	++gpio_usage;

	log_assert((comp->system_code = conf_get_string(config, "system_code")));
	log_assert((comp->unit_code = conf_get_string(config, "unit_code")));

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

	--gpio_usage;
	if(!gpio_usage)
	{
		gpio_close(gpio);
		gpio = NULL;
		command = NULL;
	}

	free(comp);
}

void action_callback(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[])
{
	struct component *comp = ctx;
	struct net_value value = *args[0];
	const char *sval = value.enum_value;

	if(!strcmp(sval, "off"))
		change_value(comp, 0);
	else if(!strcmp(sval, "on"))
		change_value(comp, 1);
	else
		log_fatal("unknown value : %s", sval);

	net_object_attribute_change(object, "value", value);
}

void change_value(struct component *comp, int value)
{
	int pid = fork();
	log_assert(pid != -1);
	if(pid > 0)
		return;

	// in child here
	char sgpio[5];
	sprintf(sgpio, "%i", gpio->gpio);
	execlp(command, command, sgpio, comp->system_code, comp->unit_code, value ? "1" : "0", (char *)NULL);
}
