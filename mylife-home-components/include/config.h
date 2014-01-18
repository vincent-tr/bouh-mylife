/*
 * config.h
 *
 *  Created on: Jan 17, 2014
 *      Author: vincent
 */

#ifndef CONFIG_H_
#define CONFIG_H_

#include <libconfig.h>

extern void conf_init();
extern void conf_terminate();

extern config_t *conf_get();

#define conf_assert(expr) log_assert(expr == CONFIG_TRUE)

#endif /* CONFIG_H_ */
