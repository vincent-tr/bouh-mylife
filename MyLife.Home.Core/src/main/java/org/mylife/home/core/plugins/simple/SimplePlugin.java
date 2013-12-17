package org.mylife.home.core.plugins.simple;

import org.mylife.home.core.plugins.Plugin;
import org.mylife.home.core.plugins.PluginContext;
import org.mylife.home.core.plugins.PluginDesignMetadata;

/**
 * Classe de base d'un plugin
 * 
 * @author pumbawoman
 * 
 */
public class SimplePlugin implements Plugin {

	@Override
	public void init(PluginContext context) throws Exception {
	}

	@Override
	public void destroy() {
	}

	@Override
	public PluginDesignMetadata getDesignMetadata() {
		throw new UnsupportedOperationException();
	}

}
