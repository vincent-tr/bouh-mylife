/*
 * gpiodriver_pwm.h
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#ifndef GPIODRIVER_PWM_H_
#define GPIODRIVER_PWM_H_

#define GPIO_TYPE_PWM 2

#define GPIO_CTL_PWM_BASE					(0x200)
#define GPIO_CTL_GET_PERIOD					(GPIO_CTL_PWM_BASE + 1)
#define GPIO_CTL_SET_PERIOD					(GPIO_CTL_PWM_BASE + 2)
#define GPIO_CTL_GET_PULSE					(GPIO_CTL_PWM_BASE + 3)
#define GPIO_CTL_SET_PULSE					(GPIO_CTL_PWM_BASE + 4)
#define GPIO_CTL_GET_PULSES					(GPIO_CTL_PWM_BASE + 5)
#define GPIO_CTL_SET_PULSES					(GPIO_CTL_PWM_BASE + 6)

#ifdef MOD_GPIODRIVER

extern void gpio_pwm_init();
extern void gpio_pwm_terminate();

#else // MOD_GPIODRIVER

#endif // MOD_GPIODRIVER

#endif /* GPIODRIVER_PWM_H_ */
