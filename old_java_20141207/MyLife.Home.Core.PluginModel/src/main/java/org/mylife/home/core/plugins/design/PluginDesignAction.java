package org.mylife.home.core.plugins.design;

import java.util.Collection;

import org.mylife.home.net.structure.NetType;

/**
 * Données de design d'une action
 * 
 * @author pumbawoman
 * 
 */
public class PluginDesignAction {

	private final String name;
	private final String displayName;
	private final Collection<NetType> arguments;
	
	public PluginDesignAction(String name, String displayName,
			Collection<NetType> arguments) {
		this.name = name;
		this.displayName = displayName;
		this.arguments = arguments;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Collection<NetType> getArguments() {
		return arguments;
	}
}
