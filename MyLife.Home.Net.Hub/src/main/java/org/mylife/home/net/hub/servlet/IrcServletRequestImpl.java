package org.mylife.home.net.hub.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;

import org.mylife.home.net.hub.irc.Client;
import org.mylife.home.net.hub.irc.Connection;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.servlet.irc.IrcServletRequest;

/**
 * Implementation.
 */
public class IrcServletRequestImpl extends ServletRequestImpl implements IrcServletRequest {
	private final User user;
	private final String command;
	private final String message;

	public IrcServletRequestImpl(User user, String command, String message) {
		this.user = user;
		this.command = command;
		this.message = message;
		locales.add(user.getLocale());
	}
	public String getNick() {
		return user.getNick();
	}
	public String getCommand() {
		return command;
	}
	public String getText() {
		return message;
	}
	/**
	 * Returns -1 if unknown.
	 */
	public int getLocalPort() {
		Client client = (Client) user.getClient();
		if(client != null)
			return client.getConnection().getLocalPort();
		else
			return -1;
	}
	public String getLocalAddr() {
		Client client = (Client) user.getClient();
		if(client != null)
			return client.getConnection().getLocalAddress();
		else
			return null;
	}
	public String getLocalName() {
		Client client = (Client) user.getClient();
		if(client != null)
			return client.getConnection().getLocalHost();
		else
			return null;
	}
	/**
	 * Returns -1 if unknown.
	 */
	public int getRemotePort() {
		Client client = (Client) user.getClient();
		if(client != null)
			return client.getConnection().getRemotePort();
		else
			return -1;
	}
	public String getRemoteAddr() {
		Client client = (Client) user.getClient();
		if(client != null)
			return client.getConnection().getRemoteAddress();
		else
			return null;
	}
	public String getRemoteHost() {
		Client client = (Client) user.getClient();
		if(client != null)
			return client.getConnection().getRemoteHost();
		else
			return null;
	}
	public boolean isSecure() {
		Client client = (Client) user.getClient();
		if(client != null)
			return client.getConnection().isSecure();
		else
			return false;
	}
	public ServletInputStream getInputStream() {
		return new FilterServletInputStream(new ByteArrayInputStream(message.getBytes()));
	}
	public BufferedReader getReader() {
		return new BufferedReader(new StringReader(message));
	}
	public int getContentLength() {
		return message.length();
	}
	public String getContentType() {
		return null;
	}
	public String getProtocol() {
		return "IRC";
	}
	public String getScheme() {
		return "irc";
	}
	public String getServerName() {
		return getLocalName();
	}
	public int getServerPort() {
		return getLocalPort();
	}
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	public String getRealPath(String path) {
		return null;
	}
}
