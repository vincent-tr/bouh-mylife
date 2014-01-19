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
extern time_t conf_lastupdate();

#define conf_assert(expr) log_assert(expr == CONFIG_TRUE)

extern const char *conf_get_string(config_setting_t *setting, const char *name);
extern int conf_get_int(config_setting_t *setting, const char *name);

#endif /* CONFIG_H_ */
