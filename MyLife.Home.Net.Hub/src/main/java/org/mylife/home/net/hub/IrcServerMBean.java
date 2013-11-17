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

package org.mylife.home.net.hub;

import java.io.IOException;
import java.util.Set;

import org.mylife.home.net.hub.configuration.IrcConfiguration;
import org.mylife.home.net.hub.configuration.IrcLinkAccept;
import org.mylife.home.net.hub.configuration.IrcLinkConnect;
import org.mylife.home.net.hub.irc.CommandContext;
import org.mylife.home.net.hub.irc.ConnectionManager;
import org.mylife.home.net.hub.irc.Listener;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Operator;
import org.mylife.home.net.hub.irc.Server;

/**
 * JMX management interface.
 * 
 * @author markhale
 */
public interface IrcServerMBean {
	long getUptimeMillis();

	long getStartTimeMillis();

	String getVersion();

	int getVisibleUserCount();

	int getInvisibleUserCount();

	int getNetworkVisibleUserCount();

	int getNetworkInvisibleUserCount();

	int getChannelCount();

	int getServerCount();

	Server getServer();

	IrcConfiguration getConfiguration();

	String getHostName();

	void reloadConfiguration() throws IOException;

	void reloadPolicy();

	void start();

	void stop();

	void invokeCommand(Message message);

	CommandContext getCommandContext(String name);

	Set<CommandContext> getCommandContexts();

	Set<Listener> getListeners();

	ConnectionManager getLinks();

	IrcLinkAccept findLinkAccept(String remoteAddress, int localPort);

	IrcLinkConnect findLinkConnect(String remoteAddress, int remotePort);

	Set<Operator> getOperators();
}
