package org.mylife.home.core.plugins.simple;

import org.mylife.home.core.plugins.Plugin;
import org.mylife.home.core.plugins.PluginFactory;
import org.mylife.home.core.plugins.design.PluginDesignMetadata;

/**
 * Fabrique de base d'un plugin
 * 
 * @author pumbawoman
 * 
 */
public class SimplePluginFactory implements PluginFactory {

	private final Class<? extends Plugin> pluginClass;
	private final String displayType;

	public SimplePluginFactory(Class<? extends Plugin> pluginClass,
			String displayType) {
		this.pluginClass = pluginClass;
		this.displayType = displayType;
	}

	public SimplePluginFactory(Class<? extends Plugin> pluginClass) {
		this(pluginClass, pluginClass.getSimpleName());
	}

	@Override
	public String getType() {
		return pluginClass.getSimpleName();
	}

	@Override
	public String getDisplayType() {
		return displayType;
	}

	@Override
	public Plugin create() throws Exception {
		return pluginClass.newInstance();
	}

	@Override
	public PluginDesignMetadata getDesignMetadata() {
		throw new UnsupportedOperationException();
	}
}
