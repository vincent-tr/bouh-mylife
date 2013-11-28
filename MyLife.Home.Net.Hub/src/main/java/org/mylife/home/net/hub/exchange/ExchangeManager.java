package org.mylife.home.net.hub.exchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.mylife.home.common.NetUtils;
import org.mylife.home.net.hub.configuration.IrcBinding;
import org.mylife.home.net.hub.configuration.IrcConfiguration;
import org.mylife.home.net.hub.configuration.IrcLinkAccept;
import org.mylife.home.net.hub.configuration.IrcLinkConnect;
import org.mylife.home.net.hub.configuration.IrcOperator;
import org.mylife.home.net.hub.services.ServiceAccess;

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
		private String serverDescription;
		private int serverToken;
		private int pingIntervalMs;
		private int pingTimeoutMs;
		private String location1;
		private String location2;
		private String email;
		private String serverInfoContent;
		private String serverMotdContent;
		private final Collection<IrcBinding> bindings = new ArrayList<IrcBinding>();
		private final Collection<IrcOperator> operators = new ArrayList<IrcOperator>();

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

		public String getServerDescription() {
			return serverDescription;
		}

		public void setServerDescription(String serverDescription) {
			this.serverDescription = serverDescription;
		}

		public int getServerToken() {
			return serverToken;
		}

		public void setServerToken(int serverToken) {
			this.serverToken = serverToken;
		}

		public int getPingIntervalMs() {
			return pingIntervalMs;
		}

		public void setPingIntervalMs(int pingIntervalMs) {
			this.pingIntervalMs = pingIntervalMs;
		}

		public int getPingTimeoutMs() {
			return pingTimeoutMs;
		}

		public void setPingTimeoutMs(int pingTimeoutMs) {
			this.pingTimeoutMs = pingTimeoutMs;
		}

		public String getLocation1() {
			return location1;
		}

		public void setLocation1(String location1) {
			this.location1 = location1;
		}

		public String getLocation2() {
			return location2;
		}

		public void setLocation2(String location2) {
			this.location2 = location2;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getServerInfoContent() {
			return serverInfoContent;
		}

		public void setServerInfoContent(String serverInfoContent) {
			this.serverInfoContent = serverInfoContent;
		}

		public String getServerMotdContent() {
			return serverMotdContent;
		}

		public void setServerMotdContent(String serverMotdContent) {
			this.serverMotdContent = serverMotdContent;
		}

		public Collection<IrcLinkAccept> getLinksAccept() {
			return ServiceAccess.getInstance().getLinkService().getLinksAccept();
		}

		public Collection<IrcLinkConnect> getLinksConnect() {
			return ServiceAccess.getInstance().getLinkService().getLinksConnect();
		}

		public Collection<IrcBinding> getBindings() {
			return bindings;
		}

		public Collection<IrcOperator> getOperators() {
			return operators;
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
	public static IrcConfiguration marshal(XmlIrcConfiguration container,
			Collection<IrcLinkAccept> linksAccept,
			Collection<IrcLinkConnect> linksConnect) {
		
		IrcConfigImpl impl = new IrcConfigImpl();
		
		impl.setNetworkName(container.networkName);
		impl.setServerName(container.serverName);
		impl.setServerDescription(container.serverDescription);
		impl.setServerToken(container.serverToken);
		impl.setPingIntervalMs(container.pingIntervalMs);
		impl.setPingTimeoutMs(container.pingTimeoutMs);
		impl.setLocation1(container.location1);
		impl.setLocation2(container.location2);
		impl.setEmail(container.email);
		impl.setServerInfoContent(container.serverInfoContent);
		impl.setServerMotdContent(container.serverMotdContent);
		
		if(container.bindings != null) {
			for(XmlIrcBinding xmlBinding : container.bindings) {
				impl.getBindings().add(marshal(xmlBinding));
			}
		}
		
		if(container.operators != null) {
			for(XmlIrcOperator xmlOperator : container.operators) {
				impl.getOperators().add(marshal(xmlOperator));
			}
		}
		
		return impl;
		
	}
	
	private static IrcBinding marshal(XmlIrcBinding binding) {
		
		String address = binding.address;
		if("@host".equalsIgnoreCase(address)) {
			try {
				address = NetUtils.getPublicAddress().getHostAddress();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
			
		return new IrcBinding(address, binding.port, binding.ssl);
	}
	
	private static IrcOperator marshal(XmlIrcOperator operator) {
		return new IrcOperator(operator.name, operator.host, operator.pass);
	}
}
