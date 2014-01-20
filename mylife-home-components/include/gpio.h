/*
 * gpio.h
 *
 *  Created on: Jan 19, 2014
 *      Author: vincent
 */

#ifndef GPIO_H_
#define GPIO_H_

#include "logger.h"

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

extern void gpio_init();
extern void gpio_terminate();

extern struct gpio *gpio_open(int pin, const char *usage, int type, ...);
extern void gpio_close(struct gpio *gpio);
extern int gpio_ctl(struct gpio *gpio, int ctl, ...);

/******************************** types data ********************************/

#define GPIO_TYPE_IO 1
#define GPIO_TYPE_PWM 2
#define GPIO_TYPE_LOCK 3

#define GPIO_CTL_BASE						(0x000)
#define GPIO_CTL_GET_PIN_NUMBER				(GPIO_CTL_BASE + 1)
#define GPIO_CTL_GET_GPIO_NUMBER			(GPIO_CTL_BASE + 2)
#define GPIO_CTL_GET_TYPE					(GPIO_CTL_BASE + 3)
#define GPIO_CTL_GET_USAGE					(GPIO_CTL_BASE + 4)

#define GPIO_CTL_IO_BASE					(0x100)
#define GPIO_CTL_GET_VALUE					(GPIO_CTL_IO_BASE + 1)
#define GPIO_CTL_SET_VALUE					(GPIO_CTL_IO_BASE + 2)
#define GPIO_CTL_GET_DIRECTION				(GPIO_CTL_IO_BASE + 3)
#define GPIO_CTL_SET_DIRECTION				(GPIO_CTL_IO_BASE + 4)
#define GPIO_CTL_SET_CHANGE_CALLBACK		(GPIO_CTL_IO_BASE + 5)

#define GPIO_CTL_PWM_BASE					(0x200)
#define GPIO_CTL_GET_PERIOD					(GPIO_CTL_PWM_BASE + 1)
#define GPIO_CTL_SET_PERIOD					(GPIO_CTL_PWM_BASE + 2)
#define GPIO_CTL_GET_PULSE					(GPIO_CTL_PWM_BASE + 3)
#define GPIO_CTL_SET_PULSE					(GPIO_CTL_PWM_BASE + 4)
#define GPIO_CTL_GET_PULSES					(GPIO_CTL_PWM_BASE + 5)
#define GPIO_CTL_SET_PULSES					(GPIO_CTL_PWM_BASE + 6)

#define GPIO_CTL_LOCK_BASE					(0x300)

enum gpio_direction
{
	in,
	out
};

typedef void (*gpio_change_callback)(struct gpio *gpio, int value, void *ctx);

inline int gpio_get_pin_number(struct gpio *gpio) 														{ int val; log_assert(gpio_ctl(gpio, GPIO_CTL_GET_PIN_NUMBER, &val)); return val; }
inline int gpio_get_gpio_number(struct gpio *gpio)														{ int val; log_assert(gpio_ctl(gpio, GPIO_CTL_GET_GPIO_NUMBER, &val)); return val; }
inline int gpio_get_type(struct gpio *gpio)																{ int val; log_assert(gpio_ctl(gpio, GPIO_CTL_GET_TYPE, &val)); return val; }
inline const char *gpio_get_usage(struct gpio *gpio)													{ const char *val; log_assert(gpio_ctl(gpio, GPIO_CTL_GET_USAGE, &val)); return val; }

inline int gpio_io_get_value(struct gpio *gpio)															{ int val; log_assert(gpio_ctl(gpio, GPIO_CTL_GET_VALUE, &val)); return val; }
inline void gpio_io_set_value(struct gpio *gpio, int value)												{ log_assert(gpio_ctl(gpio, GPIO_CTL_SET_VALUE, value)); }
inline enum gpio_direction gpio_io_get_direction(struct gpio *gpio)										{ enum gpio_direction val; log_assert(gpio_ctl(gpio, GPIO_CTL_GET_DIRECTION, &val)); return val; }
inline void gpio_io_set_direction(struct gpio *gpio, enum gpio_direction value)							{ log_assert(gpio_ctl(gpio, GPIO_CTL_SET_DIRECTION, value)); }
inline void gpio_io_set_change_callback(struct gpio *gpio, gpio_change_callback callback, void *ctx)	{ log_assert(gpio_ctl(gpio, GPIO_CTL_SET_CHANGE_CALLBACK, callback, ctx)); }

inline int gpio_pwm_get_period(struct gpio *gpio)														{ int val; log_assert(gpio_ctl(gpio, GPIO_CTL_GET_PERIOD, &val)); return val; }
inline void gpio_pwm_set_period(struct gpio *gpio, int value)											{ log_assert(gpio_ctl(gpio, GPIO_CTL_SET_PERIOD, value)); }
inline int gpio_pwm_get_pulse(struct gpio *gpio)														{ int val; log_assert(gpio_ctl(gpio, GPIO_CTL_GET_PULSE, &val)); return val; }
inline void gpio_pwm_set_pulse(struct gpio *gpio, int value)											{ log_assert(gpio_ctl(gpio, GPIO_CTL_SET_PULSE, value)); }
inline int gpio_pwm_get_pulses(struct gpio *gpio)														{ int val; log_assert(gpio_ctl(gpio, GPIO_CTL_GET_PULSES, &val)); return val; }
inline void gpio_pwm_set_pulses(struct gpio *gpio, int value)											{ log_assert(gpio_ctl(gpio, GPIO_CTL_SET_PULSES, value)); }

#endif /* GPIO_H_ */
