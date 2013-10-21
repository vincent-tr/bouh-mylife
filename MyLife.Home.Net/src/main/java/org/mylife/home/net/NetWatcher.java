package org.mylife.home.net;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

/**
 * Gestion de la surveillance des objets distants
 * @author pumbawoman
 *
 */
class NetWatcher implements IRCEventListener {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(NetWatcher.class.getName());

	/**
	 * Connexion irc
	 */
	private final IRCConnection connection;

	/**
	 * Fermeture
	 */
	private boolean closed;
	
	/**
	 * Liste des salons à surveiller
	 */
	private final Set<String> channels = new HashSet<String>();

	/**
	 * Constructeur par défaut
	 */
	public NetWatcher(String id) {
		String server = Configuration.getInstance().getProperty("ircserver");
		connection = new IRCConnection(server, new int[] { 6667 }, null, id, id, id);
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
	 * @param name
	 */
	public void addChannel(String name) {
		channels.add(name);
		if(connection.isConnected())
			connection.doJoin("#" + name);
	}
	
	/**
	 * Suppression d'un salon
	 * @param name
	 */
	public void removeChannel(String name) {
		channels.remove(name);
		if(connection.isConnected())
			connection.doPart("#" + name);
	}
	
	/**
	 * Liste des salons
	 * @return
	 */
	public Set<String> getChannels() {
		return Collections.unmodifiableSet(channels);
	}

	/**
	 * Connexion avec logging
	 */
	private void doConnect() {
		if(closed)
			return;
		try {
			connection.connect();
		} catch(IOException ex) {
			log.log(Level.SEVERE, "IRC connection error", ex);
		}
	}
	
	/**
	 * Exécution de la déconnexion
	 */
	private void doDisconnect() {
		// TODO : déconnecter tous les objets
		connection.close();
	}

	@Override
	public void onRegistered() {
		for(String channel : channels) {
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
		// TODO
	}

	@Override
	public void onKick(String chan, IRCUser user, String passiveNick, String msg) {
		// TODO
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
		// TODO
	}

	@Override
	public void onNotice(String target, IRCUser user, String msg) {
		// nothing
	}

	@Override
	public void onPart(String chan, IRCUser user, String msg) {
		// TODO
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
		// TODO
	}

	@Override
	public void onReply(int num, String value, String msg) {
		// TODO : names
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
