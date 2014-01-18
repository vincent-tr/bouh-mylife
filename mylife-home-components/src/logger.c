/*
 * logger.c
 *
 *  Created on: 15 juin 2013
 *      Author: pumbawoman
 */

// localtime_r
#define _POSIX_C_SOURCE 1

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <stdarg.h>
#include <syslog.h>
#include <string.h>
#include <errno.h>

#include "logger.h"

static int is_interactive;
static const size_t buffer_len = 1024;

static void log_timestamp(char *buffer);
static const char *log_level(int level);

void log_init(int interactive)
{
	is_interactive = interactive;
	openlog(NULL, (is_interactive ? /*LOG_CONS*/LOG_PERROR : 0), LOG_USER);
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

const char *log_level(int level)
{
	switch(level)
	{
	case LOG_DEBUG: return "DEBUG";
	case LOG_INFO: return "INFO";
	case LOG_WARNING: return "WARNING";
	case LOG_ERR: return "ERROR";
	case LOG_EMERG: return "FATAL";
	default: return "UNKWNOWN";
	}
}

void log_write(const char *file, int line, int level, const char *format, ...)
{
	char buffer[buffer_len];

	log_timestamp(buffer);

	// calcul de l'espace restant
	size_t len = strlen(buffer);
	char *ptr = buffer + len;
	len = buffer_len - len;

	// ecriture du fichier/ligne/level
	snprintf(ptr, len, "%s:%i %s ", file, line, log_level(level));

	// calcul de l'espace restant
	len = strlen(buffer);
	ptr = buffer + len;
	len = buffer_len - len;

	// ecriture du buffer
	va_list ap;
	va_start(ap, format);
	vsnprintf(ptr, len, format, ap);
	va_end(ap);

	syslog(level, "%s", buffer);

	if(level == LOG_EMERG)
	{
		const char *serrno = NULL;
		if(errno != 0)
			serrno = strerror(errno);
		if(serrno)
			syslog(level, "%s", serrno);

		log_terminate();
		exit(EXIT_FAILURE);
	}
}
