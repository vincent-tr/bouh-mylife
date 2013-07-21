/*
 * component.h
 *
 *  Created on: 9 juil. 2013
 *      Author: pumbawoman
 */

#ifndef COMPONENT_H_
#define COMPONENT_H_

struct component;

extern void component_init();
extern void component_terminate();

extern struct component *component_create(const char *id, int pin_red, int pin_green, int pin_blue);
extern void component_delete(struct component *comp, int delete_config);

extern const char *component_get_id(struct component *comp);

#endif /* COMPONENT_H_ */
