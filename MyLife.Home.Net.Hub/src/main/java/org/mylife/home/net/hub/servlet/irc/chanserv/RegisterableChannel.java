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

package org.mylife.home.net.hub.servlet.irc.chanserv;

import java.sql.*;
import org.mylife.home.net.hub.jIRCdMBean;
import org.mylife.home.net.hub.irc.Channel;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.User;

/**
 * @author markhale
 */
public class RegisterableChannel extends Channel {
	private final jIRCdMBean jircd;
	private final String dataSource;
	private boolean isRegistered;
	private String entryMsg;

	public RegisterableChannel(String name, jIRCdMBean jircd) {
		super(name);
		this.jircd = jircd;
		dataSource = jircd.getProperty("chanserv.jdbc.url");
	}
	RegisterableChannel(ResultSet rs, jIRCdMBean jircd) throws SQLException {
		this(rs.getString(1), jircd);
		topic = rs.getString(2);
		topicAuthor = rs.getString(3);
		topicTime = rs.getLong(4);
		entryMsg = rs.getString(5);
		isRegistered = true;
	}
        private Connection getDataSourceConnection() throws SQLException {
                return DriverManager.getConnection(dataSource);
        }

	boolean isRegistered() {
		return isRegistered;
	}
	void setRegistered(boolean register) {
		isRegistered = register;
		if(register) {
			register();
		} else {
			unregister();
		}
	}
	private void register() {
		try {
			Connection conn = getDataSourceConnection();
			try {
				PreparedStatement stmt = conn.prepareStatement(jircd.getProperty("chanserv.sql.registerChannel"));
				try {
					stmt.setString(1, getName());
					stmt.executeUpdate();
				} finally {
					stmt.close();
				}
			} finally {
				conn.close();
			}
		} catch(SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	private void unregister() {
		try {
			Connection conn = getDataSourceConnection();
			try {
				PreparedStatement stmt = conn.prepareStatement(jircd.getProperty("chanserv.sql.unregisterChannel"));
				try {
					stmt.setString(1, getName());
					stmt.executeUpdate();
				} finally {
					stmt.close();
				}
			} finally {
				conn.close();
			}
		} catch(SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	void setEntryMessage(String text) {
		entryMsg = text;
		try {
			Connection conn = getDataSourceConnection();
			try {
				PreparedStatement stmt = conn.prepareStatement(jircd.getProperty("chanserv.sql.setEntryMessage"));
				try {
					stmt.setString(1, entryMsg);
					stmt.setString(2, getName());
					stmt.executeUpdate();
				} finally {
					stmt.close();
				}
			} finally {
				conn.close();
			}
		} catch(SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	public void setTopic(User sender, String newTopic) {
		super.setTopic(sender, newTopic);
		try {
			Connection conn = getDataSourceConnection();
			try {
				PreparedStatement stmt = conn.prepareStatement(jircd.getProperty("chanserv.sql.setTopic"));
				try {
					stmt.setString(1, topic);
					stmt.setString(2, topicAuthor);
					stmt.setLong(3, topicTime);
					stmt.setString(4, getName());
					stmt.executeUpdate();
				} finally {
					stmt.close();
				}
			} finally {
				conn.close();
			}
		} catch(SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	public void addUser(User user) {
		super.addUser(user);
		if(entryMsg != null && entryMsg.length() > 0) {
			User chanserv = user.getServer().getNetwork().getUser("ChanServ");
			Message msg = new Message(chanserv, "NOTICE", user);
			msg.appendLastParameter(entryMsg);
			user.send(msg);
		}
	}
	public void removeUser(User user) {
		super.removeUser(user);
		if(isRegistered)
			user.getServer().getNetwork().addChannel(this);
	}
}
