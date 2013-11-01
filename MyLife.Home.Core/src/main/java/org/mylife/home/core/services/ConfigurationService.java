package org.mylife.home.core.services;

import java.util.Date;
import java.util.List;

import org.mylife.home.core.data.DataAccess;
import org.mylife.home.core.data.DataConfiguration;

/**
 * Service de gestion des configurations
 * @author trumpffv
 *
 */
public class ConfigurationService {

	/* internal */ ConfigurationService() {
		
	}
	

	/**
	 * Obtention de toutes les configurations
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
	 * @param data
	 */
	public void createFromContents(byte[] data) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Création d'une configuration à partir d'une url de contenu
	 * @param url
	 */
	public void createFromContentsUrl(String url) {
		throw new UnsupportedOperationException();
	}
}
