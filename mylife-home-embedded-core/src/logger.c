/*
 * logger.c
 *
 *  Created on: 15 juin 2013
 *      Author: pumbawoman
 */

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <stdarg.h>
#include <syslog.h>
#include <string.h>

#include "logger.h"
#include "config.h"

static int is_interactive;
static const size_t buffer_len = 1024;

static void log_timestamp(char *buffer);

void log_init(int interactive)
{
	is_interactive = interactive;
	openlog(NULL, (is_interactive ? LOG_CONS : 0), LOG_USER);
}

void log_terminate()
{
	closelog();
}

void log_timestamp(char *buffer)
{
	time_t now;
	struct tm tm;
	time(&now);
	localtime_r(&now, &tm);
	strftime(buffer, buffer_len, "[%Y-%m-%d %H:%M:%S] ", &tm);
}

void log_write(const char *file, int line, int level, const char *format, ...)
{
	char buffer[buffer_len];

	log_timestamp(buffer);

	// calcul de l'espace restant
	size_t len = strlen(buffer);
	char *ptr = buffer + len;
	len = buffer_len - len;

	// écriture du buffer
	va_list ap;
	va_start(ap, format);
	vsnprintf(ptr, len, format, ap);
	va_end(ap);

	syslog(level, "%s", buffer);

	if(level == LOG_EMERG)
	{
		log_terminate();
		exit(EXIT_FAILURE);
	}
}
