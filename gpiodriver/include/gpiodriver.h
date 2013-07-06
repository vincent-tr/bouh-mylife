/*
 * gpio.h
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#ifndef GPIO_H_
#define GPIO_H_

struct gpio_handler;

#ifdef MOD_GPIODRIVER

extern void gpio_init();
extern void gpio_terminate();

extern struct gpio_handler *gpio_open();
extern struct gpio_handler *gpio_close();

#else // MOD_GPIODRIVER

#endif // MOD_GPIODRIVER

#endif /* GPIO_H_ */
