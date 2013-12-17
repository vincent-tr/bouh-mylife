package org.mylife.home.core.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.mylife.home.common.services.Service;
import org.mylife.home.core.data.DataPlugin;
import org.mylife.home.core.data.DataPluginAccess;
import org.mylife.home.core.plugins.PluginFactory;

/**
 * Service de gestion des plugins
 * 
 * @author pumbawoman
 * 
 */
public class PluginService implements Service {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(PluginService.class
			.getName());

	/* internal */PluginService() {
		initFactories();
	}

	@Override
	public void terminate() {

	}

	/**
	 * Obtention de tous les plugins
	 * 
	 * @return
	 */
	public List<DataPlugin> list() {
		DataPluginAccess access = new DataPluginAccess();
		try {
			return access.getPluginsAll();
		} finally {
			access.close();
		}
	}

	/**
	 * Obtention d'un plugin
	 * 
	 * @param id
	 * @return
	 */
	public DataPlugin get(int id) {
		DataPluginAccess access = new DataPluginAccess();
		try {
			return access.getPluginByKey(id);
		} finally {
			access.close();
		}

	}

	/**
	 * Changement du commentaire d'un plugin
	 * 
	 * @param id
	 * @param comment
	 */
	public void changeComment(int id, String comment) {
		DataPluginAccess access = new DataPluginAccess();
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
	 * 
	 * @param id
	 * @param active
	 */
	public void changeActive(int id, boolean active) {
		DataPluginAccess access = new DataPluginAccess();
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
	 * 
	 * @param id
	 */
	public void delete(int id) {
		DataPluginAccess access = new DataPluginAccess();
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
	 * 
	 * @param plugin
	 */
	public void create(DataPlugin plugin) {
		DataPluginAccess access = new DataPluginAccess();
		try {
			access.createPlugin(plugin);
		} finally {
			access.close();
		}
	}

	/**
	 * Création d'un plugin à partir d'un jar uniquement
	 * 
	 * @param data
	 */
	public void createFromJar(byte[] data) {
		throw new UnsupportedOperationException();
	}
	
	// ------------------------------------------------------------------------------------------------------------

	private void initFactories() {
		log.finest("Loadind factories");
		Map<String, PluginFactory> map = new HashMap<String, PluginFactory>();
		ServiceLoader<PluginFactory> serviceLoader = ServiceLoader
				.load(PluginFactory.class);
		for (PluginFactory factory : serviceLoader) {
			String type = factory.getType();
			if (map.containsKey(type))
				throw new UnsupportedOperationException("Factory with type '"
						+ type + "' already exists");
			map.put(type, factory);
			log.finest("Factory loaded : " + factory.getClass().toString());
		}
		factories = Collections.unmodifiableMap(map);
		log.finest("Factories loading terminated");
	}

	/**
	 * Fabriques de composants
	 */
	private Map<String, PluginFactory> factories;

	/**
	 * Obtention des fabriques de composants présentes dans le système
	 * 
	 * @return
	 */
	public Collection<PluginFactory> getFactories() {
		return factories.values();
	}

	/**
	 * Obtention d'une fabrique par type de composant
	 * 
	 * @param type
	 * @return
	 */
	public PluginFactory getFactory(String type) {
		Validate.notEmpty(type);
		return factories.get(type);
	}
}
