package org.mylife.home.net.hub.irc.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.protocol.Numerics;
import org.mylife.home.net.hub.irc.structure.Channel;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Server;
import org.mylife.home.net.hub.irc.structure.Unregistered;
import org.mylife.home.net.hub.irc.structure.User;

public final class CommandUtils {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(CommandUtils.class
			.getName());

	private CommandUtils() {
	}

	public static boolean checkRegistered(IrcServer server, IrcConnection src) {
		Connectable structure = src.getStructure();
		if (!(structure instanceof Unregistered))
			return true;

		replyError(server, src, Numerics.ERR_NOTREGISTERED);
		return false;
	}

	public static boolean checkServer(IrcServer server, IrcConnection src) {
		Connectable structure = src.getStructure();
		if (structure instanceof Server)
			return true;

		replyError(server, src, Numerics.ERR_NOPRIVILEGES);
		return false;
	}

	public static boolean checkParametersCount(IrcServer server,
			IrcConnection src, Message msg, int minCount) {
		if (msg.getParameterCount() >= minCount)
			return true;

		replyError(server, src, Numerics.ERR_NEEDMOREPARAMS);
		return false;
	}

	public static void replyError(IrcServer server, IrcConnection dest,
			Numerics num) {

		Connectable structure = dest.getStructure();

		User user = null;
		if (structure instanceof User)
			user = (User) structure;
		Message msg = Numerics.createMessage(server, num, user);

		if (structure instanceof Server) {
			Server destServer = (Server) structure;
			// pas d'erreur à un serveur, seulement un log
			log.severe("Protocol error with server : " + destServer.getName()
					+ " : " + msg.toString());
			return;
		}

		dest.send(msg);
	}

	/**
	 * Propage le message spécifié à tous les serveurs sauf ceux exclus
	 * 
	 * @param server
	 * @param msg
	 * @param excluded
	 */
	public static void dispatchServerMessage(IrcServer server, Message msg,
			IrcConnection... excluded) {

		Collection<IrcConnection> excludedList = Arrays.asList(excluded);
		Network network = server.getNetwork();

		// Transmission aux serveurs
		for (Server peer : network.getPeerServers()) {
			IrcConnection con = peer.getConnection();
			if (excludedList.contains(con))
				continue;
			con.send(msg);
		}
	}

	/**
	 * Propage le message spécifié à tous les intervenants impactés par
	 * l'utilisateur spécifié, sauf l'utilisateur en question et les connexions
	 * exclues
	 * 
	 * @param server
	 * @param user
	 * @param msg
	 * @param excluded
	 */
	public static void dispatchUserMessage(IrcServer server, User user,
			Message msg, IrcConnection... excluded) {

		Collection<IrcConnection> excludedList = Arrays.asList(excluded);
		Network network = server.getNetwork();

		// Transmission aux serveurs
		for (Server peer : network.getPeerServers()) {
			IrcConnection con = peer.getConnection();
			if (excludedList.contains(con))
				continue;
			con.send(msg);
		}

		// Transmission aux utilisateurs qui voient notre utilisateur
		for (User u : network.getLocalImpactedUsers(user)) {
			IrcConnection con = u.getConnection();
			if (excludedList.contains(con))
				continue;
			con.send(msg);
		}
	}

	public static void userError(IrcServer server, IrcConnection dest,
			String reason) {

		Connectable structure = dest.getStructure();
		if (structure instanceof Server)
			throw new IllegalArgumentException();

		Message errorMessage = new Message("ERROR");
		errorMessage.appendLastParameter(reason);
		dest.send(errorMessage);
		dest.setStructure(new Unregistered()); // empeche ConnectionClosedCommand de gérer
		dest.close();

		// si structure Unregistered rien d'autre à faire
		if (structure instanceof Unregistered)
			return;

		// c'est un user
		User user = (User) structure;

		// propagation du fait que l'utilisateur soit parti sur le réseau
		Message quitMessage = new Message(user.getNick(), "QUIT");
		quitMessage.appendLastParameter(reason);
		dispatchUserMessage(server, user, quitMessage);

		// maj de la base
		server.getNetwork().userRemove(user);
	}

