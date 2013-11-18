package org.mylife.home.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.net.irc.IRCNetConnection;
import org.schwering.irc.lib.IRCConstants;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

/**
 * Gestion de la surveillance des objets distants
 * 
 * @author pumbawoman
 * 
 */
class NetWatcher implements IRCEventListener {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(NetWatcher.class
			.getName());

	/**
	 * Connexion irc
	 */
	private final IRCNetConnection connection;

	/**
	 * Fermeture
	 */
	private boolean closed;

	/**
	 * Liste des salons à surveiller
	 */
	private final Map<String, Integer> channels = Collections
			.synchronizedMap(new HashMap<String, Integer>());

	/**
	 * Identifiant
	 */
	private final String id;

	/**
	 * Construction de l'id
	 * 
	 * @return
	 */
	private static String buildId() {
		try {
			StringBuffer id = new StringBuffer();
			id.append(InetAddress.getLocalHost().getHostName());
			id.append('-');
			id.append(System.currentTimeMillis());
			id.append('-');
			id.append("watcher");
			return id.toString();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Constructeur par défaut
	 */
	public NetWatcher() {
		id = buildId();
		String server = Configuration.getInstance().getProperty("ircserver");
		connection = new IRCNetConnection(server, 6667, id, id);
		connection.addIRCEventListener(this);
		connection.setDaemon(true);
		connection.setPong(true);
		doConnect();
	}

	/**
	 * Fermeture du watcher
	 */
	public void close() {
		closed = true;
		connection.doQuit();
		connection.close();
	}

	/**
	 * Ajout d'un salon
	 * 
	 * @param name
	 */
	public void addChannel(String name) {
		Integer value = channels.get(name);
		if (value == null)
			value = 0;
		++value;
		channels.put(name, value);
		if (value == 1 && connection.isConnected())
			connection.doJoin("#" + name);
	}

	/**
	 * Suppression d'un salon
	 * 
	 * @param name
	 */
	public void removeChannel(String name) {
		Integer value = channels.get(name);
		if (value == null)
			return;
		--value;
		channels.put(name, value);
		if (value == 0) {
			channels.remove(name);
			if (connection.isConnected())
				connection.doPart("#" + name);
		}
	}

	/**
	 * Liste des salons
	 * 
	 * @return
	 */
	public Set<String> getChannels() {
		return Collections.unmodifiableSet(channels.keySet());
	}

	/**
	 * Envoi d'un message
	 * 
	 * @param channel
	 * @param target
	 * @param message
	 */
	public void send(String channel, String target, String message) {
		if (!connection.isConnected()) {
			log.log(Level.SEVERE, "IRC connection not connected");
			return;
		}
		connection.doPrivmsg("#" + channel, target + " " + message);
	}

	/**
	 * Connexion avec logging
	 */
	private void doConnect() {
		if (closed)
			return;
		try {
			connection.connect();
		} catch (IOException ex) {
			log.log(Level.SEVERE, "IRC connection error", ex);
		}
	}

	/**
	 * Exécution de la déconnexion
	 */
	private void doDisconnect() {
		connection.close();
		RemoteConnector.nickPart(null, null);
	}

	private String getInternalChannel(String ircChannel) {
		if (ircChannel == null || ircChannel.length() == 0)
			return ircChannel;
		if (ircChannel.charAt(0) != '#')
			return ircChannel;
		return ircChannel.substring(1);
	}

	@Override
	public void onRegistered() {
		for (String channel : channels.keySet()) {
			connection.doJoin("#" + channel);
		}
	}

	@Override
	public void onDisconnected() {
		log.severe("Connection broken !");
		doDisconnect();
		doConnect();
	}

	@Override
	public void onError(String msg) {
		log.info(String.format("IRC server error received : %s", msg));
	}

	@Override
	public void onError(int num, String msg) {
		log.info(String.format("IRC server error received : (%d) %s", num, msg));
	}

	@Override
	public void onInvite(String chan, IRCUser user, String passiveNick) {
		// nothing
	}

	@Override
	public void onJoin(String chan, IRCUser user) {
		RemoteConnector.nickJoin(user.getNick(), getInternalChannel(chan));
	}

	@Override
	public void onKick(String chan, IRCUser user, String passiveNick, String msg) {
		RemoteConnector.nickPart(passiveNick, getInternalChannel(chan));
	}

	@Override
	public void onMode(String chan, IRCUser user, IRCModeParser modeParser) {
		// nothing
	}

	@Override
	public void onMode(IRCUser user, String passiveNick, String mode) {
		// nothing
	}

	@Override
	public void onNick(IRCUser user, String newNick) {
		RemoteConnector.nickChanged(user.getNick(), newNick);
	}

	@Override
	public void onNotice(String target, IRCUser user, String msg) {
		// nothing
	}

	@Override
	public void onPart(String chan, IRCUser user, String msg) {
		RemoteConnector.nickPart(user.getNick(), getInternalChannel(chan));
	}

	@Override
	public void onPing(String ping) {
		// nothing : déjà traité
	}

	@Override
	public void onPrivmsg(String target, IRCUser user, String msg) {
		// nothing
	}

	@Override
	public void onQuit(IRCUser user, String msg) {
		RemoteConnector.nickPart(user.getNick(), null);
	}

	@Override
	public void onReply(int num, String value, String msg) {
		switch (num) {
		case IRCConstants.RPL_NAMREPLY:
			StringTokenizer tokenizer = new StringTokenizer(value);
			tokenizer.nextToken(); // nick
			tokenizer.nextToken(); // type de channel, = pour public channel
			String chan = getInternalChannel(tokenizer.nextToken()); // nom du
																		// chan
			tokenizer = new StringTokenizer(msg);
			while (tokenizer.hasMoreTokens()) {
				String nick = tokenizer.nextToken();
				if ("~&@%+".indexOf(nick.charAt(0)) > -1)
					nick = nick.substring(1);

				RemoteConnector.nickJoin(nick, chan);
			}
			break;
		}
	}

	@Override
	public void onTopic(String chan, IRCUser user, String topic) {
		// nothing
	}

	@Override
	public void unknown(String prefix, String command, String middle,
			String trailing) {
		// nothing
	}
}
