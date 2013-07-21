/*
 * component.c
 *
 *  Created on: 21 juil. 2013
 *      Author: pumbawoman
 */


#define _BSD_SOURCE
#include <stddef.h>
#include <stdio.h>
#include <sys/select.h>
#include <stdarg.h>
#include <limits.h>
#include <string.h>

#include <mpd/client.h>
#include <mpd/async.h>

#include "core_api.h"
#include "tools.h"
#include "component.h"

struct component
{
	char *id;
	struct irc_bot *bot;

	struct mpd_connection *mpd_con;
	struct loop_handle *listener;

	enum mpd_state state;
	int volume;
};

static const char *ctype_bot = "mpd";

static const char *states[] =
{
	"unknown", //MPD_STATE_UNKNOWN = 0,
	"stop", //MPD_STATE_STOP = 1,
	"play", //MPD_STATE_PLAY = 2,
	"pause" //MPD_STATE_PAUSE = 3,
};

static struct irc_bot_callbacks callbacks =
{
	.on_connected = NULL,
	.on_disconnected = NULL,
	.on_comp_new = NULL,
	.on_comp_delete = NULL,
	.on_comp_change_status = NULL
};

static void read_status(struct component *comp);
static void change_status(struct component *comp, enum mpd_state state, int volume);
static void setstate_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);
static void setvolume_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx);

static void listener_callback_add(int *nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);
static void listener_callback_process(fd_set *readfds, fd_set *writefds, fd_set *exceptfds, void *ctx);

static char *cmd_setstate_desc[] =
{
	"set the state, args : [play, stop, pause]",
	NULL
};

static struct irc_command_description cmd_setstate =
{
	.verb = "setstate",
	.description = cmd_setstate_desc,
	.children = NULL,
	.callback = setstate_handler,
	.ctx = NULL
};

static char *cmd_setvolume_desc[] =
{
	"set the volume, args : volume (0 .. 100)",
	NULL
};

static struct irc_command_description cmd_setvolume =
{
	.verb = "setvolume",
	.description = cmd_setvolume_desc,
	.children = NULL,
	.callback = setvolume_handler,
	.ctx = NULL
};

void component_init()
{

}

void component_terminate()
{

}

struct component *component_create(const char *id, const char *server_address)
{
	if(!id)
		return error_failed_ptr(ERROR_CORE_INVAL);

	struct component *comp;
	malloc_nofail(comp);
	strdup_nofail(comp->id, id);
	log_assert((comp->mpd_con = mpd_connection_new(server_address, 0, 0)));
	log_assert(mpd_connection_get_error(comp->mpd_con) == MPD_ERROR_SUCCESS);
	log_assert(comp->listener = loop_register_listener(listener_callback_add, listener_callback_process, comp));

	log_assert(comp->bot = irc_bot_create(comp->id, ctype_bot, &callbacks, comp));
	log_assert(irc_bot_add_message_handler(comp->bot, 0, &cmd_setstate));
	log_assert(irc_bot_add_message_handler(comp->bot, 0, &cmd_setvolume));

	read_status(comp);
	log_assert(mpd_send_idle_mask(comp->mpd_con, MPD_IDLE_PLAYER | MPD_IDLE_MIXER));

	error_success();
	return comp;
}

void component_delete(struct component *comp, int delete_config)
{
	log_assert(mpd_send_noidle(comp->mpd_con));
	mpd_recv_idle(comp->mpd_con, 0);
	loop_unregister(comp->listener);
	irc_bot_delete(comp->bot);
	mpd_connection_free(comp->mpd_con);

	free(comp->id);
	free(comp);
}

const char *component_get_id(struct component *comp)
{
	return comp->id;
}

void read_status(struct component *comp)
{
	struct mpd_status *status;
	log_assert(status = mpd_run_status(comp->mpd_con));
	int volume = mpd_status_get_volume(status);
	enum mpd_state state = mpd_status_get_state(status);
	mpd_status_free(status);

	change_status(comp, state, volume);
}

void change_status(struct component *comp, enum mpd_state state, int volume)
{
	char status[20];

	comp->state = state;
	comp->volume = volume;

	snprintf(status, 20, "%s-%03d", states[comp->state], comp->volume);
	irc_bot_set_comp_status(comp->bot, status);
}

void setstate_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *state;
	enum mpd_state estate = 0;
	error_last = ERROR_SUCCESS;

	if(!irc_bot_read_parameters(bot, from, args, argc, &state))
		return;

	if(!strcasecmp(state, states[MPD_STATE_STOP]))
		estate = MPD_STATE_STOP;
	else if(!strcasecmp(state, states[MPD_STATE_PLAY]))
		estate = MPD_STATE_PLAY;
	else if(!strcasecmp(state, states[MPD_STATE_PAUSE]))
		estate = MPD_STATE_PAUSE;
	else
	{
		error_last = ERROR_CORE_INVAL;
		irc_bot_send_reply_from_error(bot, from, "setstate");
		return;
	}

	struct component *comp = irc_bot_get_ctx(bot);

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
	irc_bot_send_reply_from_error(bot, from, "setvalue");
}

void setvolume_handler(struct irc_bot *bot, struct irc_component *from, int is_broadcast, const char **args, int argc, void *ctx)
{
	const char *volume;
	int ivolume;
	error_last = ERROR_SUCCESS;

	if(!irc_bot_read_parameters(bot, from, args, argc, &volume))
		return;

	if(sscanf(volume, "%d", &ivolume) != 1)
	{
		error_last = ERROR_CORE_INVAL;
		irc_bot_send_reply_from_error(bot, from, "setvolume");
		return;
	}

	if(ivolume < 0 || ivolume > 100)
	{
		error_last = ERROR_CORE_INVAL;
		irc_bot_send_reply_from_error(bot, from, "setvolume");
		return;
	}

	struct component *comp = irc_bot_get_ctx(bot);

	log_assert(mpd_send_noidle(comp->mpd_con));
	mpd_recv_idle(comp->mpd_con, 0);
	log_assert(mpd_run_set_volume(comp->mpd_con, ivolume));
	log_assert(mpd_send_idle_mask(comp->mpd_con, MPD_IDLE_PLAYER | MPD_IDLE_MIXER));

	change_status(comp, comp->state, ivolume);
	irc_bot_send_reply_from_error(bot, from, "setvalue");

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

		// io arrivé, ca doit être un statut on le lit
		enum mpd_idle idle = mpd_recv_idle(comp->mpd_con, 0);
		if(idle & (MPD_IDLE_PLAYER | MPD_IDLE_MIXER))
			read_status(comp);

		log_assert(mpd_send_idle_mask(comp->mpd_con, MPD_IDLE_PLAYER | MPD_IDLE_MIXER));
	}
}

