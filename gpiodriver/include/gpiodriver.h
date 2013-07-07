/*
 * gpio.h
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#ifndef GPIO_H_
#define GPIO_H_

struct gpio;

#define GPIO_PIN_03 (3)
#define GPIO_PIN_05 (5)
#define GPIO_PIN_07 (7)
#define GPIO_PIN_08 (8)
#define GPIO_PIN_10 (10)
#define GPIO_PIN_11 (11)
#define GPIO_PIN_12 (12)
#define GPIO_PIN_13 (13)
#define GPIO_PIN_15 (15)
#define GPIO_PIN_16 (16)
#define GPIO_PIN_18 (18)
#define GPIO_PIN_19 (19)
#define GPIO_PIN_21 (21)
#define GPIO_PIN_22 (22)
#define GPIO_PIN_23 (23)
#define GPIO_PIN_24 (24)
#define GPIO_PIN_26 (26)

#define GPIO_CTL_BASE					(0x000)
#define GPIO_CTL_GET_PIN_NUMBER			(GPIO_CTL_BASE + 1)
#define GPIO_CTL_GET_GPIO_NUMBER		(GPIO_CTL_BASE + 2)
#define GPIO_CTL_GET_TYPE				(GPIO_CTL_BASE + 3)
#define GPIO_CTL_GET_USAGE				(GPIO_CTL_BASE + 4)
// other values are type dependant

#ifdef MOD_GPIODRIVER

extern void gpio_init();
extern void gpio_terminate();

extern struct gpio *gpio_open(int pin, const char *usage, int type, ...);
extern void gpio_close(struct gpio *gpio);
extern int gpio_ctl(struct gpio *gpio, int ctl, ...);

#include "gpiodriver_internal.h"

#else // MOD_GPIODRIVER

#include "gpiodriver_api.h"

#define gpio_open(pin, usage, type, ...) (gpiodriver_api->gpio_open(pin, usage, type, ##__VA_ARGS__))
#define gpio_close(gpio) (gpiodriver_api->gpio_close(gpio))
#define gpio_ctl(gpio, ctl, ...) (gpiodriver_api->gpio_ctl(gpio, ctl, ##__VA_ARGS__))

#endif // MOD_GPIODRIVER

#endif /* GPIO_H_ */
