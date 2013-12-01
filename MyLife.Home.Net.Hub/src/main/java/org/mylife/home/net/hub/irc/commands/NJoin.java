/*
 * jIRCd - Java Internet Relay Chat Daemon
 * Copyright 2003 Tyrel L. Haveman <tyrel@haveman.net>
 *
 * This file is part of jIRCd.
 *
 * jIRCd is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * jIRCd is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with jIRCd; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.Channel;
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Network;
import org.mylife.home.net.hub.irc.RegisteredEntity;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class NJoin implements Command {
	public void invoke(RegisteredEntity src, String[] params) {
		if (!(src instanceof Server))
			return;

		Server server = (Server) src;
		Network network = server.getNetwork();
		final String channame = params[0];
		if (!Util.isChannelIdentifier(channame)) {
			Util.sendNoSuchChannelError(src, channame);
			return;
		}

		// Transmission du NJOIN aux autres peers
		Message message = new Message(src, "NJOIN");
		message.appendParameter(params[0]);
		message.appendParameter(params[1]);
		// Envoi du message sauf à nous meme et a la source
		network.send(message, network.getThisServer(), server);

		Channel chan = network.getChannel(channame);
		if (chan == null) {
			chan = new Channel(channame, network);
		}

		String members[] = Util.split(params[1], ',');
		for (int i = 0; i < members.length; i++)
			addUser(network, chan, members[i]);
	}

	private void addUser(Network network, Channel chan, String nick) {
		boolean op = false;
		boolean voice = false;
		if (nick.startsWith("@@")) {
			nick = nick.substring(2);
			op = true;
		} else if (nick.charAt(0) == '@') {
			nick = nick.substring(1);
			op = true;
		} else if (nick.charAt(0) == '+') {
			nick = nick.substring(1);
			voice = true;
		}

		User user = network.getUser(nick);
		// ajout de l'utilisateur
		chan.addUser(user);
		// ajout des modes
		if(op)
			chan.setOp(user, true);
		if(voice)
			chan.setVoice(user, true);

		// transmissions aux utilisateurs locaux
		Message message = new Message(user, "JOIN", chan);
		chan.sendLocal(message);
		if(op) {
			Message modeMessage = new Message(network.getThisServer(), "MODE", chan);
			modeMessage.appendParameter("+o");
			modeMessage.appendParameter(user.getNick());
			chan.sendLocal(modeMessage);
		}
		if(voice) {
			Message modeMessage = new Message(network.getThisServer(), "MODE", chan);
			modeMessage.appendParameter("+v");
			modeMessage.appendParameter(user.getNick());
			chan.sendLocal(modeMessage);
		}
	}

	public String getName() {
		return "NJOIN";
	}

	public int getMinimumParameterCount() {
		return 2;
	}
}
