/*
 * net.h
 *
 *  Created on: Jan 11, 2014
 *      Author: vincent
 */

#ifndef NET_H_
#define NET_H_

#define NET_TYPE_ENUM 1
#define NET_TYPE_RANGE 2

#define NET_MEMBER_ATTRIBUTE 1
#define NET_MEMBER_ACTION 2

#define NET_CHANNEL_HARDWARE	"mylife-home-hardware"
#define NET_CHANNEL_DEBUG		"mylife-home-debug"
#define NET_CHANNEL_UI			"mylife-home-ui"

struct net_type;
struct net_member;
struct net_class;
struct net_object;
struct net_container;

struct net_value
{
	union
	{
		int range_value;
		char *enum_value;
	};
};

typedef void (*net_callback)(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[]); // args, NULL terminated array

extern void net_init();
extern void net_terminate();

extern struct net_container *net_repository_register(struct net_object *object, const char *channel, int local);
extern void net_repository_unregister(struct net_container *container);
extern void net_repository_foreach(int (*callback)(struct net_container *container, void *ctx), void *ctx);
extern struct net_object *net_container_get_object(struct net_container *container);
extern const char *net_container_get_channel(struct net_container *container);
extern int net_container_is_local(struct net_container *container);
extern int net_container_is_connected(struct net_container *container);

extern struct net_type *net_type_create_enum(const char *first, ...); // NULL terminated
extern struct net_type *net_type_create_range(int min, int max);
extern void net_type_destroy(struct net_type *type);
extern int net_type_get_type(struct net_type *type);
extern unsigned int net_type_get_range_min(struct net_type *type);
extern unsigned int net_type_get_range_max(struct net_type *type);
extern void net_type_enum_foreach(struct net_type *type, int (*callback)(const char *enum_value, void *ctx), void *ctx);
extern int net_type_enum_exists(struct net_type *type, const char *value);

extern struct net_class *net_class_create();
extern void net_class_destroy(struct net_class *class);

// membre automatiquement détruit dans net_class_destroy
extern struct net_member *net_class_create_attribute(struct net_class *class, const char *name, struct net_type *type);
extern struct net_member *net_class_create_action(struct net_class *class, const char *name, ...); // liste de types, terminer par NULL
extern void net_class_enum_members(struct net_class *class, int (*callback)(struct net_member *member, void *ctx), void *ctx);
extern struct net_member *net_class_get_member_by_name(struct net_class *class, const char *name);
extern size_t net_class_get_members_count(struct net_class *class);
extern size_t net_class_get_attributes_count(struct net_class *class);
extern size_t net_class_get_actions_count(struct net_class *class);

extern int net_member_get_type(struct net_member *member);
extern const char *net_member_get_name(struct net_member *member);
extern unsigned int net_member_get_order(struct net_member *member);
extern struct net_type *net_member_get_argument(struct net_member *member);
extern void net_member_enum_arguments(struct net_member *member, int (*callback)(struct net_type *type, void *ctx), void *ctx);
extern size_t net_member_get_arguments_count(struct net_member *member);

extern struct net_object *net_object_create(struct net_class *class, const char *id);
extern void net_object_destroy(struct net_object *object);

extern struct net_class *net_object_get_class(struct net_object *object);
extern const char *net_object_get_id(struct net_object *object);

extern void net_object_attribute_change(struct net_object *object, const char *name, struct net_value value);
extern const struct net_value *net_object_attribute_get(struct net_object *object, const char *name);
extern void *net_object_attribute_listener_add(struct net_object *object, const char *name, net_callback callback, void *ctx);
extern void net_object_attribute_listener_remove(struct net_object *object, const char *name, void *handler);

extern void net_object_action_execute(struct net_object *object, const char *name, ...); // struct net_value, NULL terminated
extern void net_object_action_execute_array(struct net_object *object, const char *name, struct net_value *args[]); // args, NULL terminated array
extern void net_object_action_set_handler(struct net_object *object, const char *name, net_callback callback, void *ctx);

#endif /* NET_H_ */
