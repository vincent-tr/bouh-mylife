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
//#define GPIO_CTL_ TODO

#ifdef MOD_GPIODRIVER

extern void gpio_io_init();
extern void gpio_io_terminate();

#else // MOD_GPIODRIVER

#endif // MOD_GPIODRIVER

#endif /* GPIODRIVER_IO_H_ */
