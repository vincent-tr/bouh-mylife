package org.mylife.home.ui.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.mylife.home.net.Configuration;
import org.mylife.home.net.NetRepository;
import org.mylife.home.net.exchange.ExchangeManager;
import org.mylife.home.net.exchange.net.XmlNetContainer;
import org.mylife.home.net.exchange.ui.XmlUiContainer;
import org.mylife.home.net.irc.IRCNetConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

/**
 * Réception de la structure d'ui
 * 
 * @author pumbawoman
 * 
 */
public class NetUiReceiver implements IRCEventListener {

	/**
	 * Nick
	 */
	private static final String PUBLISHER_ID = "ui-publisher";

	/**
	 * Commande à exécuter pour obtenir les données d'ui
	 */
	private static final String UI_DATA_COMMAND = "getUiData";

	/**
	 * Préfix de données
	 */
	private static final String UI_DATA_PREFIX = "dataAdd";

	/**
	 * Fin des données
	 */
	private static final String UI_DATA_END = "dataEnd";

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(NetUiReceiver.class
			.getName());

	/**
	 * Listener de synchronisation
	 * 
	 * @author pumbawoman
	 * 
	 */
	public interface DataUpdatedListener {
		public void dataSynchronized(XmlUiContainer uiContainer,
				XmlNetContainer netContainer);

		public void dataDesynchronized();
	}

	/**
	 * Connexion irc
	 */
	private final IRCNetConnection connection;

	/**
	 * Listeners de synchronisation
	 */
	private final Set<DataUpdatedListener> listeners = Collections
			.synchronizedSet(new HashSet<DataUpdatedListener>());

	/**
	 * Données
	 */
	private XmlUiContainer uiContainer;

	/**
	 * Données
	 */
	private XmlNetContainer netContainer;

	/**
	 * Indique si les données sont synchronisées
	 */
	private boolean sync = false;

	/**
	 * Identifiant
	 */
	private final String id;

	/**
	 * Constructeur par défaut
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 */
	public NetUiReceiver() {

		id = buildId();

		String server = Configuration.getInstance().getProperty("ircserver");
		connection = new IRCNetConnection(server, 6667, id, id);
		connection.addIRCEventListener(this);
		connection.setPong(true);
		connection.start();
	}

	/**
	 * Fermeture du publisher
	 */
	public void close() {
		connection.doQuit();
		connection.stop();
	}

	public void addListener(DataUpdatedListener listener) {
		Validate.notNull(listener);
		listeners.add(listener);
	}

	public void removeListener(DataUpdatedListener listener) {
		Validate.notNull(listener);
		listeners.remove(listener);
	}

	public XmlUiContainer getUiContainer() {
		return uiContainer;
	}

	public XmlNetContainer getNetContainer() {
		return netContainer;
	}

	public boolean isConnected() {
		return connection.isConnected();
	}

	public boolean isSynchronized() {
		return sync;
	}

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
			id.append("-ui-receiver");
			return id.toString();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Envoi d'un message
	 * 
	 * @param target
	 * @param message
	 */
	private void send(String target, String message) {
		if (!connection.isConnected()) {
			log.log(Level.SEVERE, "IRC connection not connected");
			return;
		}
		connection.doPrivmsg(target, message);
	}

	private void syncRequest() {
		send("#" + NetRepository.CHANNL_UI, PUBLISHER_ID + " "
				+ UI_DATA_COMMAND + " " + id);
		log.info("Sync request send");
	}

	private void nickJoin(String nick) {
		String id = getIdFromNick(nick);
		if (id.equalsIgnoreCase(PUBLISHER_ID))
			syncRequest();
	}

	private void nickPart(String nick) {
		String id = getIdFromNick(nick);
		if (id.equalsIgnoreCase(PUBLISHER_ID))
			desync();
	}

	private String getIdFromNick(String nick) {
		int sepIndex;
		String id = nick;
		if ((sepIndex = id.indexOf('|')) > 0) {
			id = id.substring(0, sepIndex);
		}
		return id;
	}

	@Override
	public void onRegistered() {
		connection.doJoin("#" + NetRepository.CHANNL_UI);
		syncRequest();
	}

	@Override
	public void onDisconnected() {
		log.severe("Connection broken !");
		desync();
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
		nickJoin(user.getNick());
	}

	@Override
	public void onKick(String chan, IRCUser user, String passiveNick, String msg) {
		nickPart(passiveNick);
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
		nickPart(user.getNick());
	}

