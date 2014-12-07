package org.mylife.home.core.plugins.design;

import java.util.Collection;

/**
 * Donnée de configuration au design d'un plugin
 * 
 * @author pumbawoman
 * 
 */
public class PluginDesignConfiguration {

	/**
	 * Nom de la donnée
	 */
	private final String name;
	
	/**
	 * Nom d'affichage de la donnée (si null le nom de la donnée sera utilisé)
	 */
	private final String displayName;
	
	/**
	 * Type de la donnée
	 */
	private final Class<?> type;

	/**
	 * Indique si la donnée est obligatoire
	 */
	private final boolean mandatory;

	/**
	 * Indique une liste de valeurs possibles, ou null si pas de liste
	 */
	private final Collection<Object> possibleValues;

	public PluginDesignConfiguration(String name, String displayName, Class<?> type,
			boolean mandatory, Collection<Object> possibleValues) {
		this.name = name;
		this.displayName = displayName;
		this.type = type;
		this.mandatory = mandatory;
		this.possibleValues = possibleValues;
	}

	/**
	 * Nom de la donnée
	 */
	public String getName() {
		return name;
	}

	/**
	 * Nom d'affichage de la donnée (si null le nom de la donnée sera utilisé)
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Type de la donnée
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Indique si la donnée est obligatoire
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * Indique une liste de valeurs possibles, ou null si pas de liste
	 */
	public Collection<Object> getPossibleValues() {
		return possibleValues;
	}
}
