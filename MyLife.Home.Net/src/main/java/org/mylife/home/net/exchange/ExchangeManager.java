package org.mylife.home.net.exchange;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Gestion des échanges
 * 
 * @author trumpffv
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
	public static void exportContainer(XmlContainer container,
			OutputStream stream) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
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
	public static XmlContainer importContainer(InputStream stream)
			throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Unmarshaller u = jc.createUnmarshaller();
		return (XmlContainer)u.unmarshal(stream);
	}

}
