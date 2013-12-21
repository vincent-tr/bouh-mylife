package org.mylife.home.core.exchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.mylife.home.core.exchange.core.XmlCoreContainer;
import org.mylife.home.core.exchange.design.XmlDesignContainer;
import org.mylife.home.core.exchange.ui.XmlUiContainer;

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
	public static void exportContainer(XmlCoreContainer container,
			OutputStream stream) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlCoreContainer.class);
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
	public static XmlCoreContainer importContainer(InputStream stream)
			throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlCoreContainer.class);
		Unmarshaller u = jc.createUnmarshaller();
		return (XmlCoreContainer) u.unmarshal(stream);
	}

	/**
	 * Export
	 * 
	 * @param container
	 * @param stream
	 * @throws JAXBException
	 */
	public static void exportContainer(XmlDesignContainer container,
			OutputStream stream) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlDesignContainer.class);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(container, stream);
	}

	/**
	 * Export du schéma
	 * 
	 * @param stream
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void exportCoreSchema(final OutputStream stream)
			throws JAXBException, IOException {
		JAXBContext jc = JAXBContext.newInstance(XmlCoreContainer.class);
		jc.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri,
					String suggestedFileName) throws IOException {
				return new StreamResult(stream);
			}
		});
	}

	/**
	 * Export du schéma
	 * 
	 * @param stream
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void exportDesignSchema(final OutputStream stream)
			throws JAXBException, IOException {
		JAXBContext jc = JAXBContext.newInstance(XmlDesignContainer.class);
		jc.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri,
					String suggestedFileName) throws IOException {
				return new StreamResult(stream);
			}
		});
	}

	/**
	 * Export du schéma
	 * 
	 * @param stream
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void exportUiSchema(final OutputStream stream)
			throws JAXBException, IOException {
		JAXBContext jc = JAXBContext.newInstance(XmlUiContainer.class);
		jc.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri,
					String suggestedFileName) throws IOException {
				return new StreamResult(stream);
			}
		});
	}
}
