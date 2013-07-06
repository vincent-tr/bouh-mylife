/*
 * gpio.c
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include <stdarg.h>
#include <sys/select.h>

#include "core_api.h"
#include "tools.h"
#include "gpiodriver.h"

struct type_lookup_data
{
	int type;
	struct driver_type *result;
};

static int type_lookup(void *node, void *ctx);

static struct list types;
static struct list gpios;

void gpio_init()
{
	list_init(&types);
	list_init(&gpios);
}

void gpio_terminate()
{
	log_assert(list_is_empty(&gpios));
	log_assert(list_is_empty(&types));
}

struct gpio *gpio_open(int pin, int type, ...)
{
	struct type_lookup_data data;
	data.type = type;
	data.result = NULL;
	list_foreach(&types, type_lookup, &data);
	if(!data.result)
		return NULL;

	struct gpio *gpio;
	malloc_nofail(gpio);
	gpio->type = data.result;

	va_list args;
	va_start(args, type);
	int ret = gpio->type->open(gpio, args);
	va_end(args);

	if(!ret)
	{
		free(gpio);
		return NULL;
	}

	list_add(&gpios, gpio);
	++(gpio->type->refcount);

	return gpio;
}

void gpio_close(struct gpio *gpio)
{
	gpio->type->close(gpio);

	--(gpio->type->refcount);
	list_remove(&gpios, gpio);

	free(gpio);
}

int gpio_ctl(struct gpio *gpio, int ctl, ...)
{
	va_list args;
	va_start(args, ctl);
	int ret = gpio->type->ctl(gpio, ctl, args);
	va_end(args);

	return ret;
}

void register_type(struct driver_type *type)
{
	type->refcount = 0;
	list_add(&types, type);
}

void unregister_type(struct driver_type *type)
{
	log_assert(type->refcount == 0);
	list_remove(&types, type);
}

int type_lookup(void *node, void *ctx)
{
	struct type_lookup_data *data = ctx;
	struct driver_type *type = node;
	if(type->type == data->type)
	{
		data->result = type;
		return 0;
	}
	return 1;
}
