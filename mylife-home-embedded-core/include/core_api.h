/*
 * core_api.h
 *
 *  Created on: 17 juin 2013
 *      Author: pumbawoman
 */

#ifndef CORE_API_H_
#define CORE_API_H_

#include "list.h"

struct core_api
{
	void (*list_init)(struct list *list);
	void (*list_add)(struct list *list, void *node);
	void (*list_remove)(struct list *list, void *node);
	void (*list_foreach)(struct list *list, int (*callback)(void *node)); // return 0 = break foreach
	void (*list_clear)(struct list *list, void (*free_node)(void *node));

	// TODO
};

#endif /* CORE_API_H_ */
