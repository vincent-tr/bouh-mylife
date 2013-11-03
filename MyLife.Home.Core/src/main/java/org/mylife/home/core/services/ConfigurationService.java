package org.mylife.home.core.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.mylife.home.core.data.DataAccess;
import org.mylife.home.core.data.DataConfiguration;
import org.mylife.home.core.exchange.XmlCoreContainer;
import org.mylife.home.net.exchange.XmlNetContainer;

/**
 * Service de gestion des configurations
 * 
 * @author trumpffv
 * 
 */
public class ConfigurationService implements Service {

	/* internal */ConfigurationService() {

	}

	@Override
	public void terminate() {

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
		DataAccess access = new DataAccess();
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
		DataAccess access = new DataAccess();
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
		DataAccess access = new DataAccess();
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
		DataAccess access = new DataAccess();
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
		DataAccess access = new DataAccess();
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
		DataAccess access = new DataAccess();
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
		DataAccess access = new DataAccess();
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
			config.setType("net");
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
			config.setType("core");
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
