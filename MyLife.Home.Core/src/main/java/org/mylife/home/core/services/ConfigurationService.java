package org.mylife.home.core.services;

import java.util.Set;

import org.mylife.home.core.data.DataAccess;
import org.mylife.home.core.data.DataConfiguration;

/**
 * Service de configuration
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
	public Set<DataConfiguration> list() {
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
}
