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

#define malloc_nofail(var) (log_assert(var = malloc(sizeof(*var))));
#define malloc_array_nofail(var, size) (log_assert(var = malloc(sizeof(*var) * size)));
#define strdup_nofail(dst, src) (log_assert(dst = strdup(src)));


#endif /* TOOLS_H_ */
