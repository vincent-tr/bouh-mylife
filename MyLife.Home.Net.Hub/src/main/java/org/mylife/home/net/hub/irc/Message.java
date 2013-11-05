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

package org.mylife.home.net.hub.irc;

/**
 * IRC message format.
 * A message consists of three parts: a sender, a command, and zero or more parameters.
 * @author markhale
 */
public class Message {
	protected final String from;
	protected final String command;
	protected final String[] params = new String[Constants.MAX_MESSAGE_PARAMETERS];
	protected int paramCount = 0;
	protected boolean hasLast = false;

	public Message(String from, String command) {
		this.from = from;
		this.command = command;
	}
	public Message(String command) {
		this.from = null;
		this.command = command;
	}
	/**
	 * Creates a message sent from a server.
	 * @param from can be null
	 */
	public Message(Server from, String command) {
		this(from != null ? from.getName() : null, command);
	}
	/**
	 * Creates a message sent from a user.
	 * @param from can be null
	 */
	public Message(User from, String command) {
		this(from != null ? from.toString() : null, command);
	}
	/**
	 * Constructs a "numeric reply" type of message.
	 */
	public Message(User from, String command, Source target) {
		this(from, command);
		appendParameter(target.getNick());
	}
	public Message(Server from, String command, Source target) {
		this(from, command);
		appendParameter(target.getNick());
	}
	/**
	 * Creates a message sent from a user to a channel.
	 */
	public Message(User from, String command, Channel target) {
		this(from, command);
		appendParameter(target.getName());
	}
	/**
	 * Creates a message sent from a server to a channel.
	 */
	public Message(Server from, String command, Channel target) {
		this(from, command);
		appendParameter(target.getName());
	}
	/**
	 * Constructs a "numeric reply" type of message.
	 */
	public Message(String command, Source target) {
		this(target.getServer(), command, target);
	}
	/**
	 * Returns the sender of this message.
	 */
	public String getSender() {
		return from;
	}
	/**
	 * Attempts to resolve the sender as a Source.
	 * @return null if the sender cannot be resolved.
	 */
	public Source resolveSender(Network network) {
		if(from != null) {
			Source src;
			final int pos = from.indexOf('!');
			if (pos < 0) { // can't find *!*, so must be server
				src = network.getServer(from);
			} else { // found a *!* in string, must be a user
				String userName = from.substring(0, pos);
				src = network.getUser(userName);
			}
			return src;
		} else
			return null;
	}
	/**
	 * Returns the command of this message.
	 */
	public String getCommand() {
		return command;
	}
	public void appendParameter(String param) {
		if(hasLast)
			throw new IllegalStateException("The last parameter has already been appended");
		if(param != null && param.length() > 0) {
			params[paramCount] = param;
			paramCount++;
		}
	}
	/**
	 * Appends a parameter forcefully prefixed with a colon.
	 */
	public void appendLastParameter(String param) {
		if(param != null) {
			params[paramCount] = param;
			paramCount++;
			hasLast = true;
		}
	}
	public String getParameter(int n) {
		return params[n];
	}
	public int getParameterCount() {
		return paramCount;
	}
	public String toString() {
		StringBuffer buf = new StringBuffer();
		// append prefix
		if(from != null)
			buf.append(':').append(from).append(' ');

		// append command
		buf.append(command);

		// append parameters
		if(paramCount > 0) {
			final int lastParamIndex = paramCount - 1;
			for(int i=0; i<lastParamIndex; i++)
				buf.append(' ').append(params[i]);
			final String lastParam = params[lastParamIndex];
			// if the last parameter contains spaces or starts with a ':'
			if(hasLast || lastParam.indexOf(' ') != -1 || lastParam.charAt(0) == ':')
				buf.append(" :").append(lastParam);
			else
				buf.append(' ').append(lastParam);
		}
		return buf.toString();
	}
}
