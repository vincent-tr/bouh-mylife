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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.net.ServerSocketFactory;

import org.mylife.home.net.hub.jIRCdMBean;
import org.apache.log4j.Logger;

/**
 * Listens on a port and accepts new clients.
 * @author thaveman
 * @author markhale
 */
public class Listener implements Runnable {
	private static final int BACKLOG = 16535;

	private static final Logger logger = Logger.getLogger(Listener.class);

	private final jIRCdMBean jircd;
	private final String boundAddress;
	private final int boundPort;
	private final ServerSocketFactory factory;
	private ServerSocket serverSocket;
	private volatile Thread listenerThread;
	private int connections;
	private int maxConnections;

	public Listener(jIRCdMBean jircd, String address, int port, ServerSocketFactory factory) {
		this.jircd = jircd;
		boundAddress = address;
		boundPort = port;
		this.factory = factory;
	}

	public String toString() {
		return getClass().getName() + '[' + factory.getClass().getName() + ',' + boundAddress + ':' + boundPort + ']';
	}

	public void start() {
		if(listenerThread == null) {
			listenerThread = new Thread(this, toString());
			listenerThread.start();
		}
	}
	public void run() {
		final Thread currentThread = Thread.currentThread();
		while (listenerThread == currentThread) {
			try {
				doAccept();
			} catch (IOException e) {
				logger.warn("IOException in thread " + Thread.currentThread().toString() + ": " + e.toString());
			}
		}
	}
        private void doAccept() throws IOException {
		Socket socket = serverSocket.accept();
		StreamClient streamClient = new StreamClient(socket);
		jircd.addClient(streamClient.getClient());
		streamClient.start();
		connections++;
		if(connections > maxConnections)
			maxConnections = connections;
	}
	public void stop() {
		if(listenerThread != null) {
			Thread thr = listenerThread;
			listenerThread = null;
			thr.interrupt();
		}
	}

	public boolean bind() {
		try {
			serverSocket = factory.createServerSocket(boundPort, BACKLOG, InetAddress.getByName(boundAddress));
		} catch (Exception e) {
			logger.warn("Bind exception", e);
			return false;
		}
		return true;
	}
	public void close() {
		stop();
		try {
			serverSocket.close();
		} catch(IOException e) {
			logger.warn("Server socket close exception", e);
		}
	}

	public String getAddress() {
		return boundAddress;
	}
	public int getPort() {
		return boundPort;
	}

	private class StreamClient extends Connection implements Runnable {
		private final BufferedReader input;
		private final BufferedWriter output;
		private final Client client;
		private volatile Thread inputThread;

		private StreamClient(Socket socket) throws IOException {
			super(socket);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream(), Constants.CHARSET), Constants.MAX_MESSAGE_SIZE);
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Constants.CHARSET), Constants.MAX_MESSAGE_SIZE);
			client = new Client(jircd, this);
			logger.debug("Initiating connection "+toString());
		}
		public final Client getClient() {
			return client;
		}
		public void start() {
			if(inputThread == null) {
				inputThread = new Thread(this, toString());
				inputThread.start();
			}
		}
		public void run() {
			final Thread currentThread = Thread.currentThread();
			while (inputThread == currentThread) {
				try {
					String line = input.readLine();
					logger.debug("Message received from " + toString() + "\n\t" + line);
					if (line != null && line.length() > 0) {
						bytesRead += line.length()+2;
						linesRead++;
						client.handleLine(line);
					} else if (line == null) {
						jircd.disconnectClient(client, "Connection reset by peer");
						return;
					}
				} catch(SocketException e) {
					jircd.disconnectClient(client, e.getMessage());
					return;
				} catch (Exception e) {
					logger.warn("Exception occured in thread " + Thread.currentThread().toString(), e);
					return;
				}
			}
		}
		public void stop() {
			if(inputThread != null) {
				Thread thr = inputThread;
				inputThread = null;
				thr.interrupt();
			}
		}
		public void writeLine(String text) {
			// Do not use PrintWriter.println() since that depends on the system property line.separator,
			// which may not be "\r\n".
			try {
				output.write(text);
				output.write("\r\n");
				output.flush();
				bytesSent += text.length()+2;
				linesSent++;
				if(logger.isDebugEnabled())
					logger.debug("Message sent to " + toString() + "\n\t" + text);
			} catch(IOException e) {
				logger.debug("Exception occurred while sending message", e);
			}
		}
		public void close() {
			logger.debug("Closing connection "+toString());
			stop();
			try {
				socket.close();
			} catch(IOException e) {
				logger.debug("Exception on socket close", e);
			} finally {
				connections--;
			}
		}
		public String toString() {
			return '[' + client.toString() + ',' + socket.getInetAddress().getHostAddress() + ':' + socket.getPort() + ',' + socket.getClass().getName() + ']';
		}
	}
}
