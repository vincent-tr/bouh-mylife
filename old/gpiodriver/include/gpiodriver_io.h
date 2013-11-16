/*
 * gpiodriver_io.h
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#ifndef GPIODRIVER_IO_H_
#define GPIODRIVER_IO_H_

#define GPIO_TYPE_IO 1

#define GPIO_CTL_IO_BASE					(0x100)

#define GPIO_CTL_GET_VALUE					(GPIO_CTL_IO_BASE + 1)
#define GPIO_CTL_SET_VALUE					(GPIO_CTL_IO_BASE + 2)
#define GPIO_CTL_GET_DIRECTION				(GPIO_CTL_IO_BASE + 3)
#define GPIO_CTL_SET_DIRECTION				(GPIO_CTL_IO_BASE + 4)
#define GPIO_CTL_SET_CHANGE_CALLBACK		(GPIO_CTL_IO_BASE + 5)

enum gpio_direction
{
	in,
	out
};

typedef void (*gpio_change_callback)(struct gpio *gpio, int value);

#ifdef MOD_GPIODRIVER

extern void gpio_io_init();
extern void gpio_io_terminate();

#else // MOD_GPIODRIVER

#endif // MOD_GPIODRIVER

#endif /* GPIODRIVER_IO_H_ */
