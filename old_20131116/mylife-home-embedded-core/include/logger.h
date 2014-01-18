/*
 * logger.h
 *
 *  Created on: 15 juin 2013
 *      Author: pumbawoman
 */

#ifndef LOGGER_H_
#define LOGGER_H_

#include <syslog.h>

#ifdef CORE

extern void log_init(int interactive);
extern void log_terminate();

extern void log_write(const char *file, int line, int level, const char *format, ...);

#define log_log(level, format, ...) log_write(__FILE__, __LINE__, level, format, __VA_ARGS__)

#define log_debug(format, ...) log_write(__FILE__, __LINE__, LOG_DEBUG, format, __VA_ARGS__)
#define log_info(format, ...) log_write(__FILE__, __LINE__, LOG_INFO, format, __VA_ARGS__)
#define log_warning(format, ...) log_write(__FILE__, __LINE__, LOG_WARNING, format, __VA_ARGS__)
#define log_error(format, ...) log_write(__FILE__, __LINE__, LOG_ERR, format, __VA_ARGS__)
#define log_fatal(format, ...) log_write(__FILE__, __LINE__, LOG_EMERG, format, __VA_ARGS__)

#define log_assert(expr) ({ int __val = (int)(expr); if(!__val) { log_fatal("assertion '%s' failed", #expr); } })

#else // CORE

#include "core_api.h"

#define log_log(level, format, ...) core_api->log_write(__FILE__, __LINE__, level, format, __VA_ARGS__)

#define log_debug(format, ...) core_api->log_write(__FILE__, __LINE__, LOG_DEBUG, format, __VA_ARGS__)
#define log_info(format, ...) core_api->log_write(__FILE__, __LINE__, LOG_INFO, format, __VA_ARGS__)
#define log_warning(format, ...) core_api->log_write(__FILE__, __LINE__, LOG_WARNING, format, __VA_ARGS__)
#define log_error(format, ...) core_api->log_write(__FILE__, __LINE__, LOG_ERR, format, __VA_ARGS__)
#define log_fatal(format, ...) core_api->log_write(__FILE__, __LINE__, LOG_EMERG, format, __VA_ARGS__)

#define log_assert(expr) ({ int __val = (int)(expr); if(!__val) { log_fatal("assertion '%s' failed", #expr); } })

#endif // CORE

#endif /* LOGGER_H_ */
