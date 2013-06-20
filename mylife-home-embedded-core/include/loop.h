/*
 * loop.h
 *
 *  Created on: 19 juin 2013
 *      Author: pumbawoman
 */

#ifndef LOOP_H_
#define LOOP_H_

struct loop_handle;

typedef void (*callback_select)(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);

#ifdef CORE

extern void loop_init();
extern void loop_terminate();
extern void loop_run();
extern void loop_exit();

extern struct loop_handle *loop_register_tick(void (*callback)(void *ctx), void *ctx);
extern struct loop_handle *loop_register_timer(void (*callback)(void *ctx), void *ctx, int period_ms);
extern struct loop_handle *loop_register_listener(callback_select callback_add, callback_select callback_process, void *ctx);
extern void loop_unregister(struct loop_handle *handle);
extern void *loop_get_ctx(struct loop_handle *handle);
extern void loop_set_ctx(struct loop_handle *handle, void *ctx);

#else // CORE

#include "core_api.h"

#define loop_register_tick(callback, ctx) (core_api->loop_register_tick(callback, ctx))
#define loop_register_timer(callback, ctx, period_ms) (core_api->loop_register_timer(callback, ctx, period_ms)) // devrait être au moins égal à CONFIG_LOOP_MS
#define loop_register_listener(callback_add, callback_process, ctx) (core_api->loop_register_listener(callback_add, callback_process, ctx))
#define loop_unregister(handle) (core_api->loop_unregister(handle))
#define loop_get_ctx(handle) (core_api->loop_get_ctx(handle))
#define loop_set_ctx(handle, ctx) (core_api->loop_set_ctx(handle, ctx))

#endif // CORE

#endif /* LOOP_H_ */
