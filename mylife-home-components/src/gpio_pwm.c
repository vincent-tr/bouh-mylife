/*
 * gpiodriver_pwm.c
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */
#include <stdio.h>
#include <stddef.h>
#include <stdarg.h>
#include <sys/select.h>

#include "list.h"
#include "gpio.h"
#include "gpio_internal.h"
#include "logger.h"
#include "tools.h"
#include "sysfs_utils.h"

struct pwm
{
	int period;
	int pulse;
	int pulses;
};

static int pwm_open(struct gpio *gpio, va_list args);
static void pwm_close(struct gpio *gpio);
static int pwm_ctl(struct gpio *gpio, int ctl, va_list args);
static char *itos_static(int value);

static struct driver_type type =
{
	.type = GPIO_TYPE_PWM,
	.open = pwm_open,
	.close = pwm_close,
	.ctl = pwm_ctl
};

static struct sysfs_def sysfs =
{
	.class = "soft_pwm",
	.obj_prefix = "pwm"
};

void pwm_init()
{
	register_type(&type);
}

void pwm_terminate()
{
	unregister_type(&type);
}

int pwm_open(struct gpio *gpio, va_list args)
{
	int gpionb = gpio->gpio;
	sysfs_export(&sysfs, gpionb);

	struct pwm *pwm;
	malloc_nofail(pwm);
	gpio->type_data = pwm;

	sscanf(sysfs_read(&sysfs, gpionb, "period"), "%d", &(pwm->period));
	sscanf(sysfs_read(&sysfs, gpionb, "pulse"), "%d", &(pwm->pulse));
	sscanf(sysfs_read(&sysfs, gpionb, "pulses"), "%d", &(pwm->pulses));

	return 1;
}

void pwm_close(struct gpio *gpio)
{
	int gpionb = gpio->gpio;

	sysfs_write(&sysfs, gpionb, "period", "0");
	sysfs_write(&sysfs, gpionb, "pulse", "0");
	sysfs_write(&sysfs, gpionb, "pulses", "-1");

	sysfs_unexport(&sysfs, gpionb);

	free(gpio->type_data);
}

int pwm_ctl(struct gpio *gpio, int ctl, va_list args)
{
	int gpionb = gpio->gpio;
	struct pwm *pwm = gpio->type_data;
	int ret;

	switch(ctl)
	{
	case GPIO_CTL_GET_PERIOD:
		{
			int *period = va_arg(args, int *);
			*period = pwm->period;
			ret = 1;
		}
		break;

	case GPIO_CTL_SET_PERIOD:
		{
			int period = va_arg(args, int);
			pwm->period = period;
			sysfs_write(&sysfs, gpionb, "period", itos_static(pwm->period));
			ret = 1;
		}
		break;

	case GPIO_CTL_GET_PULSE:
		{
			int *pulse = va_arg(args, int *);
			*pulse = pwm->pulse;
			ret = 1;
		}
		break;

	case GPIO_CTL_SET_PULSE:
		{
			int pulse = va_arg(args, int);
			pwm->pulse = pulse;
			sysfs_write(&sysfs, gpionb, "pulse", itos_static(pwm->pulse));
			ret = 1;
		}
		break;

	case GPIO_CTL_GET_PULSES:
		{
			int *pulses = va_arg(args, int *);
			*pulses = pwm->pulses;
			ret = 1;
		}
		break;

	case GPIO_CTL_SET_PULSES:
		{
			int pulses = va_arg(args, int);
			pwm->pulses = pulses;
			sysfs_write(&sysfs, gpionb, "pulses", itos_static(pwm->pulses));
			ret = 1;
		}
		break;

	default:
		ret = 0;
		break;
	}

	return ret;
}

char *itos_static(int value)
{
	static char buffer[15];
	sprintf(buffer, "%d", value);
	return buffer;
}
