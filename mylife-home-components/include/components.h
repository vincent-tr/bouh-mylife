/*
 * components.h
 *
 *  Created on: Jan 16, 2014
 *      Author: vincent
 */

#ifndef COMPONENTS_H_
#define COMPONENTS_H_

#include "list.h"
#include <libconfig.h>

struct component_type
{
	struct list_node node; // reserved
	const char *name;
	void *(*creator)(const char *id, config_setting_t *config);
	void (*destructor)(void *handle);
};

extern void components_init();
extern void components_terminate();

extern void component_register(struct component_type *type);

// internal
extern void comp_internal_test_init();
extern void comp_internal_test_terminate();
extern void comp_internal_mpd_init();
extern void comp_internal_mpd_terminate();

#endif /* COMPONENTS_H_ */
