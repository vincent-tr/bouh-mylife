package org.mylife.home.core.plugins.design;

import org.mylife.home.net.structure.NetType;

/**
 * Données de design d'un attribut
 * 
 * @author pumbawoman
 * 
 */
public class PluginDesignAttribute {

	private final String name;
	private final String displayName;
	private final NetType type;

	public PluginDesignAttribute(String name, String displayName, NetType type) {
		this.name = name;
		this.displayName = displayName;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public NetType getType() {
		return type;
	}
}