	public static void dispatchNewUser(IrcServer server, User user,
			int hopcount, IrcConnection... excluded) {

		// Propagation du nouvel utilisateur
		Network net = server.getNetwork();
		Message msg = new Message("NICK");

		msg.appendParameter(user.getNick());
		msg.appendParameter("" + hopcount);
		msg.appendParameter(user.getIdent());
		msg.appendParameter(user.getHost());
		msg.appendParameter("" + net.getLocalServer().getToken());
		msg.appendParameter("+"); // umode
		msg.appendLastParameter(user.getRealName());

		CommandUtils.dispatchServerMessage(server, msg);
	}

	public static void userWelcome(IrcServer server, User user) {

		IrcConnection dest = user.getConnection();
		Network net = server.getNetwork();
		Server localServer = net.getLocalServer();

		Message message = Numerics.createMessage(server, Numerics.RPL_WELCOME,
				user);
		message.appendLastParameter("Welcome to the " + net.getName() + " "
				+ user.getFullName());
		dest.send(message);

		message = Numerics.createMessage(server, Numerics.RPL_YOURHOST, user);
		message.appendLastParameter("Your host is " + localServer.getName()
				+ ", running version " + server.getVersion());
		dest.send(message);

		message = Numerics.createMessage(server, Numerics.RPL_CREATED, user);
		message.appendLastParameter("This server was created "
				+ new Date(server.getStartTimeMillis()));
		dest.send(message);

		message = Numerics.createMessage(server, Numerics.RPL_MYINFO, user);
		message.appendLastParameter(localServer.getName() + " "
				+ IrcServer.class.getName() + "_v" + server.getVersion()
				+ " - -");
		dest.send(message);

		message = Numerics.createMessage(server, Numerics.RPL_ISUPPORT, user);
		// message.appendParameter("NICKLEN=" + Constants.MAX_NICK_LENGTH);
		// message.appendParameter("CHANNELLEN=" +
		// Constants.MAX_CHANNEL_LENGTH);
		// message.appendParameter("TOPICLEN=" + Constants.MAX_TOPIC_LENGTH);
		// message.appendParameter("PREFIX=(ov)@+");
		message.appendParameter("CHANTYPES=#");
		// message.appendParameter("CHANMODES=b,k,l,imt");
		message.appendParameter("CASEMAPPING=ascii");
		message.appendParameter("NETWORK=" + net.getName());
		message.appendLastParameter("are supported by this server");
		dest.send(message);

		dest.execute(new Message("LUSERS"));
		dest.execute(new Message("MOTD"));
	}
	
	public static void sendNames(IrcServer server, User user, Channel channel) {
		
		IrcConnection dest = user.getConnection();
		
		StringBuffer buffer = new StringBuffer();
		for(User item : channel.getUsers()) {
			if(buffer.length() > 0)
				buffer.append(' ');
			// si modes alors préfixes !
			buffer.append(item.getNick());
		}

		String chanPrefix = "=";
		/*
		if (isModeSet(CHANMODE_SECRET))
			chanPrefix = "@";
		else if (isModeSet(CHANMODE_PRIVATE))
			chanPrefix = "*";
		*/
		
		Message nameReply = Numerics.createMessage(server, Numerics.RPL_NAMREPLY, user);
		nameReply.appendParameter(chanPrefix);
		nameReply.appendParameter(channel.getName());
		nameReply.appendLastParameter(buffer.toString());
		dest.send(nameReply);

		Message endOfNamesReply = Numerics.createMessage(server, Numerics.RPL_ENDOFNAMES, user, channel.getName());
		dest.send(endOfNamesReply);
	}
}
