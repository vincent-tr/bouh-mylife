/*
 * logger.h
 *
 *  Created on: 15 juin 2013
 *      Author: pumbawoman
 */

#ifndef LOGGER_H_
#define LOGGER_H_

#include <syslog.h>

extern void log_init(int interactive);
extern void log_terminate();

extern void log_write(const char *file, int line, int level, const char *format, ...);

#define log_log(level, format, ...) log_write(__FILE__, __LINE__, level, format, __VA_ARGS__)

#define log_debug(format, ...) log_write(__FILE__, __LINE__, LOG_DEBUG, format, __VA_ARGS__)
#define log_info(format, ...) log_write(__FILE__, __LINE__, LOG_INFO, format, __VA_ARGS__)
#define log_warning(format, ...) log_write(__FILE__, __LINE__, LOG_WARNING, format, __VA_ARGS__)
#define log_error(format, ...) log_write(__FILE__, __LINE__, LOG_ERR, format, __VA_ARGS__)
#define log_fatal(format, ...) log_write(__FILE__, __LINE__, LOG_EMERG, format, __VA_ARGS__)

#define log_assert(expr) ({ int __val = expr; if(expr) { log_fatal("assertion '%s' failed", ##expr); } })

#endif /* LOGGER_H_ */
