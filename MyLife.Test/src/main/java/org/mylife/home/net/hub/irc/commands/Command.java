package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
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
	 * @param src
	 * @param params
	 */
	void invoke(IrcConnection src, Message msg);

	/**
	 * Returns the command name, for example, PRIVMSG.
	 */
	String getName();
}
