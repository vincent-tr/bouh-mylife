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

import java.text.DateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Date;

/**
 * IRC channel.
 * @author thaveman
 * @author markhale
 */
public class Channel {
	public static final char CHANMODE_PRIVATE    = 'p';
	public static final char CHANMODE_SECRET     = 's';
	public static final char CHANMODE_INVITEONLY = 'i';
	public static final char CHANMODE_TOPICOPS   = 't';
	public static final char CHANMODE_NOEXTERNAL = 'n';
	public static final char CHANMODE_MODERATED  = 'm';
	
	public static final char CHANMODE_OPERATOR   = 'o';
	public static final char CHANMODE_VOICE      = 'v';
	public static final char CHANMODE_BAN        = 'b';
	
	public static final char CHANMODE_LIMIT      = 'l';
	public static final char CHANMODE_KEY        = 'k';

	private final String name;
	private final long creationTime;
	private final Modes modes = new Modes();
	protected String topic = "";
	protected String topicAuthor = "";
	protected long topicTime; // millis
	private String key; // null if none
	private int limit; // 0 if none
	/** (User user, Member member) */
	private final Map members = Collections.synchronizedMap(new HashMap());
	/** set of Bans */
	private final Set bans = Collections.synchronizedSet(new HashSet());
	/** set of invited Users */
	private final Set invites = Collections.synchronizedSet(new HashSet());

	/**
	 * Channel member.
	 */
	private class Member {
		private final User user;
		private final Modes chanModes = new Modes();

		public Member(User user) {
			this.user = user;
		}
		public User getUser() {
			return user;
		}
		public boolean isChanOp() {
			return chanModes.contains(Channel.CHANMODE_OPERATOR);
		}
		public void setOp(boolean state) {
			if (state)
				chanModes.add(Channel.CHANMODE_OPERATOR);
			else
				chanModes.remove(Channel.CHANMODE_OPERATOR);
		}
		public boolean isChanVoice() {
			return chanModes.contains(Channel.CHANMODE_VOICE);
		}
		public void setVoice(boolean state) {
			if (state)
				chanModes.add(Channel.CHANMODE_VOICE);
			else
				chanModes.remove(Channel.CHANMODE_VOICE);
		}
	}

	/**
	 * Channel ban.
	 */
	private class Ban {
		private final String mask;
		private final String who;
		private final long when;
		
		public Ban(String mask,String who) {
			this.mask = mask;
			this.who = who;
			this.when = System.currentTimeMillis();
		}
	}

	/**
	 * Constructs a new IRC channel.
	 */
	public Channel(String name) {
		if(name == null)
			throw new NullPointerException("Channel name cannot be null");
		this.name = name;
		this.creationTime = System.currentTimeMillis();
	}

	public String getName() {
		return name;
	}

	public long getCreationTimeMillis() {
		return creationTime;
	}

	public Set getUsers() {
		return Collections.unmodifiableSet(members.keySet());
	}
	public int getCount() {
		return members.size();
	}
	
	public String getTopic() {
		return topic;
	}
	
	public boolean isOn(User usr) {
		return members.containsKey(usr);
	}
	
	private Member getMember(User usr) {
		return (Member) members.get(usr);
	}

	public void joinUser(User us, String[] params) {
		// check for bans
		if(isBanned(us.toString())) {
			Message message = new Message(Constants.ERR_BANNEDFROMCHAN, us);
			message.appendParameter(name);
			message.appendParameter(Util.getResourceString(us, "ERR_BANNEDFROMCHAN"));
			us.send(message);
			return;
		}
		// check for key
		if (this.key != null && !this.key.equals("")) {
			String providedKey = "";
			if (params.length > 1)
				providedKey = params[1];
			if (!providedKey.equals(this.key)) {
				Message message = new Message(Constants.ERR_BADCHANNELKEY, us);
				message.appendParameter(name);
				message.appendParameter(Util.getResourceString(us, "ERR_BADCHANNELKEY"));
				us.send(message);
				return;
			}
		}
		// check for member limit
		if (this.limit > 0) {
			if (members.size() >= this.limit) {
				Message message = new Message(Constants.ERR_CHANNELISFULL, us);
				message.appendParameter(name);
				message.appendParameter("Cannot join channel (+l)");
				us.send(message);
				return;
			}
		}
		// check for invite
		if (this.isModeSet(CHANMODE_INVITEONLY) && !invites.contains(us)) {
			Message message = new Message(Constants.ERR_INVITEONLYCHAN, us);
			message.appendParameter(name);
			message.appendParameter("Cannot join channel (+i)");
			us.send(message);
			return;
		}
		
		addUser(us);
		Message message = new Message(us, "JOIN", this);
		send(message);
		if (us.getClient() != null) {
			sendNames(us);
			sendTopicInfo(us);
		}
	}

	public void addUser(User user) {
	synchronized(members) {
		user.addChannel(this);
		Member member = new Member(user);
		if(members.isEmpty())
			member.setOp(true);
		members.put(user, member);
	}
	}

