package org.mylife.home.core.plugins.enhanced;

import org.mylife.home.core.plugins.Plugin;
import org.mylife.home.core.plugins.PluginFactory;
import org.mylife.home.core.plugins.enhanced.metadata.PluginClassMetadata;

/**
 * Gestion de la fabrique de plugins avancés
 * @author pumbawoman
 *
 */
public class EnhancedPluginFactory implements PluginFactory {
	
	/**
	 * Métadonnées
	 */
	private final PluginClassMetadata metadata;
	
	public EnhancedPluginFactory(Class<?> pluginClass) throws Exception {
		metadata = new PluginClassMetadata(pluginClass);
	}
	
	@Override
	public String getType() {
		return metadata.getType();
	}

	@Override
	public String getDisplayType() {
		return metadata.getDisplayType();
	}

	@Override
	public Plugin create() throws Exception {
		return new EnhancedPluginWrapper(metadata);
	}
}
