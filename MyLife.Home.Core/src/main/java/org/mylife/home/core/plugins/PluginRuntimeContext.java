package org.mylife.home.core.plugins;

import java.util.ArrayList;
import java.util.Collection;
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
 * 
 * @author pumbawoman
 * 
 */
public class PluginRuntimeContext implements PluginContext {

	private final String id;
	private final PluginFactory factory;
	private final Plugin plugin;
	private final Map<String, String> configuration;
	private final List<NetContainer> publishedObjects = new ArrayList<NetContainer>();

	private static Map<String, String> mapFromXml(
			XmlCoreComponentConfigurationItem[] data) {
		if (data == null)
			return null;
		Map<String, String> map = new HashMap<String, String>();
		for (XmlCoreComponentConfigurationItem item : data) {
			map.put(item.key, item.value);
		}
		return map;
	}

	/**
	 * Contexte à partir de configuration
	 * 
	 * @param data
	 */
	public PluginRuntimeContext(XmlCoreComponent data) throws Exception {
		this(data.id, data.pluginType, mapFromXml(data.configuration));
	}

	/**
	 * Contexte avec données
	 * 
	 * @param id
	 * @param pluginType
	 * @param configuration
	 */
	public PluginRuntimeContext(String id, String pluginType,
			Map<String, String> configuration) throws Exception {
		this.id = id;
		this.factory = ServiceAccess.getInstance().getPluginService()
				.getFactory(pluginType);
		this.plugin = factory.create();
		if (configuration == null)
			this.configuration = Collections.emptyMap();
		else
			this.configuration = Collections.unmodifiableMap(configuration);
		this.plugin.init(this);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void publishObject(NetObject obj) {
		NetContainer container = ServiceAccess.getInstance()
				.getManagerService().registerPluginObject(this, obj);
		publishedObjects.add(container);
	}

	@Override
	public Map<String, String> getConfiguration() {
		return configuration;
	}

	@Override
	public Map<String, String> getPersistance() {
		return ServiceAccess.getInstance().getManagerService()
				.getPluginPersistance(this);
	}

	@Override
	public void savePersistance(Map<String, String> data) {
		ServiceAccess.getInstance().getManagerService()
				.savePluginPersistance(this, data);
	}

	/**
	 * Fin d'utilisation
	 */
	public void terminate() {
		plugin.destroy();
		ManagerService service = ServiceAccess.getInstance()
				.getManagerService();
		for (NetContainer obj : publishedObjects) {
			service.unregisterPluginObject(this, obj);
		}
	}

	/**
	 * Obtention des objets publiés par le plugin
	 * 
	 * @return
	 */
	public Collection<NetContainer> getPublishedObjects() {
		return Collections.unmodifiableCollection(publishedObjects);
	}

	/**
	 * Obtention de la fabrique du plugin
	 * 
	 * @return
	 */
	public PluginFactory getFactory() {
		return factory;
	}
}