	public void addBan(String mask, String who) {
		bans.add(new Ban(mask,who));
	}
	public boolean isBanned(String user) {
	synchronized(this.bans) {
		for(Iterator iter = bans.iterator(); iter.hasNext();) {
			Ban ban = (Ban) iter.next();
			if(Util.match(ban.mask, user)) {
				return true;
			}
		}
	}
		return false;
	}
	public void listBans(User towho) {
	synchronized(this.bans) {
		for(Iterator iter = bans.iterator(); iter.hasNext();) {
			Ban ban = (Ban) iter.next();
			Message message = new Message(Constants.RPL_BANLIST, towho);
			message.appendParameter(name);
			message.appendParameter(ban.mask);
			message.appendParameter(ban.who);
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, towho.getLocale());
			message.appendParameter(df.format(new Date(ban.when)));
			towho.send(message);
		}
	}
		Message message = new Message(Constants.RPL_ENDOFBANLIST, towho);
		message.appendParameter(name);
		message.appendParameter("End of channel ban list");
		towho.send(message);
	}
	
	public boolean removeBan(String mask) {
	synchronized(this.bans) {
		for(Iterator iter = bans.iterator(); iter.hasNext();) {
			Ban ban = (Ban) iter.next();
			if(ban.mask.equals(mask)) {
				iter.remove();
				return true;
			}
		}
	}
		return false;
	}

	public void invite(User usr) {
		invites.add(usr);
	}

	public void removeUser(User usr) {
	synchronized(members) {
		members.remove(usr);
		usr.removeChannel(this);
		if(members.isEmpty())
			usr.getServer().getNetwork().removeChannel(this);
	}
	}

	public boolean isOp(User usr) {
		Member member = getMember(usr);
		return (member != null && member.isChanOp());
	}
	
	public boolean isVoice(User usr) {
		Member member = getMember(usr);
		return (member != null && member.isChanVoice());
	}

	public void sendTopicInfo(User usr) {
		String t, tAuthor;
		long tTime;
		synchronized(this.topic) {
			t = topic;
			tAuthor = topicAuthor;
			tTime = topicTime;
		}
		if (t.length() == 0) {
			Message message = new Message(Constants.RPL_NOTOPIC, usr);
			message.appendParameter(name);
			message.appendParameter("No topic is set");
			usr.send(message);
		} else {
			Message message = new Message(Constants.RPL_TOPIC, usr);
			message.appendParameter(name);
			message.appendParameter(t);
			usr.send(message);
			message = new Message(Constants.RPL_TOPICWHOTIME, usr);
			message.appendParameter(name);
			message.appendParameter(tAuthor);
			message.appendParameter(Long.toString(tTime/1000));
			usr.send(message);
		}
	}

	public String getNamesList() {
		StringBuffer sb = new StringBuffer();
	synchronized(members) {
		for(Iterator iter = members.values().iterator(); iter.hasNext();) {
			Member member = (Member) iter.next();
			if (member.isChanOp())
				sb.append(",@");
			else if (member.isChanVoice())
				sb.append(",+");
			else
				sb.append(',');
			sb.append(member.getUser().getNick());
		}
	}
		return (sb.length() > 0 ? sb.substring(1) : ""); // get rid of leading comma
	}

	public void sendNames(User usr) {
		StringBuffer sb = new StringBuffer();
	synchronized(members) {
		for(Iterator iter = members.values().iterator(); iter.hasNext();) {
			Member member = (Member) iter.next();
			if (member.isChanOp())
				sb.append(" @");
			else if (member.isChanVoice())
				sb.append(" +");
			else
				sb.append(' ');
			sb.append(member.getUser().getNick());
		}
	}
		
		String ournames = (sb.length() > 0 ? sb.substring(1) : ""); // get rid of leading space ' '

		String chanPrefix = "=";
		if(isModeSet(CHANMODE_SECRET))
			chanPrefix = "@";
		else if(isModeSet(CHANMODE_PRIVATE))
			chanPrefix = "*";

		Message message = new Message(Constants.RPL_NAMREPLY, usr);
		message.appendParameter(chanPrefix);
		message.appendParameter(name);
		message.appendLastParameter(ournames);
		usr.send(message);

		message = new Message(Constants.RPL_ENDOFNAMES, usr);
		message.appendParameter(name);
		message.appendParameter("End of /NAMES list");
		usr.send(message);
	}

	/**
	 * Sends a message to this channel, excluding a specified user.
	 */
	public void send(Message message, User userExcluded) {
	synchronized(members) {
		for(Iterator iter = members.keySet().iterator(); iter.hasNext();) {
			User user = (User) iter.next();
			if (!(user.equals(userExcluded))) {
				user.send(message);
			}
		}
	}
	}
	/**
	 * Sends a message to all the users in this channel.
	 */
	public void send(Message message) {
	synchronized(members) {
		for(Iterator iter = members.keySet().iterator(); iter.hasNext();) {
			User user = (User) iter.next();
			user.send(message);
		}
	}
	}

	public void setTopic(User sender, String newTopic) {
		synchronized(this.topic) {
			topic = newTopic;
			topicAuthor = sender.getNick();
			topicTime = System.currentTimeMillis();
		}
		Message message = new Message(sender, "TOPIC", this);
		message.appendParameter(newTopic);
		send(message);
	}

	public String getModesList() {
		String modesList = modes.toString();
		StringBuffer modeParams = new StringBuffer();

		if(modes.contains(CHANMODE_KEY))
			modeParams.append(' ').append(this.key);
		if(modes.contains(CHANMODE_LIMIT))
			modeParams.append(' ').append(this.limit);

		if(modeParams.length() > 0)
			modesList = modesList+modeParams;
		return modesList;
	}

	public void processModes(User sender, String modeString, String[] modeParams) {
		if(modeString.equals("+b") && modeParams.length == 0) {
			this.listBans(sender);
			return;
		}

		boolean addingMode = true; // are we adding modes (+) or subtracting (-)

		StringBuffer goodModes = new StringBuffer();
		String[] goodParams = new String[modeParams.length];
		int goodParamsCount = 0;

		int n = 0; // modeParams index

		for (int i = 0; i < modeString.length(); i++) {
			boolean doDo = false;

			char modeChar = modeString.charAt(i);
			switch(modeChar) {
			case '+':
				addingMode = true;
				goodModes.append('+');
				break;
			case '-':
				addingMode = false;
				goodModes.append('-');
				break;
			case CHANMODE_LIMIT:
				if (addingMode) {
					if (n >= modeParams.length) break;
					try {
						int tryLimit = Integer.parseInt(modeParams[n]);
						limit = tryLimit;
						goodParams[goodParamsCount] = modeParams[n];
						goodParamsCount++;
						doDo = true;
					} catch(NumberFormatException nfe) {
					} finally {
						n++; // move on to the next parameter
					}
				} else {
					limit = 0;
					doDo = true;
				}
				break;
			case CHANMODE_KEY:
				if (addingMode) {
					if (n >= modeParams.length) break;
					String tryKey = modeParams[n];
					n++;
					if (Util.isIRCString(tryKey)) {
						key = tryKey;
						goodParams[goodParamsCount] = tryKey;
						goodParamsCount++;
						doDo = true;
					}
				} else {
					if (n >= modeParams.length) break;
					String tryKey = modeParams[n];
					n++;
					if (key.equalsIgnoreCase(tryKey)) {
						key = null;
						goodParams[goodParamsCount] = tryKey;
						goodParamsCount++;
						doDo = true;
					}
				}
				break;
			case CHANMODE_OPERATOR:
				if (n >= modeParams.length) break;
				String opName = modeParams[n];
				n++;
				User opWho = sender.getServer().getNetwork().getUser(opName);
				if (opWho != null) {
					Member opMe = this.getMember(opWho);
					if (opMe != null) {
						doDo = true;
						goodParams[goodParamsCount] = opName;
						goodParamsCount++;
						opMe.setOp(addingMode);
					} else {
						Util.sendUserNotInChannelError(sender, opName, this.name);
					}
				} else {
					Util.sendNoSuchNickError(sender, opName);
				}
				break;
			case CHANMODE_BAN:
				if (n >= modeParams.length) break;
				String banMask = modeParams[n];
				n++;
				if (addingMode) {
					this.addBan(banMask, sender.getNick());
					doDo = true;
					goodParams[goodParamsCount] = banMask;
					goodParamsCount++;
				} else {
					if (this.removeBan(banMask)) {
						doDo = true;
						goodParams[goodParamsCount] = banMask;
						goodParamsCount++;
					}
					else break;
				}
				break;
			case CHANMODE_VOICE:
				if (n >= modeParams.length) break;
				String vName = modeParams[n];
				n++;
				User vWho = sender.getServer().getNetwork().getUser(vName);
				if (vWho != null) {
					Member vMe = this.getMember(vWho);
					if (vMe != null) {
						doDo = true;
						goodParams[goodParamsCount] = vName;
						goodParamsCount++;
						vMe.setVoice(addingMode);
					} else {
						Util.sendUserNotInChannelError(sender, vName, this.name);
					}
				} else {
					Util.sendNoSuchNickError(sender, vName);
				}
				break;
			default:
				doDo = true;
			}

			if (doDo) {
				try {
					if (addingMode)
						modes.add(modeChar);
					else
						modes.remove(modeChar);
					goodModes.append(modeChar);
				} catch(IllegalArgumentException e) {
					//Invalid Mode Character Detected!
					Message message = new Message(Constants.ERR_UNKNOWNMODE, sender);
					message.appendParameter(Character.toString(modeChar));
					message.appendParameter("is unknown mode char to me for "+name);
					sender.send(message);
				}
			}
		}
		
		if (goodModes.length() > 1) {
			Message message = new Message(sender, "MODE", this);
			message.appendParameter(goodModes.toString());
			for(int i=0; i<goodParamsCount; i++)
				message.appendParameter(goodParams[i]);
			send(message);
		}
	}

	public boolean isModeSet(char mode) {
		return modes.contains(mode);
	}
}
