/*
 * export.c
 *
 *  Created on: Jan 18, 2014
 *      Author: vincent
 */

#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <linux/limits.h>
#include <unistd.h>
#include <sys/stat.h>

#include <microhttpd.h>
#include <jansson.h>

#include "export.h"
#include "logger.h"
#include "loop.h"
#include "config.h"
#include "net.h"

#define PORT 8888

static void select_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
static void select_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
static int on_client_connect(void *cls, const struct sockaddr *addr, socklen_t addrlen);
static int request_handler(void *cls, struct MHD_Connection *connection,
		const char *url, const char *method, const char *version,
		const char *upload_data, size_t *upload_data_size, void **con_cls);
static const char *version_binary();
static const char *version_config();
static char *create_content();
static int create_content_container(struct net_container *container, void *ctx);
static int create_content_member(struct net_member *member, void *ctx);
static int create_content_argument(struct net_type *type, void *ctx);
static json_t *create_content_type(struct net_type *type);
static int create_content_type_enum_value(const char *value, void *ctx);

static struct MHD_Daemon *instance;
static struct loop_handle *loop_handle;
static char *content;

void export_init()
{
	log_assert((instance = MHD_start_daemon(MHD_NO_FLAG, PORT, on_client_connect, NULL,
			&request_handler, NULL, MHD_OPTION_END)));

	log_assert((loop_handle = loop_register_listener(select_add, select_process, NULL)));

	content = NULL;
}

void export_terminate()
{
	loop_unregister(loop_handle);
	MHD_stop_daemon(instance);
	free(content);
}

void select_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	log_assert(MHD_get_fdset(instance, readfds, writefds, exceptfds, nfds) == MHD_YES);
}

void select_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	log_assert(MHD_run(instance) == MHD_YES);
}

int on_client_connect(void *cls, const struct sockaddr *addr, socklen_t addrlen)
{
	log_debug("connection from : %s", inet_ntoa(((const struct sockaddr_in *)addr)->sin_addr));

	return 1;
}

int request_handler(void *cls, struct MHD_Connection *connection,
		const char *url, const char *method, const char *version,
		const char *upload_data, size_t *upload_data_size, void **con_cls)
{
	if(!content)
		content = create_content();

	struct MHD_Response *response;
	int ret;

	response = MHD_create_response_from_buffer(strlen(content), (void*) content, MHD_RESPMEM_PERSISTENT);
	ret = MHD_queue_response(connection, MHD_HTTP_OK, response);
	MHD_destroy_response(response);

	return ret;
}

const char *version_binary()
{
	static char path[PATH_MAX];
	log_assert(readlink("/proc/self/exe", path, PATH_MAX) != -1);

    struct stat statbuf;
    log_assert(stat(path, &statbuf) != -1);
    time_t lastupdate = statbuf.st_mtime;

    static char buff[20];
    strftime(buff, 20, "%Y-%m-%d %H:%M:%S", localtime(&lastupdate));
	return buff;
}

const char *version_config()
{
	time_t lastupdate = conf_lastupdate();

    static char buff[20];
    strftime(buff, 20, "%Y-%m-%d %H:%M:%S", localtime(&lastupdate));
	return buff;
}

char *create_content()
{
	json_t *root;
	log_assert((root = json_object()));

	json_t *components;
	log_assert((components = json_array()));
	net_repository_foreach(create_content_container, components);
	json_object_set_new(root, "components", components);

	json_t *version;
	log_assert((version = json_object()));
	json_object_set_new(version, "binary", json_string(version_binary()));
	json_object_set_new(version, "config", json_string(version_config()));
	json_object_set_new(root, "version", version);

	char *ret = json_dumps(root, JSON_PRESERVE_ORDER | JSON_INDENT(4));
	json_decref(root);
	return ret;
}

int create_content_container(struct net_container *container, void *ctx)
{
	json_t *components = ctx;

	if(!net_container_is_local(container))
		return 1;

	json_t *component;
	log_assert((component = json_object()));

	struct net_object *object = net_container_get_object(container);
	struct net_class *class = net_object_get_class(object);
	json_object_set_new(component, "id", json_string(net_object_get_id(object)));

	json_t *members;
	log_assert((members = json_array()));
	net_class_enum_members(class, create_content_member, members);

	json_t *jclass;
	log_assert((jclass = json_object()));
	json_object_set_new(jclass, "members", members);
	json_object_set_new(component, "class", jclass);

	json_array_append_new(components, component);
	return 1;
}

int create_content_member(struct net_member *member, void *ctx)
{
	json_t *members = ctx;

	json_t *jmember;
	json_t *arguments;
	log_assert((jmember = json_object()));

	int type = net_member_get_type(member);
	const char *stype = NULL;
	switch(type)
	{
	case NET_MEMBER_ACTION:
		stype = "action";
		break;
	case NET_MEMBER_ATTRIBUTE:
		stype = "attribute";
		break;
	}

	json_object_set_new(jmember, "index", json_integer(net_member_get_order(member)));
	json_object_set_new(jmember, "name", json_string(net_member_get_name(member)));
	json_object_set_new(jmember, "membertype", json_string(stype));

	switch(type)
	{
	case NET_MEMBER_ACTION:
		log_assert((arguments = json_array()));
		net_member_enum_arguments(member, create_content_argument, arguments);
		json_object_set_new(jmember, "arguments", arguments);
		break;
	case NET_MEMBER_ATTRIBUTE:
		json_object_set_new(jmember, "type", create_content_type(net_member_get_argument(member)));
		break;
	}

	json_array_append_new(members, jmember);
	return 1;
}

int create_content_argument(struct net_type *type, void *ctx)
{
	json_t *arguments = ctx;
	json_array_append_new(arguments, create_content_type(type));
	return 1;
}

json_t *create_content_type(struct net_type *type)
{
	json_t *jtype;
	json_t *enum_array;
	log_assert((jtype = json_object()));

	switch(net_type_get_type(type))
	{
	case NET_TYPE_ENUM:
		json_object_set_new(jtype, "type", json_string("enum"));
		log_assert((enum_array = json_array()));
		net_type_enum_foreach(type, create_content_type_enum_value, enum_array);
		json_object_set_new(jtype, "values", enum_array);
		break;

	case NET_TYPE_RANGE:
		json_object_set_new(jtype, "type", json_string("range"));
		json_object_set_new(jtype, "min", json_integer(net_type_get_range_min(type)));
		json_object_set_new(jtype, "max", json_integer(net_type_get_range_max(type)));
		break;
	}

	return jtype;
}

int create_content_type_enum_value(const char *value, void *ctx)
{
	json_t *enum_array = ctx;
	json_array_append_new(enum_array, json_string(value));
	return 1;
}
