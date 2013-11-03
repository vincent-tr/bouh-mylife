package org.mylife.home.core.services;

import java.util.List;

import org.mylife.home.core.data.DataAccess;
import org.mylife.home.core.data.DataPlugin;

/**
 * Service de gestion des plugins
 * @author pumbawoman
 *
 */
public class PluginService implements Service {

	/* internal */ PluginService() {
		
	}
	
	public void terminate() {
		
	}

	/**
	 * Obtention de tous les plugins
	 * @return
	 */
	public List<DataPlugin> list() {
		DataAccess access = new DataAccess();
		try {
			return access.getPluginsAll();
		} finally {
			access.close();
		}
	}

	/**
	 * Obtention d'un plugin
	 * @param id
	 * @return
	 */
	public DataPlugin get(int id) {
		DataAccess access = new DataAccess();
		try {
			return access.getPluginByKey(id);
		} finally {
			access.close();
		}

	}

	/**
	 * Changement du commentaire d'un plugin
	 * @param id
	 * @param comment
	 */
	public void changeComment(int id, String comment) {
		DataAccess access = new DataAccess();
		try {
			DataPlugin item = access.getPluginByKey(id);
			item.setComment(comment);
			access.updatePlugin(item);
		} finally {
			access.close();
		}
	}

	/**
	 * Changement de l'activation d'un plugin
	 * @param id
	 * @param active
	 */
	public void changeActive(int id, boolean active) {
		DataAccess access = new DataAccess();
		try {
			DataPlugin item = access.getPluginByKey(id);
			item.setActive(active);
			access.updatePlugin(item);
		} finally {
			access.close();
		}
	}

	/**
	 * Suppression d'un plugin
	 * @param id
	 */
	public void delete(int id) {
		DataAccess access = new DataAccess();
		try {
			DataPlugin item = new DataPlugin();
			item.setId(id);
			access.deletePlugin(item);
		} finally {
			access.close();
		}
	}
	
	/**
	 * Création d'un plugin
	 * @param plugin
	 */
	public void create(DataPlugin plugin) {
		DataAccess access = new DataAccess();
		try {
			access.createPlugin(plugin);
		} finally {
			access.close();
		}
	}
	
	/**
	 * Création d'un plugin à partir d'un jar uniquement
	 * @param data
	 */
	public void createFromJar(byte[] data) {
		throw new UnsupportedOperationException();
	}
}
