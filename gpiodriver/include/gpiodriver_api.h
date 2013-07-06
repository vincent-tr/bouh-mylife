/*
 * gpiodriver_api.h
 *
 *  Created on: 6 juil. 2013
 *      Author: pumbawoman
 */

#ifndef GPIODRIVER_API_H_
#define GPIODRIVER_API_H_

#include "gpiodriver.h"

struct gpiodriver_api
{

};

#ifndef CORE

// les modules qui utilisent gpiodriver_api doivent déclarer une variable nommée gpiodriver_api où ils assignent l'api dans init
extern struct gpiodriver_api *gpiodriver_api;

#endif


#endif /* GPIODRIVER_API_H_ */
