package org.mylife.home.net.hub.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.mylife.home.common.services.BaseManagerService;
import org.mylife.home.net.hub.IrcServer;
import org.mylife.home.net.hub.IrcServerMBean;
import org.mylife.home.net.hub.configuration.IrcConfiguration;
import org.mylife.home.net.hub.configuration.IrcLinkAccept;
import org.mylife.home.net.hub.configuration.IrcLinkConnect;
import org.mylife.home.net.hub.exchange.ExchangeManager;
import org.mylife.home.net.hub.exchange.XmlIrcConfiguration;

/**
 * Service de gestion
 * 
 * @author pumbawoman
 * 
 */
public class ManagerService extends BaseManagerService {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(ManagerService.class
			.getName());

	/**
	 * Serveur
	 */
	private IrcServer server;
	
	/* internal */ManagerService() {
	}
	
	private IrcConfiguration loadConfiguration() throws JAXBException {
		
		// TODO
		Collection<IrcLinkAccept> acceptLinks = new ArrayList<IrcLinkAccept>();
		Collection<IrcLinkConnect> connectLinks = new ArrayList<IrcLinkConnect>();

		InputStream configStream = this.getClass().getClassLoader().getResourceAsStream("ircConfiguration.xml");
		XmlIrcConfiguration container = ExchangeManager.importContainer(configStream);
		return ExchangeManager.marshal(container, acceptLinks, connectLinks);
	}
	
	/**
	 * Démarrage du service
	 */
	@Override
	protected void executeStart() throws Exception {
		
		// Chargement de la configuration
		IrcConfiguration config = loadConfiguration();
		server = new IrcServer(config);
		server.start();
		
		ServiceAccess.getInstance().getLinkService().startAutoLinks(server);
	}

	/**
	 * Arrêt du service
	 */
	@Override
	protected void executeStop() throws Exception {
		ServiceAccess.getInstance().getLinkService().stopAutoLinks();

		server.stop();
		server = null;
	}
	
	/**
	 * Obtention du serveur, s'il est instancié
	 * @return
	 */
	public IrcServerMBean getServer() {
		return server;
	}
}
