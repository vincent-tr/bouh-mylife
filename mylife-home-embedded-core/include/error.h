/*
 * error.h
 *
 *  Created on: 8 juil. 2013
 *      Author: pumbawoman
 */

#ifndef ERROR_H_
#define ERROR_H_

#define ERROR_SUCCESS 0

#define ERROR_MAKE(factoryid, errid) (((unsigned int)(factoryid) << 16) | (errid))

#define ERROR_FACTORY_CORE 0

#define ERROR_CORE_INVAL			 	ERROR_MAKE(ERROR_FACTORY_CORE, 1)
#define ERROR_CORE_ACCESSDENIED		 	ERROR_MAKE(ERROR_FACTORY_CORE, 2)
#define ERROR_CORE_NOTFOUND 			ERROR_MAKE(ERROR_FACTORY_CORE, 3)
#define ERROR_CORE_EXISTS 				ERROR_MAKE(ERROR_FACTORY_CORE, 4)
#define ERROR_CORE_IOERROR 				ERROR_MAKE(ERROR_FACTORY_CORE, 5)
#define ERROR_CORE_BADMOD 				ERROR_MAKE(ERROR_FACTORY_CORE, 6)
#define ERROR_CORE_UNRESOLVEDDEP 		ERROR_MAKE(ERROR_FACTORY_CORE, 7)
#define ERROR_CORE_EXISTINGDEP 			ERROR_MAKE(ERROR_FACTORY_CORE, 8)
#define ERROR_CORE_DISCONNECTED 		ERROR_MAKE(ERROR_FACTORY_CORE, 9)

#ifdef CORE

// error composition : low word = error number, hi word = factory number
// 0 = no error

extern void error_init();
extern void error_terminate();

extern void error_register_factory(unsigned short factoryid, const char *name);
extern void error_register_value(unsigned int err, const char *description);
extern void error_unregister_factory(unsigned short factoryid); // unregister all descriptions

extern const char *error_description(unsigned int err);
extern const char *error_factory_name(unsigned int err);

extern int *error_internal_get_ptr();
extern int error_internal_success();
extern int error_internal_failed(int err);

#else // CORE

#define error_register_factory(factoryid, name) (core_api->error_register_factory(factoryid, name))
#define error_register_value(err, description) (core_api->error_register_value(err, description))
#define error_unregister_factory(factoryid) (core_api->error_unregister_factory(factoryid)) // unregister all descriptions

#define error_description(err) (core_api->error_description(err))
#define error_factory_name(err) (core_api->error_factory_name(err))

#define error_get_ptr_internal() (core_api->error_get_ptr_internal())
#define error_internal_success() (core_api->error_internal_success())
#define error_internal_failed(err)  (core_api->error_internal_failed(err))

#endif // CORE

#define error_last (*error_internal_get_ptr())
#define error_reset() (error_last = ERROR_SUCCESS)

#define error_factoryid(err) ((unsigned short)(((unsigned int)(err) >> 16) & 0xFFFF))
#define error_errid(err) ((unsigned short)(err))

#define error_success() (error_internal_success())
#define error_failed(err) (error_internal_failed(err))
#define error_failed_ptr(err) ((void *)error_internal_failed(err))

#endif /* ERROR_H_ */
