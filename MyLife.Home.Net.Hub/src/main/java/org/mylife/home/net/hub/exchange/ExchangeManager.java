package org.mylife.home.net.hub.exchange;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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
}
