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

#endif // CORE

#endif /* MANAGER_H_ */
