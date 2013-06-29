/*
 * list.c
 *
 *  Created on: 17 juin 2013
 *      Author: pumbawoman
 */

#include <stddef.h>
#include "list.h"

void list_init(struct list *list)
{
	list->head = NULL;
	list->tail = NULL;
}

void list_add(struct list *list, void *node)
{
	struct list_node *lnode = node;

	lnode->next = NULL;
	lnode->prev = NULL;

	if(!list->tail)
	{
		list->tail = list->head = lnode;
	}
	else
	{
		list->tail->next = lnode;
		lnode->prev = list->tail;
		list->tail = lnode;
	}
}

void list_remove(struct list *list, void *node)
{
	struct list_node *lnode = node;

	if(lnode->prev)
		lnode->prev->next = lnode->next;
	else
		list->head = lnode->next;

	if(lnode->next)
		lnode->next->prev = lnode->prev;
	else
		list->tail = lnode->prev;

	lnode->prev = NULL;
	lnode->next = NULL;
}

void list_foreach(struct list *list, int (*callback)(void *node, void *ctx), void *ctx) // return 0 = break foreach
{
	for(struct list_node *node = list->head; node; node = node->next)
	{
		if(!callback(node, ctx))
			return;
	}
}

void list_clear(struct list *list, void (*free_node)(void *node, void *ctx), void *ctx)
{
	struct list_node *node;

	while((node = list->head))
	{
		list_remove(list, node);
		free_node(node, ctx);
	}
}

int list_is_empty(struct list *list)
{
	return list->head ? 1 : 0;
}

int list_count(struct list *list)
{
	int count = 0;
	for(struct list_node *node = list->head; node; node = node->next, ++count);
	return count;
}
