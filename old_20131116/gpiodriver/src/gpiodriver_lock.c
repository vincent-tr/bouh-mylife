/*
 * gpiodriver_lock.c
 *
 *  Created on: 22 juil. 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include <stdarg.h>
#include <sys/select.h>
#include <fcntl.h>
#include <unistd.h>

#include "core_api.h"
#include "gpiodriver.h"
#include "gpiodriver_internal.h"
#include "gpiodriver_lock.h"
#include "sysfs_utils.h"
#include "tools.h"

static int lock_open(struct gpio *gpio, va_list args);
static void lock_close(struct gpio *gpio);
static int lock_ctl(struct gpio *gpio, int ctl, va_list args);

static struct driver_type type =
{
	.type = GPIO_TYPE_LOCK,
	.open = lock_open,
	.close = lock_close,
	.ctl = lock_ctl
};

void gpio_lock_init()
{
	register_type(&type);
}

void gpio_lock_terminate()
{
	unregister_type(&type);
}

int lock_open(struct gpio *gpio, va_list args)
{
	return error_success();
}

void lock_close(struct gpio *gpio)
{
}

int lock_ctl(struct gpio *gpio, int ctl, va_list args)
{
	return error_failed(ERROR_CORE_INVAL);
}
