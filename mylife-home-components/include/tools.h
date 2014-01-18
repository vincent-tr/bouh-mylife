/*
 * tools.h
 *
 *  Created on: 19 juin 2013
 *      Author: pumbawoman
 */

#ifndef TOOLS_H_
#define TOOLS_H_

#include <stdlib.h>
#include <string.h>
#include "logger.h"

#define malloc_nofail(var) (log_assert(((var) = malloc(sizeof(*(var))))))
#define malloc_array_nofail(var, size) (log_assert(((var) = malloc(sizeof(*(var)) * (size)))))
#define realloc_nofail(var, size) (log_assert(((var) = realloc((var), size))))
#define strdup_nofail(dst, src) (log_assert(((dst) = strdup((src)))))
#define meminit(ptr) (memset((ptr), 0, sizeof(*(ptr))))

#endif /* TOOLS_H_ */
