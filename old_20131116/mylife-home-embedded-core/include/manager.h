/*
 * manager.h
 *
 *  Created on: 22 juin 2013
 *      Author: pumbawoman
 */

#ifndef MANAGER_H_
#define MANAGER_H_

#ifdef CORE

extern void manager_init();
extern void manager_terminate();

extern struct irc_bot *manager_get_bot();

#else // CORE

#include "core_api.h"

#define manager_get_bot() (core_api->manager_get_bot())

#endif // CORE

#endif /* MANAGER_H_ */
