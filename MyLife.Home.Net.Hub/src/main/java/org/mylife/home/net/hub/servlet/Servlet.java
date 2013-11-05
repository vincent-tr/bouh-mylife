package org.mylife.home.net.hub.servlet;

import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Source;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc_p10.Util;
import org.mylife.home.net.hub.irc_p10.Server_P10;
import org.mylife.home.net.hub.irc_p10.User_P10;
import org.mylife.home.net.hub.servlet.irc.IrcServlet;

/**
 * A servlet on a server.
 * @author markhale
 */
public class Servlet extends User_P10 {
	private final IrcServlet servlet;

	public Servlet(String nick, String name, IrcServlet servlet, Server_P10 server) {
		super(nick, Util.randomUserToken(server), "Servlet", "jIRCd", name, server);
		this.servlet = servlet;
	}
	protected String maskHost(String hostname) {
		return hostname;
	}
	public void send(Message message) {
		Source sender = message.resolveSender(server.getNetwork());
		if(sender instanceof User) {
			User user = (User) sender;
			String cmd = message.getCommand();
			String text = message.getParameter(message.getParameterCount()-1);
			IrcServletRequestImpl request = new IrcServletRequestImpl(user, cmd, text);
			IrcServletResponseImpl response = new IrcServletResponseImpl(this, user, cmd);
			try {
				servlet.service(request, response);
				if(!response.isCommitted())
					response.commit();
			} catch(Exception e) {
				servlet.log("An exception occured while trying to service a request", e);
			}
		}
	}
}
