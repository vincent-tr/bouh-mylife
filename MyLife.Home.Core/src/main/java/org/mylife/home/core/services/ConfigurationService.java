package org.mylife.home.core.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.mylife.home.common.services.Service;
import org.mylife.home.core.data.DataConfigurationAccess;
import org.mylife.home.core.data.DataConfiguration;
import org.mylife.home.core.exchange.core.XmlCoreContainer;
import org.mylife.home.net.exchange.XmlNetContainer;

/**
 * Service de gestion des configurations
 * 
 * @author trumpffv
 * 
 */
public class ConfigurationService implements Service {

	public final static String TYPE_CORE = "core";
	public final static String TYPE_NET = "net";

	private final Map<String, String> types;

	/* internal */ConfigurationService() {

		Map<String, String> map = new HashMap<String, String>();
		map.put(TYPE_CORE, "Core");
		map.put(TYPE_NET, "Net");
		types = Collections.unmodifiableMap(map);
	}

	@Override
	public void terminate() {

	}

	public Map<String, String> listTypes() {
		return types;
	}

	/**
	 * Lecture des configurations actives
	 * 
	 * @param netList
	 * @param coreList
	 */
	public void loadActives(List<XmlNetContainer> netList,
			List<XmlCoreContainer> coreList) {
		List<DataConfiguration> list = listActives();
		for (DataConfiguration item : list) {
			if (tryReadNet(item.getContent(), netList))
				continue;
			if (tryReadCore(item.getContent(), coreList))
				continue;
			throw new UnsupportedOperationException(
					"Invalid configuration content wirth id : " + item.getId());
		}
	}

	private boolean tryReadNet(byte[] data, List<XmlNetContainer> netList) {
		try {
			XmlNetContainer container = org.mylife.home.net.exchange.ExchangeManager
					.importContainer(new ByteArrayInputStream(data));
			netList.add(container);
			return true;
		} catch (JAXBException e) {
			return false;
		}
	}

	private boolean tryReadCore(byte[] data, List<XmlCoreContainer> coreList) {
		try {
			XmlCoreContainer container = org.mylife.home.core.exchange.ExchangeManager
					.importContainer(new ByteArrayInputStream(data));
			coreList.add(container);
			return true;
		} catch (JAXBException e) {
			return false;
		}
	}

	/**
	 * Obtention des configurations actives
	 * 
	 * @return
	 */
	private List<DataConfiguration> listActives() {
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			return access.getConfigurationsActives();
		} finally {
			access.close();
		}
	}

	/**
	 * Obtention de toutes les configurations
	 * 
	 * @return
	 */
	public List<DataConfiguration> list() {
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			return access.getConfigurationsAll();
		} finally {
			access.close();
		}
	}

	/**
	 * Obtention d'une configuration
	 * 
	 * @param id
	 * @return
	 */
	public DataConfiguration get(int id) {
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			return access.getConfigurationByKey(id);
		} finally {
			access.close();
		}

	}

	/**
	 * Changement du commentaire d'une configuration
	 * 
	 * @param id
	 * @param comment
	 */
	public void changeComment(int id, String comment) {
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			DataConfiguration item = access.getConfigurationByKey(id);
			item.setComment(comment);
			access.updateConfiguration(item);
		} finally {
			access.close();
		}
	}

	/**
	 * Changement de l'activation d'une configuration
	 * 
	 * @param id
	 * @param active
	 */
	public void changeActive(int id, boolean active) {
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			DataConfiguration item = access.getConfigurationByKey(id);
			item.setActive(active);
			access.updateConfiguration(item);
		} finally {
			access.close();
		}
	}

	/**
	 * Suppression d'une configuration
	 * 
	 * @param id
	 */
	public void delete(int id) {
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			DataConfiguration item = new DataConfiguration();
			item.setId(id);
			access.deleteConfiguration(item);
		} finally {
			access.close();
		}
	}

	/**
	 * Création d'une configuration
	 * 
	 * @param config
	 */
	public void create(DataConfiguration config) {
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			config.setDate(new Date());
			config.setActive(false);
			access.createConfiguration(config);
		} finally {
			access.close();
		}
	}

	/**
	 * Création d'une configuration à partir du contenu uniquement
	 * 
	 * @param data
	 */
	public void createFromContents(byte[] data) {
		// tentative de création en net
		DataConfiguration config = null;

		config = tryReadNet(data);
		if (config != null) {
			create(config);
			return;
		}

		config = tryReadCore(data);
		if (config != null) {
			create(config);
			return;
		}

		throw new UnsupportedOperationException("Invalid file");
	}

	private DataConfiguration tryReadNet(byte[] data) {
		try {
			XmlNetContainer container = org.mylife.home.net.exchange.ExchangeManager
					.importContainer(new ByteArrayInputStream(data));

			DataConfiguration config = new DataConfiguration();
			config.setType(TYPE_NET);
			config.setContent(data);
			config.setComment("componentsVersion : "
					+ container.componentsVersion + "\ndocumentVersion : "
					+ container.documentVersion);
			return config;

		} catch (JAXBException e) {
			return null;
		}
	}

	private DataConfiguration tryReadCore(byte[] data) {
		try {
			XmlCoreContainer container = org.mylife.home.core.exchange.ExchangeManager
					.importContainer(new ByteArrayInputStream(data));

			DataConfiguration config = new DataConfiguration();
			config.setType(TYPE_CORE);
			config.setContent(data);
			config.setComment("documentName : " + container.documentName
					+ "\ndocumentVersion : " + container.documentVersion);
			return config;
		} catch (JAXBException e) {
			return null;
		}
	}

	/**
	 * Création d'une configuration à partir d'une url de contenu
	 * 
	 * @param url
	 * @throws IOException
	 */
	public void createFromContentsUrl(String url) throws IOException {
		byte[] data = IOUtils.toByteArray(new URL(url).openStream());
		createFromContents(data);
	}
}
