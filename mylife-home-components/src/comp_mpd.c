/*
 * comp_mpd.c
 *
 *  Created on: Jan 19, 2014
 *      Author: vincent
 */

#include <stddef.h>
#include <stdio.h>
#include <sys/select.h>
#include <stdarg.h>
#include <limits.h>
#include <string.h>
#include <libconfig.h>
#include <mpd/client.h>
#include <mpd/async.h>

#include "components.h"
#include "net.h"
#include "tools.h"
#include "logger.h"
#include "config.h"
#include "loop.h"

struct component
{
	struct net_object *object;
	struct net_container *container;

	struct mpd_connection *mpd_con;
	struct loop_handle *listener;

	enum mpd_state state;
	int volume;
};

static void *creator(const char *id, config_setting_t *config);
static void destructor(void *handle);

static struct component_type type =
{
	.name = "mpd",
	.creator = creator,
	.destructor = destructor
};

static const char *states[] =
{
	"unknown", //MPD_STATE_UNKNOWN = 0,
	"stop", //MPD_STATE_STOP = 1,
	"play", //MPD_STATE_PLAY = 2,
	"pause" //MPD_STATE_PAUSE = 3,
};

static struct net_class *net_class;
static struct net_type *net_type_volume;
static struct net_type *net_type_status;

static void read_status(struct component *comp);
static void change_status(struct component *comp, enum mpd_state state, int volume);
static void setstate_handler(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[]);
static void setvolume_handler(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[]);

static void listener_callback_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
static void listener_callback_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);

void comp_internal_mpd_init()
{
	net_type_volume = net_type_create_range(0, 100);
	net_type_status = net_type_create_enum(
			"unknown", //MPD_STATE_UNKNOWN = 0,
			"stop", //MPD_STATE_STOP = 1,
			"play", //MPD_STATE_PLAY = 2,
			"pause", //MPD_STATE_PAUSE = 3,
			NULL);
	net_class = net_class_create();
	net_class_create_attribute(net_class, "state", net_type_status);
	net_class_create_attribute(net_class, "volume", net_type_volume);
	net_class_create_action(net_class, "setstate", net_type_status, NULL);
	net_class_create_action(net_class, "setvolume", net_type_volume, NULL);

	component_register(&type);
}

void comp_internal_mpd_terminate()
{
	net_class_destroy(net_class);
	net_type_destroy(net_type_volume);
	net_type_destroy(net_type_status);
}

void *creator(const char *id, config_setting_t *config)
{
	struct component *comp;
	malloc_nofail(comp);

	const char *server_address = conf_get_string(config, "address");
	int server_port = conf_get_int(config, "port");

	log_assert((comp->object = net_object_create(net_class, id)));

	log_assert((comp->mpd_con = mpd_connection_new(server_address, server_port, 0)));
	log_assert(mpd_connection_get_error(comp->mpd_con) == MPD_ERROR_SUCCESS);
	log_assert((comp->listener = loop_register_listener(listener_callback_add, listener_callback_process, comp)));

	net_object_action_set_handler(comp->object, "setstate", setstate_handler, comp);
	net_object_action_set_handler(comp->object, "setvolume", setvolume_handler, comp);

	read_status(comp);
	log_assert(mpd_send_idle_mask(comp->mpd_con, MPD_IDLE_PLAYER | MPD_IDLE_MIXER));

	log_assert((comp->container = net_repository_register(comp->object, NET_CHANNEL_HARDWARE, 1)));

	return comp;
}

void destructor(void *handle)
{
	struct component *comp = handle;

	log_assert(mpd_send_noidle(comp->mpd_con));
	mpd_recv_idle(comp->mpd_con, 0);
	loop_unregister(comp->listener);
	net_repository_unregister(comp->container);
	net_object_destroy(comp->object);
	mpd_connection_free(comp->mpd_con);
	free(comp);
}

void read_status(struct component *comp)
{
	struct mpd_status *status;
	log_assert((status = mpd_run_status(comp->mpd_con)));
	int volume = mpd_status_get_volume(status);
	enum mpd_state state = mpd_status_get_state(status);
	mpd_status_free(status);

	change_status(comp, state, volume);
}

