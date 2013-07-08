/*
 * sysfs_utils.h
 *
 *  Created on: 7 juil. 2013
 *      Author: pumbawoman
 */

#ifndef SYSFS_UTILS_H_
#define SYSFS_UTILS_H_

struct sysfs_def
{
	const char *class;
	const char *obj_prefix;
};

extern void sysfs_export(struct sysfs_def *def, int gpio); // thread unsafe
extern void sysfs_unexport(struct sysfs_def *def, int gpio); // thread unsafe

extern char *sysfs_read(struct sysfs_def *def, int gpio, const char *attr); // ret static --  thread unsafe
extern void sysfs_write(struct sysfs_def *def, int gpio, const char *attr, const char *value); // thread unsafe
extern int sysfs_open(struct sysfs_def *def, int gpio, const char *attr, int open_flags); // return fd

#endif /* SYSFS_UTILS_H_ */
