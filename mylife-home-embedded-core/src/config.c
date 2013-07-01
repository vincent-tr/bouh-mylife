/*
 * config.c
 *
 *  Created on: 28 juin 2013
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
#include <sys/mman.h>
#include <limits.h>

#include "config.h"
#include "list.h"
#include "tools.h"
#include "config_base.h"

struct file_header
{
	int magic;
	size_t entry_count;
};

struct file_entry
{
	char *name;
	enum config_type type;
	union
	{
		char char_value;
		int int_value;
		long long int64_value;
		char *string_value;

		struct
		{
			void *data;
			size_t len;
		} buffer_value;

		struct
		{
			char *array;
			size_t count;
		} char_array_value;

		struct
		{
			int *array;
			size_t count;
		} int_array_value;

		struct
		{
			long long *array;
			size_t count;
		} int64_array_value;

		struct
		{
			void *buffer; // buffer : array of bits, char aligned, to indicate NULL or filled value, and then string1 + \0 + string2 + \0 + ...
			size_t count;
		} string_array_value;
	} data;
};

struct config_section
{
	struct list_node node;

	char *name;
	struct list entries;
};

struct config_entry
{
	struct list_node node;

	struct file_entry entry;
};

struct writer_symbol
{
	off_t address_offset; // file offset where to write the file offset of the written buffer
	off_t buffer_offset;
	void *buffer;
	size_t len;
};

struct config_file_write_data
{
	int fd;
	struct writer_symbol *symbols;
	size_t used_symbols;
};

struct node_lookup_data
{
	const char *name;
	void *result;
};

struct enum_section_data
{
	void *ctx;
	int (*callback)(const char *section, void *ctx);
};

struct enum_entry_data
{
	void *ctx;
	int (*callback)(const char *name, void *ctx);
};

static const char *file_get(const char *name); // thread unsafe
static char *file_load_string(void *filebuff, size_t filesize, char *offset);
static void *file_load_buffer(void *filebuff, size_t filesize, void *offset, size_t buffsize);
static size_t config_file_string_array_len(const void *array_buffer, size_t array_len);
static int config_file_write_item(void *node, void *ctx);
static void config_file_load(const char *name);
static void config_file_save(struct config_section *section);
static void config_file_delete(struct config_section *section);

static int validate_section(const char *section);
static int validate_name(const char *name);

static struct config_section *get_section(const char *section, int create);
static struct config_entry *get_entry(struct config_section *section, const char *name);
static int section_find_item(void *node, void *ctx);
static int entry_find_item(void *node, void *ctx);
static struct config_entry *read_entry(const char *section_name, const char *entry_name, enum config_type requested_type);
static struct config_entry *prepare_write_entry(const char *section_name, const char *entry_name, enum config_type requested_type, struct config_section **sectionref);
static void section_free(void *node, void *ctx);
static void entry_free(void *node, void *ctx);
static int config_enum_entry_item(void *node, void *ctx);
static int config_enum_section_item(void *node, void *ctx);

static struct list sections;

static const char config_magic[] = {'c', 'o', 'n', 'f'};
#define MAGIC (*(int *)config_magic)

#define read_bit(buf, pos) (((const char*)(buf))[(pos) / CHAR_BIT] & (((char)1) >> ((pos) ^ CHAR_BIT)))
#define write_bit(buf, pos, value) (((char*)(buf))[(pos) / CHAR_BIT] = value ? (((char*)(buf))[(pos) / CHAR_BIT] | (((char)1) >> ((pos) ^ CHAR_BIT))) : (((char*)(buf))[(pos) / CHAR_BIT] & ~(((char)1) >> ((pos) ^ CHAR_BIT))))

void config_init()
{
	list_init(&sections);

	DIR *dir;
	struct dirent *item;
	log_assert(dir = opendir(CONFIG_CONFIG_DIRECTORY));

	while((item = readdir(dir)))
	{
		// on ne sélectionne que les fichiers
		if(item->d_type != DT_REG)
			continue;

		config_file_load(item->d_name);
	}

	closedir(dir);
}

void config_terminate()
{
	list_clear(&(sections), section_free, NULL);
}

const char *file_get(const char *name) // thread unsafe
{
	static char path[PATH_MAX];

	// nom de fichier
	snprintf(path, PATH_MAX, "%s/%s", CONFIG_MODULES_DIRECTORY, name);
	path[PATH_MAX-1] = '\0';

	return path;
}

char *file_load_string(void *filebuff, size_t filesize, char *offset)
{
	if(!offset)
		return NULL;
	log_assert(filesize >= (size_t)offset);
	// warning : may fail if string overlap filesize but ok for now ..
	return strdup((char*)filebuff + (ptrdiff_t)offset);
}

void *file_load_buffer(void *filebuff, size_t filesize, void *offset, size_t buffsize)
{
	if(!buffsize)
		return NULL;

	log_assert(offset);
	void *ret;
	log_assert(ret = malloc(buffsize));
	memcpy(ret, (char*)filebuff + (ptrdiff_t)offset, buffsize);
	return ret;
}

size_t config_file_string_array_len(const void *array_buffer, size_t array_len)
{
	const char *bitarray = array_buffer;
	const char *str = array_buffer;

	size_t buflen = array_len / CHAR_BIT;
	str += buflen;

	for(size_t i=0; i<array_len; i++)
	{
		char bit = read_bit(bitarray, i);
		if(bit)
		{
			size_t slen = strlen(str) + 1;
			buflen += slen;
			str += slen;
		}
	}

	return buflen;
}

void config_file_load(const char *name)
{
	const char *path = file_get(name);
	struct stat sb;
	int fd;
	void *filebuff;
	struct file_header *fheader;
	struct file_entry *fentry;
	size_t i;

	log_assert(!stat(path, &sb));
	size_t size = sb.st_size;

	log_assert((fd = open(path, O_RDONLY)) != -1);
	log_assert((filebuff = mmap(NULL, size, PROT_READ, MAP_PRIVATE, fd, 0)) != MAP_FAILED);
	close(fd);

	// now file content is mapped
	log_assert(size >= sizeof(*fheader));
	fheader = filebuff;
	log_assert(fheader->magic == MAGIC);
	log_assert(size >= sizeof(*fheader) + fheader->entry_count * sizeof(*fentry));

	struct config_section *section;
	malloc_nofail(section);
	strdup_nofail(section->name, name);
	list_init(&(section->entries));

	for(i=0, fentry = (struct file_entry *)(fheader+1); i<fheader->entry_count; i++, fentry++)
	{
		struct config_entry *entry;
		malloc_nofail(entry);

		// basic copy for type and basic data
		memcpy(&(entry->entry), fentry, sizeof(*fentry));

		entry->entry.name = file_load_string(filebuff, size, fentry->name);

		switch(entry->entry.type)
		{
			case CHAR:
			case INT:
			case INT64:
				break;

			case STRING:
				entry->entry.data.string_value = file_load_string(filebuff, size, fentry->data.string_value);
				break;

			case BUFFER:
				entry->entry.data.buffer_value.data = file_load_buffer(filebuff, size, fentry->data.buffer_value.data, entry->entry.data.buffer_value.len);
				break;

			case CHAR_ARRAY:
				entry->entry.data.char_array_value.array = file_load_buffer(filebuff, size, fentry->data.char_array_value.array, entry->entry.data.char_array_value.count * sizeof(char));
				break;

			case INT_ARRAY:
				entry->entry.data.int_array_value.array = file_load_buffer(filebuff, size, fentry->data.int_array_value.array, entry->entry.data.int_array_value.count * sizeof(int));
				break;

			case INT64_ARRAY:
				entry->entry.data.int64_array_value.array = file_load_buffer(filebuff, size, fentry->data.int64_array_value.array, entry->entry.data.int64_array_value.count * sizeof(long long));
				break;

			case STRING_ARRAY:
				{
					// buffer : array of bits, char aligned, to indicate NULL or filled value, and then string1 + \0 + string2 + \0 + ...
					size_t array_len = fentry->data.string_array_value.count;
					size_t buflen = 0;

					if(array_len)
					{
						// get buffer ptr
						char *buf =  (char *)fentry->data.string_array_value.buffer + (ptrdiff_t)filebuff;
						// measure buf len
						buflen = config_file_string_array_len(buf, array_len);
					}

					entry->entry.data.string_array_value.buffer = file_load_buffer(filebuff, size, fentry->data.string_array_value.buffer, buflen);
				}
				break;
		}

		list_add(&(section->entries), entry);
	}

	list_add(&sections, section);

	munmap(filebuff, size);
}

#define write_nofail(fd, buf, count) log_assert(write(fd, buf, count) == count)
#define write_type_nofail(fd, ptr) write_nofail(fd, (ptr), sizeof(*(ptr)))

void config_file_save(struct config_section *section)
{

	const char *path = file_get(section->name);
	int fd;
	log_assert((fd = open(path, O_WRONLY | O_CREAT | O_TRUNC)) != -1);

	struct file_header fheader;

	fheader.magic = MAGIC;
	fheader.entry_count = list_count(&(section->entries));
	write_type_nofail(fd, &fheader);

	if(fheader.entry_count > 0)
	{
		// needed symbols is at most 2x entry count, an entry must as a named and may have an attached data buffer
		struct config_file_write_data data;
		data.fd = fd;
		malloc_array_nofail(data.symbols, 2*fheader.entry_count);
		memset(data.symbols, 0, sizeof(*(data.symbols)) * 2*fheader.entry_count);
		data.used_symbols = 0;

		list_foreach(&(section->entries), config_file_write_item, &data);

		// write symbols
		off_t pos = lseek(fd, 0, SEEK_CUR);
		for(int i=0; i<data.used_symbols; i++)
		{
			struct writer_symbol *sym = data.symbols + i;
			if(!sym->buffer || !sym->len)
				continue;

			sym->buffer_offset = pos;
			write_nofail(fd, sym->buffer, sym->len);
			pos += sym->len;
		}

		// write address of symbols
		for(int i=0; i<data.used_symbols; i++)
		{
			struct writer_symbol *sym = data.symbols + i;
			if(!sym->buffer || !sym->len)
				continue;

			lseek(fd, sym->address_offset, SEEK_SET);
			write_type_nofail(fd, &(sym->buffer_offset));
		}
	}

	close(fd);
}

#define get_symbol(data) (data->symbols + ((data->used_symbols)++))

int config_file_write_item(void *node, void *ctx)
{
	struct config_entry *entry = node;
	struct config_file_write_data *data = ctx;

	struct file_entry fentry;

	// offset where we write the entry in the file
	off_t pos = lseek(data->fd, 0, SEEK_CUR);

	// basic copy for type and basic data
	memcpy(&fentry, entry, sizeof(*entry));

	write_type_nofail(data->fd, &fentry);

	// symbol for name
	struct writer_symbol *sym = get_symbol(data);
	sym->buffer = entry->entry.name;
	sym->len = strlen(entry->entry.name) + 1;
	sym->address_offset = pos + offsetof(struct file_entry, name);

	switch(entry->entry.type)
	{
		case CHAR:
		case INT:
		case INT64:
			break;

		case STRING:
			sym = get_symbol(data);
			sym->buffer = entry->entry.data.string_value;
			sym->len = strlen(entry->entry.data.string_value) + 1;
			sym->address_offset = pos + offsetof(struct file_entry, data.string_value);
			break;

		case BUFFER:
			sym = get_symbol(data);
			sym->buffer = entry->entry.data.buffer_value.data;
			sym->len = entry->entry.data.buffer_value.len;
			sym->address_offset = pos + offsetof(struct file_entry, data.buffer_value.data);
			break;

		case CHAR_ARRAY:
			sym = get_symbol(data);
			sym->buffer = entry->entry.data.char_array_value.array;
			sym->len = entry->entry.data.char_array_value.count * sizeof(char);
			sym->address_offset = pos + offsetof(struct file_entry, data.char_array_value.array);
			break;

		case INT_ARRAY:
			sym = get_symbol(data);
			sym->buffer = entry->entry.data.int_array_value.array;
			sym->len = entry->entry.data.int_array_value.count * sizeof(int);
			sym->address_offset = pos + offsetof(struct file_entry, data.int_array_value.array);
			break;

		case INT64_ARRAY:
			sym = get_symbol(data);
			sym->buffer = entry->entry.data.int64_array_value.array;
			sym->len = entry->entry.data.int64_array_value.count * sizeof(long long);
			sym->address_offset = pos + offsetof(struct file_entry, data.int64_array_value.array);
			break;

		case STRING_ARRAY:
			{
				size_t len = entry->entry.data.string_array_value.count;
				if(len)
					len = config_file_string_array_len(entry->entry.data.string_array_value.buffer, len);

				sym = get_symbol(data);
				sym->buffer = entry->entry.data.string_array_value.buffer;
				sym->len = len;
				sym->address_offset = pos + offsetof(struct file_entry, data.string_array_value.buffer);
			}
			break;
	}

	return 1;
}

#undef get_symbol
#undef write_nofail
#undef write_type_nofail

void config_file_delete(struct config_section *section)
{
	const char *path = file_get(section->name);
	unlink(path);
}

int validate_section(const char *section)
{
	if(!section)
		return 0;
	if(strchr(section, '/'))
		return 0;
	// if only '.' or empty string then error else success
	const char *ptr;
	for(ptr = section; *ptr; ++ptr)
	{
		if(*ptr != '.')
			return 1;
	}
	return 0;
}

int validate_name(const char *name)
{
	if(!name)
		return 0;
	// if empty string then error else success
	return *name != '\0';
}

struct config_section *get_section(const char *section, int create)
{
	struct config_section *sec;

	struct node_lookup_data data;
	data.name = section;
	data.result = NULL;
	list_foreach(&sections, section_find_item, &data);
	sec = data.result;

	if(!sec && create)
	{
		malloc_nofail(sec);
		sec->name = strdup(section);
		list_init(&sec->entries);
		list_add(&sections, sec);
	}

	return sec;
}

struct config_entry *get_entry(struct config_section *section, const char *name)
{
	struct node_lookup_data data;
	data.name = name;
	data.result = NULL;
	list_foreach(&(section->entries), entry_find_item, &data);
	return data.result;
}

int section_find_item(void *node, void *ctx)
{
	struct config_section *sec = node;
	struct node_lookup_data *data = ctx;

	if(!strcmp(sec->name, data->name))
	{
		data->result = sec;
		return 0; // we found, break
	}

	return 1;
}

int entry_find_item(void *node, void *ctx)
{
	struct config_entry *entry = node;
	struct node_lookup_data *data = ctx;

	if(!strcmp(entry->entry.name, data->name))
	{
		data->result = entry;
		return 0; // we found, break
	}

	return 1;
}

struct config_entry *read_entry(const char *section_name, const char *entry_name, enum config_type requested_type)
{
	if(!validate_section(section_name))
		return NULL;
	if(!validate_name(entry_name))
		return NULL;

	struct config_section *section = get_section(section_name, 0);
	if(!section)
		return NULL;

	struct config_entry *entry = get_entry(section, entry_name);
	if(!entry)
		return NULL;

	if(entry->entry.type != requested_type)
		return NULL;
	return entry;
}

struct config_entry *prepare_write_entry(const char *section_name, const char *entry_name, enum config_type requested_type, struct config_section **sectionref)
{
	if(!validate_section(section_name))
		return NULL;
	if(!validate_name(entry_name))
		return NULL;

	struct config_section *section = get_section(section_name, 1);
	if(sectionref)
		*sectionref = section;

	struct config_entry *entry = get_entry(section, entry_name);
	if(entry)
	{
		if(entry->entry.type != requested_type)
			return NULL;
		return entry;
	}

	malloc_nofail(entry);
	memset(entry, 0, sizeof(*entry)); // default data
	strdup_nofail(entry->entry.name, entry_name);
	entry->entry.type = requested_type;
	list_add(&(section->entries), entry);
	return entry;
}

void section_free(void *node, void *ctx)
{
	struct config_section *sec = node;

	// clear entries
	list_clear(&(sec->entries), entry_free, NULL);

	// clear section
	free(sec->name);
	free(sec);
}

void entry_free(void *node, void *ctx)
{
	struct config_entry *entry = node;
	void *bufptr;

	switch(entry->entry.type)
	{
		case CHAR:
		case INT:
		case INT64:
			bufptr = NULL;
			break;

		case STRING:
			bufptr = entry->entry.data.string_value;
			break;

		case BUFFER:
			bufptr = entry->entry.data.buffer_value.data;
			break;

		case CHAR_ARRAY:
			bufptr = entry->entry.data.char_array_value.array;
			break;

		case INT_ARRAY:
			bufptr = entry->entry.data.int_array_value.array;
			break;

		case INT64_ARRAY:
			bufptr = entry->entry.data.int64_array_value.array;
			break;

		case STRING_ARRAY:
			bufptr = entry->entry.data.string_array_value.buffer;
			break;
	}

	if(bufptr)
		free(bufptr);
	free(entry->entry.name);
	free(entry);
}

int config_read_char(const char *section, const char *name, char *value)
{
	if(!value)
		return 0;

	struct config_entry *entry = read_entry(section, name, CHAR);
	if(!entry)
		return 0;

	*value = entry->entry.data.char_value;
	return 1;
}

int config_read_int(const char *section, const char *name, int *value)
{
	if(!value)
		return 0;

	struct config_entry *entry = read_entry(section, name, INT);
	if(!entry)
		return 0;

	*value = entry->entry.data.int_value;
	return 1;
}

int config_read_int64(const char *section, const char *name, long long *value)
{
	if(!value)
		return 0;

	struct config_entry *entry = read_entry(section, name, INT64);
	if(!entry)
		return 0;

	*value = entry->entry.data.int64_value;
	return 1;
}

int config_read_string(const char *section, const char *name, char **value) // value allocated, free it after usage
{
	if(!value)
		return 0;

	struct config_entry *entry = read_entry(section, name, STRING);
	if(!entry)
		return 0;

	char *string_value = entry->entry.data.string_value;
	*value = NULL;
	if(string_value)
	{
		strdup_nofail(*value, string_value);
	}
	return 1;
}

int config_read_buffer(const char *section, const char *name, void **buffer, size_t *buffer_len) // buffer allocated, free it after usage
{
	if(!buffer || !buffer_len)
		return 0;

	struct config_entry *entry = read_entry(section, name, BUFFER);
	if(!entry)
		return 0;

	size_t len;
	void *buf;

	len = entry->entry.data.buffer_value.len;
	buf = NULL;
	if(len > 0)
	{
		log_assert(buf = malloc(len));
		memcpy(buf, entry->entry.data.buffer_value.data, len);
	}

	*buffer_len = len;
	*buffer = buf;

	return 1;
}

int config_read_char_array(const char *section, const char *name, size_t *array_len, char **value) // array allocated, free it after range
{
	if(!array_len || !value)
		return 0;

	struct config_entry *entry = read_entry(section, name, CHAR_ARRAY);
	if(!entry)
		return 0;

	size_t count = entry->entry.data.char_array_value.count;
	char *val = NULL;
	if(count > 0)
	{
		malloc_array_nofail(val, count);
		memcpy(val, entry->entry.data.char_array_value.array, count * sizeof(*val));
	}

	*array_len = count;
	*value = val;

	return 1;
}

int config_read_int_array(const char *section, const char *name, size_t *array_len, int **value) // array allocated, free it after range
{
	if(!array_len || !value)
		return 0;

	struct config_entry *entry = read_entry(section, name, INT_ARRAY);
	if(!entry)
		return 0;

	size_t count = entry->entry.data.int_array_value.count;
	int *val = NULL;
	if(count > 0)
	{
		malloc_array_nofail(val, count);
		memcpy(val, entry->entry.data.int_array_value.array, count * sizeof(*val));
	}

	*array_len = count;
	*value = val;

	return 1;
}

int config_read_int64_array(const char *section, const char *name, size_t *array_len, long long **value) // array allocated, free it after range
{
	if(!array_len || !value)
		return 0;

	struct config_entry *entry = read_entry(section, name, INT_ARRAY);
	if(!entry)
		return 0;

	size_t count = entry->entry.data.int64_array_value.count;
	long long *val = NULL;
	if(count > 0)
	{
		malloc_array_nofail(val, count);
		memcpy(val, entry->entry.data.int64_array_value.array, count * sizeof(*val));
	}

	*array_len = count;
	*value = val;

	return 1;
}

int config_read_string_array(const char *section, const char *name, size_t *array_len, char ***value) // value allocated, free it after usage (1 buffer)
{
	if(!array_len || !value)
		return 0;

	struct config_entry *entry = read_entry(section, name, STRING_ARRAY);
	if(!entry)
		return 0;

	size_t count = entry->entry.data.string_array_value.count;
	char **val = NULL;
	if(count > 0)
	{
		const char *srcbuf = entry->entry.data.string_array_value.buffer;
		size_t bufflen = config_file_string_array_len(srcbuf, count);
		bufflen -= (count / CHAR_BIT); // remove bit array prefix
		bufflen += sizeof(char*) * count;// add char ** prefix
		const char *src = srcbuf + (count / CHAR_BIT); // add prefix to source to begin at strings

		log_assert(val = malloc(bufflen));

		char *str = (char*)(val + count + 1);
		for(size_t i=0; i<count; i++)
		{
			char isvalue = read_bit(srcbuf, i);
			if(isvalue)
			{
				// read string
				val[i] = str;
				while((*++str = *++src));
			}
			else
			{
				val[i] = NULL;
			}
		}
	}

	*array_len = count;
	*value = val;

	return 1;
}

int config_write_char(const char *section, const char *name, char value)
{
	struct config_section *sec;
	struct config_entry *entry = prepare_write_entry(section, name, CHAR, &sec);
	if(!entry)
		return 0;

	entry->entry.data.char_value = value;

	config_file_save(sec);
	return 1;
}

int config_write_int(const char *section, const char *name, int value)
{
	struct config_section *sec;
	struct config_entry *entry = prepare_write_entry(section, name, INT, &sec);
	if(!entry)
		return 0;

	entry->entry.data.int_value = value;

	config_file_save(sec);
	return 1;
}

int config_write_int64(const char *section, const char *name, long long value)
{
	struct config_section *sec;
	struct config_entry *entry = prepare_write_entry(section, name, INT64, &sec);
	if(!entry)
		return 0;

	entry->entry.data.int64_value = value;

	config_file_save(sec);
	return 1;
}

int config_write_string(const char *section, const char *name, const char *value)
{
	struct config_section *sec;
	struct config_entry *entry = prepare_write_entry(section, name, STRING, &sec);
	if(!entry)
		return 0;

	if(entry->entry.data.string_value)
		free(entry->entry.data.string_value); // clean old value

	entry->entry.data.string_value = NULL;
	if(value)
	{
		strdup_nofail(entry->entry.data.string_value, value);
	}

	config_file_save(sec);
	return 1;
}

int config_write_buffer(const char *section, const char *name, const void *buffer, size_t buffer_len)
{
	if(buffer_len && !buffer)
		return 0;

	struct config_section *sec;
	struct config_entry *entry = prepare_write_entry(section, name, BUFFER, &sec);
	if(!entry)
		return 0;

	if(entry->entry.data.buffer_value.data)
		free(entry->entry.data.buffer_value.data); // clean old value

	void *bufcopy = NULL;
	if(buffer_len)
	{
		log_assert(bufcopy = malloc(buffer_len));
		memcpy(bufcopy, buffer, buffer_len);
	}
	entry->entry.data.buffer_value.data = bufcopy;
	entry->entry.data.buffer_value.len = buffer_len;

	config_file_save(sec);
	return 1;
}

int config_write_char_array(const char *section, const char *name, size_t array_len, const char *value)
{
	if(array_len && !value)
		return 0;

	struct config_section *sec;
	struct config_entry *entry = prepare_write_entry(section, name, CHAR_ARRAY, &sec);
	if(!entry)
		return 0;

	if(entry->entry.data.char_array_value.array)
		free(entry->entry.data.char_array_value.array); // clean old value

	char *array = NULL;
	if(array_len)
	{
		malloc_array_nofail(array, array_len);
		memcpy(array, value, array_len * sizeof(*array));
	}
	entry->entry.data.char_array_value.array = array;
	entry->entry.data.char_array_value.count = array_len;

	config_file_save(sec);
	return 1;
}

int config_write_int_array(const char *section, const char *name, size_t array_len, const int *value)
{
	if(array_len && !value)
		return 0;

	struct config_section *sec;
	struct config_entry *entry = prepare_write_entry(section, name, INT_ARRAY, &sec);
	if(!entry)
		return 0;

	if(entry->entry.data.int_array_value.array)
		free(entry->entry.data.int_array_value.array); // clean old value

	int *array = NULL;
	if(array_len)
	{
		malloc_array_nofail(array, array_len);
		memcpy(array, value, array_len * sizeof(*array));
	}
	entry->entry.data.int_array_value.array = array;
	entry->entry.data.int_array_value.count = array_len;

	config_file_save(sec);
	return 1;
}

int config_write_int64_array(const char *section, const char *name, size_t array_len, const long long *value)
{
	if(array_len && !value)
		return 0;

	struct config_section *sec;
	struct config_entry *entry = prepare_write_entry(section, name, INT64_ARRAY, &sec);
	if(!entry)
		return 0;

	if(entry->entry.data.int64_array_value.array)
		free(entry->entry.data.int64_array_value.array); // clean old value

	long long *array = NULL;
	if(array_len)
	{
		malloc_array_nofail(array, array_len);
		memcpy(array, value, array_len * sizeof(*array));
	}
	entry->entry.data.int64_array_value.array = array;
	entry->entry.data.int64_array_value.count = array_len;

	config_file_save(sec);
	return 1;
}

int config_write_string_array(const char *section, const char *name, size_t array_len, const char **value)
{
	if(array_len && !value)
		return 0;

	struct config_section *sec;
	struct config_entry *entry = prepare_write_entry(section, name, STRING_ARRAY, &sec);
	if(!entry)
		return 0;

	if(entry->entry.data.string_array_value.buffer)
		free(entry->entry.data.string_array_value.buffer); // clean old value

	void *buf = NULL;
	if(array_len)
	{
		size_t bufflen = array_len / CHAR_BIT;
		for(size_t i=0; i<array_len; i++)
		{
			if(!value[i])
				continue;
			bufflen += strlen(value[i]) + 1;
		}

		log_assert(buf = malloc(bufflen));

		char *str = (char*)buf + (array_len / CHAR_BIT);
		for(size_t i=0; i<array_len; i++)
		{
			const char *src = value[i];
			char isvalue = src ? 1 : 0;
			write_bit(buf, i, isvalue);
			if(!isvalue)
				continue;
			while((*++str = *++src));
		}
	}

	entry->entry.data.string_array_value.buffer = buf;
	entry->entry.data.string_array_value.count = array_len;

	config_file_save(sec);
	return 1;
}

int config_delete_entry(const char *section, const char *name) // 1 if success 0 if error
{
	if(!validate_section(section))
		return 0;
	if(!validate_name(name))
		return 0;

	struct config_section *sec = get_section(section, 0);
	if(!sec)
		return 0;

	struct config_entry *entry = get_entry(sec, name);
	if(!entry)
		return 0;

	list_remove(&(sec->entries), entry);
	entry_free(entry, NULL);

	config_file_save(sec);
	return 1;
}

int config_delete_section(const char *section) // 1 if success 0 if error
{
	if(!validate_section(section))
		return 0;

	struct config_section *sec = get_section(section, 0);
	if(!sec)
		return 0;

	// remove from list
	list_remove(&sections, sec);

	// clear file
	config_file_delete(sec);

	section_free(sec, NULL);

	return 1;
}

void config_enum_sections(int (*callback)(const char *section, void *ctx), void *ctx)
{
	struct enum_section_data data;
	data.callback = callback;
	data.ctx = ctx;
	list_foreach(&sections, config_enum_section_item, &data);
}

int config_enum_section_item(void *node, void *ctx)
{
	struct enum_section_data *data = ctx;
	struct config_section *section = node;

	return data->callback(section->name, data->ctx);
}

int config_enum_entries(const char *section, int (*callback)(const char *name, void *ctx), void *ctx) // 1 if success 0 if error
{
	if(!validate_section(section))
		return 0;

	struct config_section *sec = get_section(section, 0);
	if(!sec)
		return 0;

	struct enum_entry_data data;
	data.callback = callback;
	data.ctx = ctx;
	list_foreach(&(sec->entries), config_enum_entry_item, &data);

	return 1;
}

int config_enum_entry_item(void *node, void *ctx)
{
	struct enum_entry_data *data = ctx;
	struct config_entry *entry = node;

	return data->callback(entry->entry.name, data->ctx);
}

int config_get_entry_type(const char *section, const char *name, enum config_type *type) // 1 if success 0 if error
{
	if(!validate_section(section))
		return 0;
	if(!validate_name(name))
		return 0;
	if(!type)
		return 0;

	struct config_section *sec = get_section(section, 0);
	if(!sec)
		return 0;

	struct config_entry *entry = get_entry(sec, name);
	if(!entry)
		return 0;

	*type = entry->entry.type;
	return 1;
}