void change_status(struct component *comp, enum mpd_state state, int volume)
{
	struct net_value value;

	if(comp->state != state)
	{
		comp->state = state;
		value.enum_value = (char *)states[state]; // unmodified
		net_object_attribute_change(comp->object, "state", value);
	}

	if(comp->volume != volume)
	{
		comp->volume = volume;
		value.range_value = volume;
		net_object_attribute_change(comp->object, "volume", value);
	}
}

void setstate_handler(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[])
{
	struct component *comp = ctx;
	const char *state = args[0]->enum_value;
	enum mpd_state estate = 0;

	if(!strcmp(state, states[MPD_STATE_STOP]))
		estate = MPD_STATE_STOP;
	else if(!strcmp(state, states[MPD_STATE_PLAY]))
		estate = MPD_STATE_PLAY;
	else if(!strcmp(state, states[MPD_STATE_PAUSE]))
		estate = MPD_STATE_PAUSE;
	else
		log_fatal("unknown value : %s", state);

	log_assert(mpd_send_noidle(comp->mpd_con));
	mpd_recv_idle(comp->mpd_con, 0);
	switch(estate)
	{
	case MPD_STATE_STOP:
		log_assert(mpd_run_stop(comp->mpd_con));
		break;
	case MPD_STATE_PLAY:
		log_assert(mpd_run_play(comp->mpd_con));
		//log_assert(mpd_run_pause(comp->mpd_con, 0));
		break;
	case MPD_STATE_PAUSE:
		//log_assert(mpd_run_play(comp->mpd_con));
		log_assert(mpd_run_pause(comp->mpd_con, 1));
		break;
	default:
		log_fatal("unhandled switch value : %i", estate);
	}
	log_assert(mpd_send_idle_mask(comp->mpd_con, MPD_IDLE_PLAYER | MPD_IDLE_MIXER));

	change_status(comp, estate, comp->volume);
}

void setvolume_handler(void *ctx, struct net_object *object, struct net_member *member, struct net_value *args[])
{
	struct component *comp = ctx;
	int volume = args[0]->range_value;

	log_assert(mpd_send_noidle(comp->mpd_con));
	mpd_recv_idle(comp->mpd_con, 0);
	log_assert(mpd_run_set_volume(comp->mpd_con, volume));
	log_assert(mpd_send_idle_mask(comp->mpd_con, MPD_IDLE_PLAYER | MPD_IDLE_MIXER));

	change_status(comp, comp->state, volume);
}

void listener_callback_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	struct component *comp = ctx;
	struct mpd_async *async = mpd_connection_get_async(comp->mpd_con);
	int fd = mpd_async_get_fd(async);
	enum mpd_async_event events = mpd_async_events(async);

	if (events & MPD_ASYNC_EVENT_READ)
		FD_SET(fd, readfds);
	if (events & MPD_ASYNC_EVENT_WRITE)
		FD_SET(fd, writefds);
	if (events & (MPD_ASYNC_EVENT_HUP|MPD_ASYNC_EVENT_ERROR))
		FD_SET(fd, exceptfds);

	if(events > 0 && (fd+1) > *nfds)
		*nfds = fd + 1;
}

void listener_callback_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx)
{
	struct component *comp = ctx;
	struct mpd_async *async = mpd_connection_get_async(comp->mpd_con);
	int fd = mpd_async_get_fd(async);
	enum mpd_async_event events = 0;

	if (FD_ISSET(fd, readfds))
		events |= MPD_ASYNC_EVENT_READ;
	if (FD_ISSET(fd, writefds))
		events |= MPD_ASYNC_EVENT_WRITE;
	if (FD_ISSET(fd, exceptfds))
		events |= (MPD_ASYNC_EVENT_HUP | MPD_ASYNC_EVENT_ERROR);

	if(events)
	{
		mpd_async_io(async, events);

		// io arrive, ca doit etre un statut on le lit
		enum mpd_idle idle = mpd_recv_idle(comp->mpd_con, 0);
		if(idle & (MPD_IDLE_PLAYER | MPD_IDLE_MIXER))
			read_status(comp);

		log_assert(mpd_send_idle_mask(comp->mpd_con, MPD_IDLE_PLAYER | MPD_IDLE_MIXER));
	}
}

