/*
 * gpiodriver_pwm.h
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#ifndef GPIODRIVER_PWM_H_
#define GPIODRIVER_PWM_H_

#ifdef MOD_GPIODRIVER

extern void gpio_pwm_init();
extern void gpio_pwm_terminate();

#else // MOD_GPIODRIVER

#define GPIO_TYPE_PWM 2

#define GPIO_CTL_PWM_BASE					(0x200)
//#define GPIO_CTL_ TODO

#endif // MOD_GPIODRIVER

#endif /* GPIODRIVER_PWM_H_ */
