/*
 * sysfs_utils.c
 *
 *  Created on: 7 juil. 2013
 *      Author: pumbawoman
 */

#define _BSD_SOURCE
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dirent.h>
#include <unistd.h>

#include "core_api.h"
#include "sysfs_utils.h"

static void write_value(const char *file, const char *value);
static char *read_value(const char *file);
static int directory_exists(const char *dir);

#define BUFFER_SIZE 512

void sysfs_export(struct sysfs_def *def, int gpio)
{
	static char path[PATH_MAX];

	// ensure not already opened (soft_pwm crashes if reopen ??)
	snprintf(path, PATH_MAX, "/sys/class/%s/%d", def->class, gpio);
	path[PATH_MAX-1] = '\0';
	log_assert(!directory_exists(path));

	snprintf(path, PATH_MAX, "/sys/class/%s/export", def->class);
	path[PATH_MAX-1] = '\0';

	static char buffer[15];
	sprintf(buffer, "%d", gpio);

	write_value(path, buffer);
}

void sysfs_unexport(struct sysfs_def *def, int gpio)
{
	static char path[PATH_MAX];
	snprintf(path, PATH_MAX, "/sys/class/%s/unexport", def->class);
	path[PATH_MAX-1] = '\0';

	static char buffer[15];
	sprintf(buffer, "%d", gpio);

	write_value(path, buffer);
}

char *sysfs_read(struct sysfs_def *def, int gpio, const char *attr)
{
	static char path[PATH_MAX];
	snprintf(path, PATH_MAX, "/sys/class/%s/%s%d/%s", def->class, def->obj_prefix, gpio, attr);
	path[PATH_MAX-1] = '\0';

	return read_value(path);
}

void sysfs_write(struct sysfs_def *def, int gpio, const char *attr, const char *value)
{
	static char path[PATH_MAX];
	snprintf(path, PATH_MAX, "/sys/class/%s/%s%d/%s", def->class, def->obj_prefix, gpio, attr);
	path[PATH_MAX-1] = '\0';

	write_value(path, value);
}

int sysfs_open(struct sysfs_def *def, int gpio, const char *attr, int open_flags)
{
	static char path[PATH_MAX];
	snprintf(path, PATH_MAX, "/sys/class/%s/%s%d/%s", def->class, def->obj_prefix, gpio, attr);
	path[PATH_MAX-1] = '\0';

	int fd;
	log_assert((fd = open(path, open_flags)) != -1);
	return fd;
}

void write_value(const char *file, const char *value)
{
	int fd;
	log_assert((fd = open(file, O_WRONLY)) != -1);
	write(fd, value, strlen(value));

	close(fd);
}

char *read_value(const char *file)
{
	static char buffer[BUFFER_SIZE];

	int fd;
	log_assert((fd = open(file, O_RDONLY)) != -1);

	size_t size = read(fd, buffer, BUFFER_SIZE - 1);
	buffer[size] = '\0';

	close(fd);

	return buffer;
}

int directory_exists(const char *dir)
{
	DIR* d = opendir(dir);
	if (dir)
		closedir(d);
	return d ? 1 : 0;
}
