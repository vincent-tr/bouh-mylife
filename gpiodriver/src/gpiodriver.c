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

struct pin_lookup_data
{
	int pin;
	struct gpio*result;
};

static int type_lookup(void *node, void *ctx);
static int pin_lookup(void *node, void *ctx);

static struct list types;
static struct list gpios;

static int pins[] =
{
	GPIO_PIN_03,
	GPIO_PIN_05,
	GPIO_PIN_07,
	GPIO_PIN_08,
	GPIO_PIN_10,
	GPIO_PIN_11,
	GPIO_PIN_12,
	GPIO_PIN_13,
	GPIO_PIN_15,
	GPIO_PIN_16,
	GPIO_PIN_18,
	GPIO_PIN_19,
	GPIO_PIN_21,
	GPIO_PIN_22,
	GPIO_PIN_23,
	GPIO_PIN_24,
	GPIO_PIN_26
};

#define PINS_COUNT 17

static int gpio_from_pin[] =
{
	// TODO
};

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

struct gpio *gpio_open(int pin, const char *usage, int type, ...)
{
	if(!usage)
		return NULL; // no usage specified

	int valid = 0;
	int i;
	for(i=0; i<PINS_COUNT; i++)
	{
		if(pins[i] == pin)
		{
			valid = 1;
			break;
		}
	}
	if(!valid)
		return NULL; // invalid pin number

	struct pin_lookup_data data_pin;
	data_pin.pin = pin;
	data_pin.result = NULL;
	list_foreach(&types, pin_lookup, &data_pin);
	if(data_pin.result)
		return NULL; // already in use

	struct type_lookup_data data;
	data.type = type;
	data.result = NULL;
	list_foreach(&types, type_lookup, &data);
	if(!data.result)
		return NULL; // type not found

	struct gpio *gpio;
	malloc_nofail(gpio);
	strdup_nofail(gpio->usage, usage);
	gpio->type = data.result;

	va_list args;
	va_start(args, type);
	int ret = gpio->type->open(gpio, args);
	va_end(args);

	if(!ret)
	{
		free(gpio->usage);
		free(gpio);
		return NULL; // type open failed
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

	free(gpio->usage);
	free(gpio);
}

int gpio_ctl(struct gpio *gpio, int ctl, ...)
{
	va_list args;
	va_start(args, ctl);
	int ret = 0;

	switch(ctl)
	{
	case GPIO_CTL_GET_PIN_NUMBER:
		// TODO : base functions
		break;

	case GPIO_CTL_GET_GPIO_NUMBER:
		// TODO : base functions
		break;

	case GPIO_CTL_GET_TYPE:
		// TODO : base functions
		break;

	default:
		// type dependant functions
		ret = gpio->type->ctl(gpio, ctl, args);
	}

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

int pin_lookup(void *node, void *ctx)
{
	struct pin_lookup_data *data = ctx;
	struct gpio *gpio = node;
	if(gpio->pin == data->pin)
	{
		data->result = gpio;
		return 0;
	}
	return 1;
}
