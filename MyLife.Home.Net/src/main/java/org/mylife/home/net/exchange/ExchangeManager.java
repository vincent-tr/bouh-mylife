package org.mylife.home.net.exchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.mylife.home.net.NetObject;
import org.mylife.home.net.exchange.core.XmlCoreContainer;
import org.mylife.home.net.exchange.design.XmlDesignContainer;
import org.mylife.home.net.exchange.net.XmlNetAction;
import org.mylife.home.net.exchange.net.XmlNetAttribute;
import org.mylife.home.net.exchange.net.XmlNetClass;
import org.mylife.home.net.exchange.net.XmlNetContainer;
import org.mylife.home.net.exchange.net.XmlNetEnum;
import org.mylife.home.net.exchange.net.XmlNetMember;
import org.mylife.home.net.exchange.net.XmlNetObject;
import org.mylife.home.net.exchange.net.XmlNetRange;
import org.mylife.home.net.exchange.net.XmlNetType;
import org.mylife.home.net.exchange.ui.XmlUiContainer;
import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetClass;
import org.mylife.home.net.structure.NetEnum;
import org.mylife.home.net.structure.NetMember;
import org.mylife.home.net.structure.NetRange;
import org.mylife.home.net.structure.NetType;

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
	public static void exportNetContainer(XmlNetContainer container,
			OutputStream stream) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlNetContainer.class);
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
	public static XmlNetContainer importNetContainer(InputStream stream)
			throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlNetContainer.class);
		Unmarshaller u = jc.createUnmarshaller();
		return (XmlNetContainer) u.unmarshal(stream);
	}

	/**
	 * Marshalling
	 * 
	 * @param obj
	 * @return
	 */
	public static XmlNetObject marshal(NetObject net) {
		XmlNetObject xml = new XmlNetObject();
		xml.id = net.getId();
		xml.clazz = marshal(net.getNetClass());
		return xml;
	}

	private static XmlNetClass marshal(NetClass net) {
		XmlNetClass xml = new XmlNetClass();
		List<XmlNetMember> xmlMembers = new ArrayList<XmlNetMember>();
		for (NetMember netMember : net.getMembers())
			xmlMembers.add(marshal(netMember));
		xml.members = xmlMembers.toArray(new XmlNetMember[xmlMembers.size()]);
		return xml;
	}

	private static XmlNetMember marshal(NetMember net) {
		XmlNetMember xml = null;
		if (net instanceof NetAttribute)
			xml = marshal((NetAttribute) net);
		else if (net instanceof NetAction)
			xml = marshal((NetAction) net);
		xml.index = net.getIndex();
		xml.name = net.getName();
		return xml;
	}

	private static XmlNetAttribute marshal(NetAttribute net) {
		XmlNetAttribute xml = new XmlNetAttribute();
		xml.type = marshal(net.getType());
		return xml;
	}

	private static XmlNetAction marshal(NetAction net) {
		XmlNetAction xml = new XmlNetAction();
		List<XmlNetType> xmlArgs = new ArrayList<XmlNetType>();
		for (NetType netArg : net.getArguments())
			xmlArgs.add(marshal(netArg));
		xml.arguments = xmlArgs.toArray(new XmlNetType[xmlArgs.size()]);
		return xml;
	}

	public static XmlNetType marshal(NetType net) {
		XmlNetType xml = null;
		if (net instanceof NetRange)
			xml = marshal((NetRange) net);
		else if (net instanceof NetEnum)
			xml = marshal((NetEnum) net);
		return xml;
	}

	private static XmlNetRange marshal(NetRange net) {
		XmlNetRange xml = new XmlNetRange();
		xml.min = net.getMin();
		xml.max = net.getMax();
		return xml;
	}

	private static XmlNetEnum marshal(NetEnum net) {
		XmlNetEnum xml = new XmlNetEnum();
		xml.values = net.getValues()
				.toArray(new String[net.getValues().size()]);
		return xml;
	}

	/**
	 * Unmarshalling
	 * 
	 * @param xml
	 * @return
	 */
	public static NetObject unmarshal(XmlNetObject xml) {
		NetClass clazz = unmarshal(xml.clazz);
		return new NetObject(xml.id, clazz);
	}

	private static NetClass unmarshal(XmlNetClass xml) {
		List<NetMember> netMembers = new ArrayList<NetMember>();
		for (XmlNetMember xmlMember : xml.members)
			netMembers.add(unmarshal(xmlMember));
		return new NetClass(netMembers);
	}

	private static NetMember unmarshal(XmlNetMember xml) {
		if (xml instanceof XmlNetAttribute) {
			return new NetAttribute(xml.index, xml.name,
					unmarshal(((XmlNetAttribute) xml).type));
		} else if (xml instanceof XmlNetAction) {
			List<NetType> netArgs = new ArrayList<NetType>();
			XmlNetType[] arguments = ((XmlNetAction) xml).arguments;
			if (arguments != null) {
				for (XmlNetType xmlArg : arguments)
					netArgs.add(unmarshal(xmlArg));
			}
			return new NetAction(xml.index, xml.name, netArgs);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public static NetType unmarshal(XmlNetType xml) {
		if (xml instanceof XmlNetRange) {
			XmlNetRange xmlRange = (XmlNetRange) xml;
			return new NetRange(xmlRange.min, xmlRange.max);
		} else if (xml instanceof XmlNetEnum) {
			return new NetEnum(((XmlNetEnum) xml).values);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Export
	 * 
	 * @param container
	 * @param stream
	 * @throws JAXBException
	 */
	public static void exportCoreContainer(XmlCoreContainer container,
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
	public static XmlCoreContainer importCoreContainer(InputStream stream)
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
	public static void exportDesignContainer(XmlDesignContainer container,
			OutputStream stream) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlDesignContainer.class);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(container, stream);
	}

	/**
	 * Export
	 * 
	 * @param container
	 * @param stream
	 * @throws JAXBException
	 */
	public static void exportUiContainer(XmlUiContainer container,
			OutputStream stream) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlUiContainer.class);
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
	public static XmlUiContainer importUiContainer(InputStream stream)
			throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XmlUiContainer.class);
		Unmarshaller u = jc.createUnmarshaller();
		return (XmlUiContainer) u.unmarshal(stream);
	}

	/**
	 * Export du schéma
	 * 
	 * @param stream
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void exportNetSchema(final OutputStream stream)
			throws JAXBException, IOException {
		JAXBContext jc = JAXBContext.newInstance(XmlNetContainer.class);
		jc.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri,
					String suggestedFileName) throws IOException {
				StreamResult sr = new StreamResult(stream);
				sr.setSystemId(String.valueOf(stream.hashCode()));
				return sr;
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
	public static void exportCoreSchema(final OutputStream stream)
			throws JAXBException, IOException {
		JAXBContext jc = JAXBContext.newInstance(XmlCoreContainer.class);
		jc.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri,
					String suggestedFileName) throws IOException {
				StreamResult sr = new StreamResult(stream);
				sr.setSystemId(String.valueOf(stream.hashCode()));
				return sr;
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
				StreamResult sr = new StreamResult(stream);
				sr.setSystemId(String.valueOf(stream.hashCode()));
				return sr;
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
				StreamResult sr = new StreamResult(stream);
				sr.setSystemId(String.valueOf(stream.hashCode()));
				return sr;
			}
		});
	}
}