	@Override
	public void onPing(String ping) {
		// nothing : déjà traité
	}

	@Override
	public void onPrivmsg(String target, IRCUser user, String msg) {
		if (target.equalsIgnoreCase("#" + NetRepository.CHANNL_UI)) {

			// message sur channel : on doit voir si le 1er token est notre id
			StringTokenizer tokenizer = new StringTokenizer(msg);
			if (tokenizer.hasMoreTokens()) {
				String chanTarget = tokenizer.nextToken();
				if (chanTarget.equalsIgnoreCase(id)) {
					processCommand(user, tokenizer);
				}
			}

		} else if (target.equalsIgnoreCase(id)) {

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
	 * 
	 * @param from
	 * @param tokenizer
	 */
	private void processCommand(IRCUser from, StringTokenizer tokenizer) {

		String command = null;
		List<String> args = new ArrayList<String>();

		if (tokenizer.hasMoreTokens()) {
			command = tokenizer.nextToken();
		}

		while (tokenizer.hasMoreTokens()) {
			args.add(tokenizer.nextToken());
		}

		if (command != null)
			processCommand(from, command, args);
	}

	/**
	 * Exécution de commande
	 * 
	 * @param from
	 * @param command
	 * @param args
	 */
	private void processCommand(IRCUser from, String command, List<String> args) {

		if (command.equalsIgnoreCase(UI_DATA_PREFIX)) {
			if (dataBuilder == null)
				dataBuilder = new DataBuilder();
			dataBuilder.dataAdd(args.get(0)); // base64 encode => pas d'espaces
		} else if (command.equalsIgnoreCase(UI_DATA_END)) {
			if (dataBuilder == null)
				dataBuilder = new DataBuilder();

			if (dataBuilder.dataEnd())
				sync();
			else
				desync();
		}
	}

	private DataBuilder dataBuilder;

	/**
	 * Gestion de la construction des données
	 * 
	 * @author pumbawoman
	 * 
	 */
	private static class DataBuilder {

		private final StringBuffer buffer = new StringBuffer();
		private XmlUiContainer uiContainer;
		private XmlNetContainer netContainer;

		public void dataAdd(String line) {
			buffer.append(line);
		}

		public boolean dataEnd() {
			try {
				String stringValue = buffer.toString();
				byte[] data = Base64
						.decodeBase64(stringValue.getBytes("UTF-8"));
				readZip(data);
				return true;
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error reading synchronization data !", e);
				return false;
			}
		}

		private void readZip(byte[] data) throws IOException, JAXBException {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ZipInputStream zipStream = new ZipInputStream(bais);

			InputStream is = readEntry(zipStream, "ui-design-data.xml");
			uiContainer = ExchangeManager.importUiContainer(is);
			is.close();

			is = readEntry(zipStream, "ui-components-data.xml");
			netContainer = ExchangeManager.importNetContainer(is);
			is.close();

			zipStream.close();
		}

		private InputStream readEntry(ZipInputStream zipStream, String name)
				throws IOException {
			ZipEntry entry = zipStream.getNextEntry();
			Validate.isTrue(entry.getName().equalsIgnoreCase(name));
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			IOUtils.copy(zipStream, data);
			return new ByteArrayInputStream(data.toByteArray());
		}

		public XmlUiContainer getUiContainer() {
			return uiContainer;
		}

		public XmlNetContainer getNetContainer() {
			return netContainer;
		}
	}

	private void sync() {
		this.uiContainer = dataBuilder.getUiContainer();
		this.netContainer = dataBuilder.getNetContainer();
		dataBuilder = null;
		this.sync = true;

		DataUpdatedListener[] localListeners;
		synchronized (listeners) {
			localListeners = listeners.toArray(new DataUpdatedListener[0]);
		}
		for (DataUpdatedListener listener : localListeners)
			listener.dataSynchronized(uiContainer, netContainer);
		
		log.info("Synchronized");
	}

	private void desync() {
		this.uiContainer = null;
		this.netContainer = null;
		dataBuilder = null;

		if (this.sync) {
			this.sync = false;
			DataUpdatedListener[] localListeners;
			synchronized (listeners) {
				localListeners = listeners.toArray(new DataUpdatedListener[0]);
			}
			for (DataUpdatedListener listener : localListeners)
				listener.dataDesynchronized();
		}
		
		log.info("Desynchronized");
	}
}
