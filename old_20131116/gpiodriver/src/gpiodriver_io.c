/*
 * gpiodriver_io.c
 *
 *  Created on: 6 juil. 2013
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
#include "gpiodriver_io.h"
#include "sysfs_utils.h"
#include "tools.h"

static int io_open(struct gpio *gpio, va_list args);
static void io_close(struct gpio *gpio);
static int io_ctl(struct gpio *gpio, int ctl, va_list args);

static void monitor(struct gpio *gpio);
static void unmonitor(struct gpio *gpio);
static void select_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
static void select_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
static int select_add_item(void *node, void *ctx);
static int select_process_item(void *node, void *ctx);

struct io
{
	enum gpio_direction direction;
	gpio_change_callback change_callback;
	int value;
	struct monitor_data *monitor;
};

struct monitor_data
{
	struct list_node node;

	struct gpio *gpio;
	int pollfd;
};

static struct driver_type type =
{
	.type = GPIO_TYPE_IO,
	.open = io_open,
	.close = io_close,
	.ctl = io_ctl
};

static struct sysfs_def sysfs =
{
	.class = "gpio",
	.obj_prefix = "gpio"
};

struct select_data
{
	int *nfds;
	fd_set *readfds;
};

static struct list monitors;
static struct loop_handle *loop_handle;

void gpio_io_init()
{
	register_type(&type);
	list_init(&monitors);
	log_assert(loop_handle = loop_register_listener(select_add, select_process, NULL));
}

void gpio_io_terminate()
{
	loop_unregister(loop_handle);
	log_assert(list_is_empty(&monitors));
	unregister_type(&type);
}

int io_open(struct gpio *gpio, va_list args)
{
	int gpionb = gpio->gpio;
	sysfs_export(&sysfs, gpionb);

	struct io *io;
	malloc_nofail(io);
	gpio->type_data = io;

	io->direction = (!strcmp(sysfs_read(&sysfs, gpionb, "direction"), "out")) ? out : in;
	io->value = (!strcmp(sysfs_read(&sysfs, gpionb, "value"), "1")) ? 1 : 0;
	io->change_callback = NULL;
	sysfs_write(&sysfs, gpionb, "edge", "both");

	if(io->direction == in)
		monitor(gpio);

	return error_success();
}

void io_close(struct gpio *gpio)
{
	int gpionb = gpio->gpio;

	struct io *io = gpio->type_data;
	if(io->direction == in)
		unmonitor(gpio);

	sysfs_write(&sysfs, gpionb, "direction", "in"); // in = less dangerous than out

	sysfs_unexport(&sysfs, gpionb);

	free(gpio->type_data);
}

int io_ctl(struct gpio *gpio, int ctl, va_list args)
{
	int gpionb = gpio->gpio;
	struct io *io = gpio->type_data;
	int ret;

	switch(ctl)
	{
	case GPIO_CTL_GET_VALUE:
		{
			int *value = va_arg(args, int *);
			*value = io->value;
			ret = error_success();
		}
		break;

	case GPIO_CTL_SET_VALUE:
		{
			if(io->direction == out)
			{
				int value = va_arg(args, int);
				io->value = value;
				sysfs_write(&sysfs, gpionb, "value", value ? "1" : "0");
				ret = error_success();
			}
			else
			{
				ret = error_failed(ERROR_CORE_ACCESSDENIED);
			}
		}
		break;

	case GPIO_CTL_GET_DIRECTION:
		{
			enum gpio_direction *direction = va_arg(args, enum gpio_direction *);
			*direction = io->direction;
			ret = error_success();
		}
		break;

	case GPIO_CTL_SET_DIRECTION:
		{
			enum gpio_direction direction = va_arg(args, enum gpio_direction);
			if(io->direction != direction)
			{
				if(io->direction == in)
					unmonitor(gpio);
				io->direction = direction;
				if(io->direction == in)
					monitor(gpio);
				sysfs_write(&sysfs, gpionb, "direction", direction == in ? "in" : "out");
			}
			ret = error_success();
		}
		break;

	case GPIO_CTL_SET_CHANGE_CALLBACK:
		{
			gpio_change_callback *change_callback = va_arg(args, gpio_change_callback *);
			*change_callback = io->change_callback;
			ret = error_success();
		}
		break;

	default:
		ret = error_failed(ERROR_CORE_INVAL);
		break;
	}

	return ret;
}

void monitor(struct gpio *gpio)
{
	int gpionb = gpio->gpio;
	struct io *io = gpio->type_data;

	struct monitor_data *monitor;
	malloc_nofail(monitor);
	io->monitor = monitor;
	monitor->gpio = gpio;
	list_add(&monitors, monitor);

	monitor->pollfd = sysfs_open(&sysfs, gpionb, "value", O_RDONLY);
}

void unmonitor(struct gpio *gpio)
{
	struct io *io = gpio->type_data;

	struct monitor_data *monitor = io->monitor;
	io->monitor = NULL;
	list_remove(&monitors, monitor);

	close(monitor->pollfd);
	free(monitor);
}

void select_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	struct select_data data;
	data.nfds = nfds;
	data.readfds = readfds;
	list_foreach(&monitors, select_add_item, &data);
}

int select_add_item(void *node, void *ctx)
{
	struct monitor_data *monitor = node;
	struct select_data *select = ctx;

	FD_SET(monitor->pollfd, select->readfds);
	if(*(select->nfds) < (monitor->pollfd + 1))
		*(select->nfds) = monitor->pollfd + 1;

	return 1;
}

void select_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	struct select_data data;
	data.nfds = NULL;
	data.readfds = readfds;
	list_foreach(&monitors, select_process_item, &data);
}

int select_process_item(void *node, void *ctx)
{
	struct monitor_data *monitor = node;
	struct select_data *select = ctx;

	if(FD_ISSET(monitor->pollfd, select->readfds))
	{
		// value changed

		// read file to reset readable state
		char c;
		log_assert(read(monitor->pollfd, &c, 1) != -1);

		// access gpio
		struct gpio *gpio = monitor->gpio;
		int gpionb = gpio->gpio;
		struct io *io = gpio->type_data;

		// read value and call callback
		io->value = (!strcmp(sysfs_read(&sysfs, gpionb, "value"), "1")) ? 1 : 0;
		if(io->change_callback)
			io->change_callback(gpio, io->value);
	}

	return 1;
}
