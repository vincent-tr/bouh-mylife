/*
 * gpiodriver_api.h
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#ifndef GPIODRIVER_API_H_
#define GPIODRIVER_API_H_

#include "gpiodriver.h"
#include "gpiodriver_io.h"
#include "gpiodriver_pwm.h"

struct gpiodriver_api
{
	struct gpio *(*gpio_open)(int pin, const char *usage, int type, ...);
	void (*gpio_close)(struct gpio *gpio);
	int (*gpio_ctl)(struct gpio *gpio, int ctl, ...);
};

#ifndef CORE

// les modules qui utilisent gpiodriver_api doivent déclarer une variable nommée gpiodriver_api où ils assignent l'api dans init
extern struct gpiodriver_api *gpiodriver_api;

#endif


#endif /* GPIODRIVER_API_H_ */
