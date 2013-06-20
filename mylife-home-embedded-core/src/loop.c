/*
 * loop.c
 *
 *  Created on: 19 juin 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include <sys/select.h>
#include <sys/time.h>

#include "logger.h"
#include "loop.h"
#include "list.h"
#include "tools.h"
#include "config_base.h"

struct tick
{
	void (*callback)(void *ctx);
};

struct timer
{
	void (*callback)(void *ctx);
	int period_ms;
	struct timeval last_run;
};

struct listener
{
	callback_select callback_add;
	callback_select callback_process;
};

struct loop_handle
{
	struct list_node node;

	enum
	{
		TICK,
		TIMER,
		LISTENER
	} type;

	void *ctx;

	union
	{
		struct tick tick;
		struct timer timer;
		struct listener listener;
	} data;
};

struct timer_exec_data
{

};

struct listener_exec_data
{
	int *nfds;
	fd_set *readfds;
	fd_set *writefds;
	fd_set *exceptfds;
};

struct list handles;
static int running = 1;

static void run_ticks(struct list *handles_copy);
static void run_timers(struct list *handles_copy);
static void run_listeners(struct list *handles_copy);

static void list_free_item(void *node, void *ctx);
static int list_copy_item(void *node, void *ctx);
static int list_run_item_tick(void *node, void *ctx);
static int list_run_item_listener_add(void *node, void *ctx);
static int list_run_item_listener_process(void *node, void *ctx);

static void ms2tv(struct timeval *result, unsigned long interval_ms);

void loop_init()
{
	list_init(&handles);
}

void loop_terminate()
{
	list_clear(&handles, list_free_item, NULL);
}

void loop_run()
{
	while(running)
	{
		// copie de la liste pour ne pas subir les suppressions/ajouts d'items
		struct list handles_copy;
		list_init(&handles_copy);
		list_foreach(&handles, list_copy_item, &handles_copy);

		run_ticks(&handles_copy);
		run_timers(&handles_copy);
		run_listeners(&handles_copy);

		list_clear(&handles_copy, list_free_item, NULL);
	}
}

void loop_exit()
{
	running = 0;
}

void run_ticks(struct list *handles_copy)
{
	list_foreach(handles_copy, list_run_item_tick, NULL);
}

void run_timers(struct list *handles_copy)
{
	// TODO
}

void run_listeners(struct list *handles_copy)
{
	int nfds;
	fd_set readfds;
	fd_set writefds;
	fd_set exceptfds;
	struct timeval tv;

	struct listener_exec_data data;
	data.exceptfds = &exceptfds;
	data.readfds = &readfds;
	data.writefds = &writefds;
	data.nfds = &nfds;

	nfds = -1;
	FD_ZERO(&readfds);
	FD_ZERO(&writefds);
	FD_ZERO(&exceptfds);
	ms2tv(&tv, CONFIG_LOOP_MS);

	list_foreach(handles_copy, list_run_item_listener_add, &data);
	log_assert(select(nfds, &readfds, &writefds, &exceptfds, &tv) != -1);
	list_foreach(handles_copy, list_run_item_listener_process, &data);
}

void list_free_item(void *node, void *ctx)
{
	free(node);
}

int list_copy_item(void *node, void *ctx)
{
	struct list *handles_copy = ctx;
	struct loop_handle *src = node;
	struct loop_handle *dest;

	malloc_nofail(dest);
	memcpy(dest, src, sizeof(*src));
	list_add(handles_copy, dest);

	return 1;
}

int list_run_item_tick(void *node, void *ctx)
{
	struct loop_handle *handle = node;

	if(handle->type != TICK)
		return 1;

	handle->data.tick.callback(handle->ctx);

	return 1;
}

int list_run_item_listener_add(void *node, void *ctx)
{
	struct loop_handle *handle = node;
	struct listener_exec_data *data = ctx;

	if(handle->type != LISTENER)
		return 1;

	handle->data.listener.callback_add(data->nfds, data->readfds, data->writefds, data->exceptfds, handle->ctx);
	return 1;
}

int list_run_item_listener_process(void *node, void *ctx)
{
	struct loop_handle *handle = node;
	struct listener_exec_data *data = ctx;

	if(handle->type != LISTENER)
		return 1;

	handle->data.listener.callback_process(data->nfds, data->readfds, data->writefds, data->exceptfds, handle->ctx);
	return 1;
}

/*
 * convert ms(milliseconds) to timeval struct
 * http://enl.usc.edu/enl/trunk/peg/testPlayer/timeval.c
 */
void ms2tv(struct timeval *result, unsigned long interval_ms)
{
    result->tv_sec = (interval_ms / 1000);
    result->tv_usec = ((interval_ms % 1000) * 1000);
}

struct loop_handle *loop_register_tick(void (*callback)(void *ctx), void *ctx)
{
	if(!callback)
		return NULL;

	struct loop_handle *handle;
	malloc_nofail(handle);

	handle->type = TICK;
	handle->data.tick.callback = callback;
	handle->ctx = ctx;

	list_add(&handles, handle);
	return handle;
}

struct loop_handle *loop_register_timer(void (*callback)(void *ctx), void *ctx, int period_ms)
{
	if(!callback)
		return NULL;
	if(period_ms <= 0)
		return NULL;

	struct loop_handle *handle;
	malloc_nofail(handle);

	handle->type = TIMER;
	handle->data.timer.callback = callback;
	handle->data.timer.period_ms = period_ms;
	gettimeofday(&(handle->data.timer.last_run), NULL);
	handle->ctx = ctx;

	list_add(&handles, handle);
	return handle;
}

struct loop_handle *loop_register_listener(callback_select callback_add, callback_select callback_process, void *ctx)
{
	if(!callback_add)
		return NULL;
	if(!callback_process)
		return NULL;

	struct loop_handle *handle;
	malloc_nofail(handle);

	handle->type = LISTENER;
	handle->data.listener.callback_add = callback_add;
	handle->data.listener.callback_process = callback_process;
	handle->ctx = ctx;

	list_add(&handles, handle);
	return handle;
}

void loop_unregister(struct loop_handle *handle)
{
	list_remove(&handles, handle);
	free(handle);
}

void *loop_get_ctx(struct loop_handle *handle)
{
	return handle->ctx;
}

void loop_set_ctx(struct loop_handle *handle, void *ctx)
{
	handle->ctx = ctx;
}
