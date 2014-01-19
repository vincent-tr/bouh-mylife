/*
 * gpio_internal.h
 *
 *  Created on: Jan 19, 2014
 *      Author: vincent
 */

#ifndef GPIO_INTERNAL_H_
#define GPIO_INTERNAL_H_

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

extern void io_init();
extern void io_terminate();
extern void pwm_init();
extern void pwm_terminate();
extern void lock_init();
extern void lock_terminate();

extern void register_type(struct driver_type *type);
extern void unregister_type(struct driver_type *type);

extern void enum_opened_gpios(int (*callback)(struct gpio *gpio, void *ctx), void *ctx);
extern void enum_all_gpios(int (*callback)(int pin, struct gpio *gpio, void *ctx), void *ctx);

#endif /* GPIO_INTERNAL_H_ */
