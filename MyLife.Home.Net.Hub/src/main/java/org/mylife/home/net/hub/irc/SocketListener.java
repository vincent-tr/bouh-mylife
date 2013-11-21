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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import org.mylife.home.net.hub.IrcServerMBean;

/**
 * Listens on a port and accepts new clients.
 * 
 * @author thaveman
 * @author markhale
 */
public class SocketListener extends Listener {
	private static final int BACKLOG = 16535;

	private static final Logger logger = Logger.getLogger(SocketListener.class
			.getName());

	private final ServerSocketFactory factory;
	private final ExecutorService streamThreadPool = Executors
			.newCachedThreadPool();
	private ServerSocket serverSocket;

	public SocketListener(IrcServerMBean jircd, String address, int port, ServerSocketFactory factory, 
			ExecutorService pool) {
		super(jircd, address, port, pool);
		this.factory = factory;
	}

	public String toString() {
		return getClass().getName() + '[' + factory.getClass().getName() + ','
				+ boundAddress + ':' + boundPort + ']';
	}

	/** Waits for a connection */
	protected void waitForActivity() throws IOException {
		Socket socket = serverSocket.accept();
		StreamConnection connection = new StreamConnection(socket, this,
				streamThreadPool);
		Connection.Handler handler = new Connection.Handler(jircd, connection);
		connection.setHandler(handler);
		connection.start();
	}

	public boolean bind() {
		try {
			serverSocket = factory.createServerSocket(boundPort, BACKLOG,
					InetAddress.getByName(boundAddress));
		} catch (IOException ioe) {
			logger.log(Level.WARNING, "Bind exception", ioe);
			return false;
		}
		return true;
	}

	public void close() {
		super.close();
		streamThreadPool.shutdown();
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Server socket close exception", e);
		}
	}
}