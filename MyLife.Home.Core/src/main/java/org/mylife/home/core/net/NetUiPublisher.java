package org.mylife.home.core.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.mylife.home.net.Configuration;
import org.mylife.home.net.NetRepository;
import org.mylife.home.net.exchange.ExchangeManager;
import org.mylife.home.net.exchange.net.XmlNetContainer;
import org.mylife.home.net.exchange.ui.XmlUiContainer;
import org.mylife.home.net.irc.IRCNetConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

import com.google.common.base.Splitter;

public class NetUiPublisher implements IRCEventListener {

	/**
	 * Nick
	 */
	private static final String ID = "ui-publisher";

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
	 * Longueur de données utiles dans une ligne
	 */
	private static final int LINE_LEN = 400;

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(NetUiPublisher.class
			.getName());

	/**
	 * Connexion irc
	 */
	private final IRCNetConnection connection;

	/**
	 * Données d'ui compressées, découpées en lignes
	 */
	private final Collection<String> lines;

	/**
	 * Hashcode
	 */
	private final int hashCode;

	/**
	 * Constructeur par défaut
	 * 
	 * @param uiDesign
	 * @param uiComponents
	 * @throws IOException
	 * @throws JAXBException
	 */
	public NetUiPublisher(XmlUiContainer uiDesign, XmlNetContainer uiComponents)
			throws IOException, JAXBException {
		byte[] uiDesignRaw = containerToRaw(uiDesign);
		byte[] uiComponentsRaw = containerToRaw(uiComponents);
		String stringValue = buildZipBase64(uiDesignRaw, uiComponentsRaw);
		hashCode = stringValue.hashCode();
		lines = Collections.unmodifiableList(Splitter.fixedLength(LINE_LEN)
				.splitToList(stringValue));

		String server = Configuration.getInstance().getProperty("ircserver");
		connection = new IRCNetConnection(server, 6667, getNick(), ID);
		connection.addIRCEventListener(this);
		connection.setPong(true);
		connection.start();
	}

	private byte[] containerToRaw(XmlUiContainer container)
			throws JAXBException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ExchangeManager.exportUiContainer(container, baos);
		return baos.toByteArray();
	}

	private byte[] containerToRaw(XmlNetContainer container)
			throws JAXBException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ExchangeManager.exportNetContainer(container, baos);
		return baos.toByteArray();
	}

	private ZipEntry createZipEntry(String name) {
		ZipEntry entry = new ZipEntry(name);
		entry.setMethod(ZipEntry.DEFLATED);
		entry.setTime(0); // Sinon le hash est tout le temps différent
		return entry;
	}
	
	private String buildZipBase64(byte[] uiDesignRaw, byte[] uiComponentsRaw)
			throws IOException {
		ByteArrayOutputStream store = new ByteArrayOutputStream();
		ZipOutputStream zipStream = new ZipOutputStream(store);
		zipStream.putNextEntry(createZipEntry("ui-design-data.xml"));
		zipStream.write(uiDesignRaw);
		zipStream.closeEntry();
		zipStream.putNextEntry(createZipEntry("ui-components-data.xml"));
		zipStream.write(uiComponentsRaw);
		zipStream.closeEntry();
		zipStream.close();
		byte[] zipData = store.toByteArray();

		return new String(Base64.encodeBase64(zipData), "UTF-8");
	}

	/**
	 * Fermeture du publisher
	 */
	public void close() {
		connection.doQuit();
		connection.stop();
	}

	private String getNick() {
		return ID + "|" + hashCode;
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

	@Override
	public void onRegistered() {
		connection.doJoin("#" + NetRepository.CHANNL_UI);
	}

	@Override
	public void onDisconnected() {
		log.severe("Connection broken !");
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
		// nothing
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
		if (target.equalsIgnoreCase("#" + NetRepository.CHANNL_UI)) {

			// message sur channel : on doit voir si le 1er token est notre id
			StringTokenizer tokenizer = new StringTokenizer(msg);
			if (tokenizer.hasMoreTokens()) {
				String chanTarget = tokenizer.nextToken();
				if (chanTarget.equalsIgnoreCase(ID)) {
					processCommand(user, tokenizer);
				}
			}

		} else if (target.equalsIgnoreCase(getNick())) {

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
	private void processCommand(IRCUser from, String command,
			List<String> args) {

		if (!command.equalsIgnoreCase(UI_DATA_COMMAND))
			return;
		if(args.size() < 1)
			return;
		log.info("Executing command : " + UI_DATA_COMMAND + " from :" + from.getNick());

		// Ce mode de réponse permet d'être exécuté sur la partie distante comme
		// une action
		for (String line : lines) {
			send(from.getNick(), UI_DATA_PREFIX + " " + line);
		}
		send(from.getNick(), UI_DATA_END);

		log.info("Command executed : " + UI_DATA_COMMAND);
	}
}
