/*
 * config.h
 *
 *  Created on: 16 juin 2013
 *      Author: pumbawoman
 */

#ifndef CONFIG_H_
#define CONFIG_H_

enum config_type
{
	CHAR,
	INT,
	INT64,
	STRING,
	BUFFER,
	CHAR_ARRAY,
	INT_ARRAY,
	INT64_ARRAY,
	STRING_ARRAY
};

#ifdef CORE

extern void config_init();
extern void config_terminate();

extern int config_read_char(const char *section, const char *name, char *value);
extern int config_read_int(const char *section, const char *name, int *value);
extern int config_read_int64(const char *section, const char *name, long long *value);
extern int config_read_string(const char *section, const char *name, char **value); // value allocated, free it after usage
extern int config_read_buffer(const char *section, const char *name, void **buffer, size_t *buffer_len); // buffer allocated, free it after usage
extern int config_read_char_array(const char *section, const char *name, size_t *array_len, char **value); // array allocated, free it after range
extern int config_read_int_array(const char *section, const char *name, size_t *array_len, int **value); // array allocated, free it after range
extern int config_read_int64_array(const char *section, const char *name, size_t *array_len, long long **value); // array allocated, free it after range
extern int config_read_string_array(const char *section, const char *name, size_t *array_len, char ***value); // value allocated, free it after usage (1 buffer)

extern int config_write_char(const char *section, const char *name, char value);
extern int config_write_int(const char *section, const char *name, int value);
extern int config_write_int64(const char *section, const char *name, long long value);
extern int config_write_string(const char *section, const char *name, const char *value);
extern int config_write_buffer(const char *section, const char *name, const void *buffer, size_t buffer_len);
extern int config_write_char_array(const char *section, const char *name, size_t array_len, const char *value);
extern int config_write_int_array(const char *section, const char *name, size_t array_len, const int *value);
extern int config_write_int64_array(const char *section, const char *name, size_t array_len, const long long *value);
extern int config_write_string_array(const char *section, const char *name, size_t array_len, const char **value);

extern int config_delete_entry(const char *section, const char *name); // 1 if success 0 if error
extern int config_delete_section(const char *section); // 1 if success 0 if error

extern void config_enum_sections(int (*callback)(const char *section, void *ctx), void *ctx);
extern int config_enum_entries(const char *section, int (*callback)(const char *name, void *ctx), void *ctx); // 1 if success 0 if error
extern int config_get_entry_type(const char *section, const char *name, enum config_type *type); // 1 if success 0 if error

#else // CORE

// TODO

#endif // CORE

#endif /* CONFIG_H_ */
