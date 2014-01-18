/*
 * gpiodriver_internal.h
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#ifndef GPIODRIVER_INTERNAL_H_
#define GPIODRIVER_INTERNAL_H_

struct driver_type
{
	struct list_node node; // reserved

	int type;
	int (*open)(struct gpio *gpio, va_list args);
	void (*close)(struct gpio *gpio);
	int (*ctl)(struct gpio *gpio, int ctl, va_list args);

	size_t refcount; // reserved
};

struct gpio
{
	struct list_node node; // reserved

	int pin;
	int gpio;
	char *usage;
	struct driver_type *type;
	void *type_data;
};

extern void register_type(struct driver_type *type);
extern void unregister_type(struct driver_type *type);

extern void enum_opened_gpios(int (*callback)(struct gpio *gpio, void *ctx), void *ctx);
extern void enum_all_gpios(int (*callback)(int pin, struct gpio *gpio, void *ctx), void *ctx);

#endif /* GPIODRIVER_INTERNAL_H_ */
