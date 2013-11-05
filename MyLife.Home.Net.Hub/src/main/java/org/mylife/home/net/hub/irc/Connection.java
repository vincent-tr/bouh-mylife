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

import java.net.Socket;

/**
 * @author markhale
 */
public abstract class Connection {
	protected final Socket socket;
	private final long connectTime;
	protected long bytesRead;
	protected int linesRead;
	protected long bytesSent;
	protected int linesSent;

	protected Connection(Socket socket) {
		this.socket = socket;
		connectTime = System.currentTimeMillis();
	}
	public final String getRemoteAddress() {
		return socket.getInetAddress().getHostAddress();
	}
	public final String getRemoteHost() {
		return socket.getInetAddress().getHostName();
	}
	public final int getRemotePort() {
		return socket.getPort();
	}
	public final String getLocalAddress() {
		return socket.getLocalAddress().getHostAddress();
	}
	public final String getLocalHost() {
		return socket.getLocalAddress().getHostName();
	}
	public final int getLocalPort() {
		return socket.getLocalPort();
	}
	public final boolean isSecure() {
		return (socket instanceof javax.net.ssl.SSLSocket);
	}

	public final long getBytesSent() {
		return bytesSent;
	}
	public final int getLinesSent() {
		return linesSent;
	}
	public final long getBytesRead() {
		return bytesRead;
	}
	public final int getLinesRead() {
		return linesRead;
	}
	public final long getConnectTimeMillis() {
		return connectTime;
	}

	public abstract void writeLine(String s);
	public abstract void close();
}
