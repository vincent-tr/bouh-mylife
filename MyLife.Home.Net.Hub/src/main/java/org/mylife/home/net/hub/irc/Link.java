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

import org.mylife.home.net.hub.jIRCdMBean;

/**
 * An outbound socket connection to another server.
 * @author thaveman
 * @author markhale
 */
public class Link {
	protected final jIRCdMBean jircd;
	protected final Connection connection;
	protected MessageFactory messageFactory = new MessageFactory();
	protected Source server; // the server on the other end of this link

	public Link(jIRCdMBean jircd, Connector connection) {
		if(connection == null)
			throw new IllegalArgumentException("The connection cannot be null");
		this.jircd = jircd;
		this.connection = connection;
		server = new Unknown(connection, this, jircd.getServer());
		login(jircd.getServer());
	}
	public final Connection getConnection() {
		return connection;
	}
	public void setMessageFactory(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}
	protected void login(Server thisServer) {
		String linkPassword = jircd.getProperty("jircd.connect."+connection.getRemoteAddress()+'#'+connection.getRemotePort());
		connection.writeLine("PASS "+linkPassword+" 0210 IRC|");
		connection.writeLine("SERVER "+thisServer.getName()+" 1 "+thisServer.getToken()+" :"+thisServer.getDescription());
	}
	public final void handleLine(String line) {
		if (line.length() > Constants.MAX_MESSAGE_LENGTH)
			line = line.substring(0, Constants.MAX_MESSAGE_LENGTH); // max length per RFC

		final Message inMessage = messageFactory.createMessage(line);
		final String cmd = inMessage.getCommand();
		if (cmd.equalsIgnoreCase("PASS")) {
			if(!checkPassword(inMessage.getParameter(0)))
				jircd.disconnectLink(this);
		} else if (cmd.equalsIgnoreCase("SERVER")) {
			loginServer(inMessage);
		} else if (Character.isDigit(cmd.charAt(0))) {
			// numeric reply
		} else {
			jircd.invokeCommand(server, inMessage);
		}
	}
	private boolean checkPassword(String password) {
		String expectedPassword = jircd.getProperty("jircd.accept."+connection.getRemoteAddress()+'#'+connection.getLocalPort());
		return password.equals(expectedPassword);
	}
	protected void loginServer(Message loginMsg) {
		final Server thisServer = jircd.getServer();
		if(server instanceof Server) {
			connection.writeLine(':' + thisServer.getName() + " 462 :You may not reregister");
		} else {
			String name = loginMsg.getParameter(0);
			int hopCount = Integer.parseInt(loginMsg.getParameter(1));
			int token = Integer.parseInt(loginMsg.getParameter(2));
			String desc = loginMsg.getParameter(3);
			server = new Server(name, token, desc, thisServer, connection, this);
			thisServer.getNetwork().addServer((Server)server);
			Util.sendNetSync(thisServer, (Server)server);
		}
	}
	public String toString() {
		return server.toString();
	}
}
