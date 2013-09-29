package mylife.home.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import mylife.home.net.irc.IRCConnection;
import mylife.home.net.irc.IRCEventListener;
import mylife.home.net.irc.IRCModeParser;
import mylife.home.net.irc.IRCUser;
import mylife.home.net.structure.NetAttribute;
import mylife.home.net.structure.NetMember;

/**
 * Connecteur pour un objet local
 * 
 * @author pumbawoman
 * 
 */
class LocalConnector implements AttributeChangeListener, IRCEventListener, Connector {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(LocalConnector.class.getName());
	
	/**
	 * Conteneur publié
	 */
	private final NetContainer container;

	/**
	 * Connexion irc
	 */
	private final IRCConnection connection;

	/**
	 * Identifiant
	 */
	private final String id;

	/**
	 * Attributs
	 */
	private final List<String> attributesNames;
	
	/**
	 * Fermeture
	 */
	private boolean closed;
	
	/**
	 * Construction du connecteur avec l'objet
	 * 
	 * @param object
	 * @param channel
	 */
	public LocalConnector(NetContainer container) {
		
		this.container = container;
		NetObject object = container.getObject();
		this.id = object.getId();
		attributesNames = new ArrayList<String>();
		for (NetMember member : object.getNetClass().getMembers()) {
			if (!(member instanceof NetAttribute))
				continue;
			NetAttribute attr = (NetAttribute) member;
			String name = attr.getName();
			attributesNames.add(name);
			object.registerAttributeChange(name, this);
		}
		String server = Configuration.getInstance().getProperty("ircserver");
		connection = new IRCConnection(server, new int[] { 6667 }, null, getNick(), id, id);
		connection.addIRCEventListener(this);
		connection.setDaemon(true);
		connection.setPong(true);
		doConnect();
	}
	
	/**
	 * Fermeture du connecteur
	 */
	@Override
	public void close() {
		NetObject object = container.getObject();
		for (NetMember member : object.getNetClass().getMembers()) {
			if (!(member instanceof NetAttribute))
				continue;
			NetAttribute attr = (NetAttribute) member;
			String name = attr.getName();
			object.unregisterAttributeChange(name, this);
		}
		
		closed = true;
		connection.doQuit();
		connection.close();
	}

	/**
	 * Obtention du nick
	 * 
	 * @return
	 */
	private String getNick() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(id);
		for (String attributeName : attributesNames) {
			Object value = container.getObject().getAttributeValue(attributeName);
			buffer.append("|");
			buffer.append(String.valueOf(value));
		}
		return buffer.toString();
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
		container.setConnected(false);
		connection.close();
	}
	
	/**
	 * Appelé sur changement d'un attribut
	 * 
	 * @param obj
	 * @param attribute
	 * @param value
	 */
	@Override
	public void attributeChanged(NetObject obj, NetAttribute attribute,
			Object value) {
		connection.doNick(getNick());
	}

	@Override
	public void onRegistered() {
		connection.doJoin("#" + container.getChannel());
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
		if(user.getNick().equalsIgnoreCase(getNick())) {
			container.setConnected(true);
		}
	}

	@Override
	public void onKick(String chan, IRCUser user, String passiveNick, String msg) {
		// nothing
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
		// nothing
	}

	@Override
	public void onNotice(String target, IRCUser user, String msg) {
		// nothing
	}

	@Override
	public void onPart(String chan, IRCUser user, String msg) {
		// nothing
	}

	@Override
	public void onPing(String ping) {
		// nothing : déjà traité
	}

	@Override
	public void onPrivmsg(String target, IRCUser user, String msg) {
		if(target.equalsIgnoreCase("#" + container.getChannel())) {
			
			// message sur channel : on doit voir si le 1er token est notre id
			StringTokenizer tokenizer = new StringTokenizer(msg);
			if(tokenizer.hasMoreTokens()) {
				String chanTarget = tokenizer.nextToken();
				if(chanTarget.equalsIgnoreCase(id)) {
					processCommand(user, tokenizer);
				}
			}
			
		} else if(target.equalsIgnoreCase(getNick())) {
			
			// message privé : forcément adressé à nous
			StringTokenizer tokenizer = new StringTokenizer(msg);
			processCommand(user, tokenizer);
		}
	}

	@Override
	public void onQuit(IRCUser user, String msg) {
		// nothing
	}

	@Override
	public void onReply(int num, String value, String msg) {
		// nothing
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
	
	/**
	 * Exécution de commande
	 * @param from
	 * @param tokenizer
	 */
	private void processCommand(IRCUser from, StringTokenizer tokenizer) {
		
		String command = null;
		Collection<String> args = new ArrayList<String>();
		
		if(tokenizer.hasMoreTokens()) {
			command = tokenizer.nextToken();
		}

		while(tokenizer.hasMoreTokens()) {
			args.add(tokenizer.nextToken());
		}
		
		if(command != null)
			processCommand(from, command, args);
	}
	
	/**
	 * Exécution de commande
	 * @param from
	 * @param command
	 * @param args
	 */
	private void processCommand(IRCUser from, String command, Collection<String> args) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(command);
		for(String arg : args) {
			buffer.append(" ");
			buffer.append(arg);
		}
		log.info(String.format("Executing command : %s", buffer.toString()));
		
		try {
			container.getObject().executeActionAsString(command, args.toArray(new String[args.size()]));
		} catch(Exception ex) {
			log.log(Level.SEVERE, String.format("Error executing action %s", command), ex);
		}
	}
}
