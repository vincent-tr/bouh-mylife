/*
 * loop.c
 *
 *  Created on: 19 juin 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include <sys/select.h>
#include <sys/time.h>

#include "loop.h"
#include "list.h"
#include "tools.h"

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

struct list handles;
static int running = 1;

static void clear_free(void *node, void *ctx);
static void run_step();

void loop_init()
{
	list_init(&handles);
}

void loop_terminate()
{
	list_clear(&handles, clear_free, NULL);
}

void clear_free(void *node, void *ctx)
{
	free(node);
}

void loop_run()
{
	while(running)
		run_step();
}

void loop_exit()
{
	running = 0;
}

void run_step()
{
	// TODO
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
