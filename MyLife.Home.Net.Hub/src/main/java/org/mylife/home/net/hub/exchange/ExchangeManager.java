package org.mylife.home.net.hub.exchange;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.mylife.home.net.hub.irc.IrcConfiguration;

/**
 * Gestion des échanges
 * 
 * @author pumbawoman
 * 
 */
public class ExchangeManager {

	/**
	 * Export
	 * 
	 * @param container
	 * @param stream
	 * @throws JAXBException
	 */
	public static void exportContainer(XmlIrcConfiguration container,
			OutputStream stream) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlIrcConfiguration.class);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(container, stream);
	}

	/**
	 * Import
	 * 
	 * @param stream
	 * @return
	 * @throws JAXBException
	 */
	public static XmlIrcConfiguration importContainer(InputStream stream)
			throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlIrcConfiguration.class);
		Unmarshaller u = jc.createUnmarshaller();
		return (XmlIrcConfiguration) u.unmarshal(stream);
	}

	/**
	 * Implémentation interne de IrcConfiguration
	 * 
	 * @author pumbawoman
	 * 
	 */
	private static class IrcConfigImpl implements IrcConfiguration {

		private String networkName;
		private String serverName;
		private int serverToken;
		private final Collection<IrcConfiguration.Listener> listeners = new ArrayList<IrcConfiguration.Listener>();

		public IrcConfigImpl() {
		}
		
		public String getNetworkName() {
			return networkName;
		}

		public void setNetworkName(String networkName) {
			this.networkName = networkName;
		}

		public String getServerName() {
			return serverName;
		}

		public void setServerName(String serverName) {
			this.serverName = serverName;
		}

		public int getServerToken() {
			return serverToken;
		}

		public void setServerToken(int serverToken) {
			this.serverToken = serverToken;
		}

		public Collection<IrcConfiguration.Listener> getListeners() {
			return listeners;
		}
	}
	
	private static class IrcListenerImpl implements IrcConfiguration.Listener {

		private final String address;
		private final int port;
		
		public IrcListenerImpl(String address, int port) {
			this.address = address;
			this.port = port;
		}
		
		@Override
		public String getAddress() {
			return address;
		}

		@Override
		public int getPort() {
			return port;
		}
		
	}

	/**
	 * Lecture
	 * 
	 * @param container
	 * @param linksAccept
	 * @param linksConnect
	 * @return
	 */
	public static IrcConfiguration marshal(XmlIrcConfiguration container) {
		
		IrcConfigImpl impl = new IrcConfigImpl();
		
		String serverName = container.serverName;
		if(StringUtils.isEmpty(serverName))
			serverName = null;
		impl.setNetworkName(container.networkName);
		impl.setServerName(serverName);
		impl.setServerToken(container.serverToken);
		
		if(container.listeners != null) {
			for(XmlIrcListener xmlListener : container.listeners) {
				impl.getListeners().add(marshal(xmlListener));
			}
		}
		
		return impl;
		
	}
	
	private static IrcConfiguration.Listener marshal(XmlIrcListener listener) {
		
		String address = listener.address;
		if(StringUtils.isEmpty(address))
			address = null;
		return new IrcListenerImpl(address, listener.port);
	}
}
