/*
 * loop.h
 *
 *  Created on: Jan 17, 2014
 *      Author: vincent
 */

#ifndef LOOP_H_
#define LOOP_H_

struct loop_handle;

typedef void (*callback_select_add)(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
typedef void (*callback_select_process)(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);

extern void loop_init();
extern void loop_terminate();
extern void loop_run();
extern void loop_exit();

extern struct loop_handle *loop_register_tick(void (*callback)(void *ctx), void *ctx);
extern struct loop_handle *loop_register_timer(void (*callback)(void *ctx), void *ctx, int period_ms);
extern struct loop_handle *loop_register_listener(callback_select_add callback_add, callback_select_process callback_process, void *ctx);
extern void loop_unregister(struct loop_handle *handle);
extern void *loop_get_ctx(struct loop_handle *handle);
extern void loop_set_ctx(struct loop_handle *handle, void *ctx);

#endif /* LOOP_H_ */
