package mylife.home.irc.server;

import java.util.HashMap;
import java.util.Map;

import mylife.home.irc.server.commands.Command;

/**
 * Configuration d'un serveur
 * @author pumbawoman
 *
 */
public class Configuration {
	
	/**
	 * Nom du serveur
	 */
	private String name;
	
	/**
	 * Token (ou numeric) du serveur, doit être unique sur le réseau
	 */
	private int token;
	
	/**
	 * Infos sur le serveur
	 */
	private String info;

	/**
	 * Port d'écoute du serveur
	 */
	private int listenPort;
	
	/**
	 * Taille du buffer de réception
	 */
	private int recvBufferSize;
	
	/**
	 * Taille du buffer d'envoi
	 */
	private int sendBufferSize;
	
	/**
	 * Liste des commandes à implémenter
	 */
	private final Map<String, Command> commands = new HashMap<String, Command>();

	/**
	 * Nom du serveur
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Nom du serveur
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Token (ou numeric) du serveur, doit être unique sur le réseau
	 * @return
	 */
	public int getToken() {
		return token;
	}

	/**
	 * Token (ou numeric) du serveur, doit être unique sur le réseau
	 * @param token
	 */
	public void setToken(int token) {
		this.token = token;
	}

	/**
	 * Infos sur le serveur
	 * @return
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Infos sur le serveur
	 * @param info
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * Port d'écoute du serveur
	 * @return
	 */
	public int getListenPort() {
		return listenPort;
	}
	
	/**
	 * Port d'écoute du serveur
	 * @param listenPort
	 */
	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

	/**
	 * Taille du buffer de réception
	 * @return
	 */
	public int getRecvBufferSize() {
		return recvBufferSize;
	}

	/**
	 * Taille du buffer de réception
	 * @param recvBufferSize
	 */
	public void setRecvBufferSize(int recvBufferSize) {
		this.recvBufferSize = recvBufferSize;
	}

	/**
	 * Taille du buffer d'envoi
	 * @return
	 */
	public int getSendBufferSize() {
		return sendBufferSize;
	}

	/**
	 * 
	 * @param sendBufferSize
	 */
	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	/**
	 * Liste des commandes à implémenter
	 * @return
	 */
	public Map<String, Command> getCommands() {
		return commands;
	}
}
