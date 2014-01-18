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

extern void list_init(struct list *list);
extern void list_add(struct list *list, void *node);
extern void list_remove(struct list *list, void *node);
extern void list_foreach(struct list *list, int (*callback)(void *node, void *ctx), void *ctx); // return 0 = break foreach
extern void list_clear(struct list *list, void (*free_node)(void *node, void *ctx), void *ctx);
extern int list_is_empty(struct list *list);
extern int list_count(struct list *list);

#endif /* LIST_H_ */
