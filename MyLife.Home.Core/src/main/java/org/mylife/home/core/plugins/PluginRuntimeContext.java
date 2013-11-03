package org.mylife.home.core.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mylife.home.core.exchange.XmlCoreComponent;
import org.mylife.home.core.exchange.XmlCoreComponentConfigurationItem;
import org.mylife.home.core.services.ManagerService;
import org.mylife.home.core.services.ServiceAccess;
import org.mylife.home.net.NetContainer;
import org.mylife.home.net.NetObject;

/**
 * Gestion d'un contexte d'exécution de plugin
 * @author pumbawoman
 *
 */
public class PluginRuntimeContext implements PluginContext {

	private final String id;
	private final Plugin plugin;
	private final Map<String, String> configuration;
	private final List<NetContainer> publishedObjects = new ArrayList<NetContainer>();
	
	private static Map<String, String> mapFromXml(XmlCoreComponentConfigurationItem[] data) {
		if(data == null)
			return null;
		Map<String, String> map = new HashMap<String, String>();
		for(XmlCoreComponentConfigurationItem item : data) {
			map.put(item.key, item.value);
		}
		return map;
	}
	
	private static Plugin instanciatePlugin(String pluginClass) {
		try {
			Class<?> clazz = Class.forName(pluginClass);
			return (Plugin)clazz.newInstance();
		} catch(Exception e) {
			throw new PluginInstanciateException(e);
		}
	}
	
	/**
	 * Contexte à partir de configuration
	 * @param data
	 */
	public PluginRuntimeContext(XmlCoreComponent data) {
		this(data.id, data.pluginClass, mapFromXml(data.configuration));
	}
	
	/**
	 * Contexte avec données
	 * @param id
	 * @param pluginClass
	 * @param configuration
	 */
	public PluginRuntimeContext(String id, String pluginClass, Map<String, String> configuration) {
		this.id = id;
		this.plugin = instanciatePlugin(pluginClass);
		this.configuration = configuration != null ? Collections.unmodifiableMap(configuration) : null;
		this.plugin.initialize(this);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getPurpose() {
		return PluginContext.PURPOSE_RUNTIME;
	}

	@Override
	public void publishObject(NetObject obj) {
		NetContainer container = ServiceAccess.getManagerService().registerPluginObject(this, obj);
		publishedObjects.add(container);
	}

	@Override
	public Map<String, String> getConfiguration() {
		return configuration;
	}

	@Override
	public Map<String, String> getPersistance() {
		return ServiceAccess.getManagerService().getPluginPersistance(this);
	}

	@Override
	public void savePersistance(Map<String, String> data) {
		ServiceAccess.getManagerService().savePluginPersistance(this, data);
	}
	
	/**
	 * Fin d'utilisation
	 */
	public void terminate() {
		plugin.terminate();
		ManagerService service = ServiceAccess.getManagerService();
		for(NetContainer obj : publishedObjects) {
			service.unregisterPluginObject(this, obj);
		}
	}
}
