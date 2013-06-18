/*
 * list.h
 *
 *  Created on: 17 juin 2013
 *      Author: pumbawoman
 */

#ifndef LIST_H_
#define LIST_H_

struct list_node
{
	struct list_node *prev;
	struct list_node *next;
};

struct list
{
	struct list_node *head;
	struct list_node *tail;
};

#ifdef CORE

extern void list_init(struct list *list);
extern void list_add(struct list *list, void *node);
extern void list_remove(struct list *list, void *node);
extern void list_foreach(struct list *list, int (*callback)(void *node, void *ctx), void *ctx); // return 0 = break foreach
extern void list_clear(struct list *list, void (*free_node)(void *node, void *ctx), void *ctx);
extern int list_is_empty(struct list *list);

#else // CORE

#include "core_api.h"

#define list_init(list) (core_api->list_init(list))
#define list_add(list, node) (core_api->list_add(list, node))
#define list_remove(list, node) (core_api->list_remove(list, node))
#define list_foreach(list, callback, ctx) (core_api->list_foreach(list, callback, ctx)) // return 0 = break foreach
#define list_clear(list, free_node, ctx) (core_api->list_clear(list, free_node, ctx))
#define list_is_empty(list) (core_api->list_is_empty(list))

#endif // CORE

#endif /* LIST_H_ */
