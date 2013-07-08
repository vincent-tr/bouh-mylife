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

/* REV 2
P3 GPIO2
P5 GPIO3
P7 GPIO4
P8 GPIO14
P10 GPIO15
P11 GPIO17
P12 GPIO18
P13 GPIO27
P15 GPIO22
P16 GPIO23
P18 GPIO24
P19 GPIO10
P21 GPIO9
P22 GPIO25
P23 GPIO11
P24 GPIO8
P26 GPIO7
 */

static int gpio_from_pin[] =
{
	/* 00 */ -1,
	/* 01 */ -1,
	/* 02 */ -1,
	/* 03 */ 2,
	/* 04 */ -1,
	/* 05 */ 3,
	/* 06 */ -1,
	/* 07 */ 4,
	/* 08 */ 14,
	/* 09 */ -1,
	/* 10 */ 15,
	/* 11 */ 17,
	/* 12 */ 18,
	/* 13 */ 27,
	/* 14 */ -1,
	/* 15 */ 22,
	/* 16 */ 23,
	/* 17 */ -1,
	/* 18 */ 24,
	/* 19 */ 10,
	/* 20 */ -1,
	/* 21 */ 9,
	/* 22 */ 25,
	/* 23 */ 11,
	/* 24 */ 8,
	/* 25 */ -1,
	/* 26 */ 7,
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
		return error_failed_ptr(ERROR_CORE_INVAL); // no usage specified

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
		return error_failed_ptr(ERROR_CORE_INVAL); // invalid pin number

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
		return error_failed_ptr(ERROR_CORE_INVAL); // type not found

	struct gpio *gpio;
	malloc_nofail(gpio);
	strdup_nofail(gpio->usage, usage);
	gpio->type = data.result;
	gpio->gpio = gpio_from_pin[gpio->pin];

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

	error_success();
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
	int ret;

	switch(ctl)
	{
	case GPIO_CTL_GET_PIN_NUMBER:
		{
			int *pin = va_arg(args, int *);
			*pin = gpio->pin;
			ret = error_success();
		}
		break;

	case GPIO_CTL_GET_GPIO_NUMBER:
		{
			int *gpionb = va_arg(args, int *);
			*gpionb = gpio->gpio;
			ret = error_success();
		}
		break;

	case GPIO_CTL_GET_TYPE:
		{
			int *type = va_arg(args, int *);
			*type = gpio->type->type;
			ret = error_success();
		}
		break;

	case GPIO_CTL_GET_USAGE:
		{
			const char **usage = va_arg(args, const char **);
			*usage = gpio->usage;
			ret = error_success();
		}
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

void enum_gpios(int (*callback)(struct gpio *gpio, void *ctx), void *ctx)
{
	list_foreach(&gpios, (int (*)(void *, void *))callback, ctx);
}
