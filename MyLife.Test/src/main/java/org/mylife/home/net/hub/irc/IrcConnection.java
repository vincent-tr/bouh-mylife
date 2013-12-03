package org.mylife.home.net.hub.irc;

import org.mylife.home.net.hub.irc.io.IOConnection;
import org.mylife.home.net.hub.irc.protocol.Parser;

/**
 * Représentation d'une connexion IRC
 * @author pumbawoman
 *
 */
public class IrcConnection {

	private final IOConnection connection;
	private final Parser parser;
}
