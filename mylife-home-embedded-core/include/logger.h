/*
 * logger.h
 *
 *  Created on: 15 juin 2013
 *      Author: pumbawoman
 */

#ifndef LOGGER_H_
#define LOGGER_H_

extern void log_init();
extern void log_terminate();

#define LOG_DEBUG	1
#define LOG_INFO	2
#define LOG_WARNING	3
#define LOG_ERROR	4
#define LOG_FATAL	5

extern void log_write(const char *file, int line, int level, const char *format, ...);

#endif /* LOGGER_H_ */
