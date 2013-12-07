package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;

/**
 * Command IRC
 * 
 * @author pumbawoman
 * 
 */
public interface Command {

	/**
	 * Invokes this command
	 * 
	 * @param server
	 * @param src
	 * @param params
	 */
	void invoke(IrcServer server, IrcConnection src, Message msg);

	/**
	 * Returns the command name, for example, PRIVMSG.
	 */
	String getName();
}
