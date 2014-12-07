package org.mylife.home.core.plugins;

import java.util.Collection;
import java.util.Map;

import org.mylife.home.net.NetObject;

/**
 * Vue d'un plugin
 * 
 * @author pumbawoman
 * 
 */
public class PluginView {

	/**
	 * Identifiant du plugin
	 */
	private final String id;

	/**
	 * Fabrique du plugin
	 */
	private final PluginFactory factory;

	/**
	 * Configuration du plugin
	 */
	private final Map<String, String> configuration;

	/**
	 * Objets publiés par le plugin
	 */
	private final Collection<NetObject> publishedObjects;

	public PluginView(String id, PluginFactory factory,
			Map<String, String> configuration,
			Collection<NetObject> publishedObjects) {
		this.id = id;
		this.factory = factory;
		this.configuration = configuration;
		this.publishedObjects = publishedObjects;
	}
	/**
	 * Identifiant du plugin
	 */
	public String getId() {
		return id;
	}

	/**
	 * Type du plugin
	 */
	public String getType() {
		return factory.getType();
	}

	/**
	 * Type destiné à l'affichage
	 */
	public String getDisplayType() {
		return factory.getDisplayType();
	}

	/**
	 * Plugin d'ui ?
	 */
	public boolean isUi() {
		return factory.getDesignMetadata().isUi();
	}

	/**
	 * Configuration du plugin
	 */
	public Map<String, String> getConfiguration() {
		return configuration;
	}

	/**
	 * Objets publiés par le plugin
	 */
	public Collection<NetObject> getPublishedObjects() {
		return publishedObjects;
	}

}
