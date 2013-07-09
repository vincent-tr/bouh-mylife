/*
 * component.h
 *
 *  Created on: 9 juil. 2013
 *      Author: pumbawoman
 */

#ifndef COMPONENT_H_
#define COMPONENT_H_

struct component;

extern const char *ctype;

extern void component_init();
extern void component_terminate();

extern struct component *component_create(const char *id, int pin_red, int pin_green, int pin_blue);
extern void component_delete(struct component *comp, int delete_config);

#endif /* COMPONENT_H_ */
