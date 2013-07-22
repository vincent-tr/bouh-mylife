/*
 * gpiodriver_lock.h
 *
 *  Created on: 22 juil. 2013
 *      Author: pumbawoman
 */

#ifndef GPIODRIVER_LOCK_H_
#define GPIODRIVER_LOCK_H_

#define GPIO_TYPE_LOCK 3

#ifdef MOD_GPIODRIVER

extern void gpio_lock_init();
extern void gpio_lock_terminate();

#else // MOD_GPIODRIVER

#endif // MOD_GPIODRIVER

#endif /* GPIODRIVER_LOCK_H_ */
