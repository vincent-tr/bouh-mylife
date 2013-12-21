package org.mylife.home.core.services;

import java.util.ArrayList;
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
import org.mylife.home.core.exchange.XmlDesignAction;
import org.mylife.home.core.exchange.XmlDesignAttribute;
import org.mylife.home.core.exchange.XmlDesignConfiguration;
import org.mylife.home.core.exchange.XmlDesignContainer;
import org.mylife.home.core.exchange.XmlDesignPlugin;
import org.mylife.home.core.exchange.XmlDesignValueType;
import org.mylife.home.core.plugins.PluginFactory;
import org.mylife.home.core.plugins.design.PluginDesignAction;
import org.mylife.home.core.plugins.design.PluginDesignAttribute;
import org.mylife.home.core.plugins.design.PluginDesignConfiguration;
import org.mylife.home.core.plugins.design.PluginDesignMetadata;
import org.mylife.home.net.exchange.ExchangeManager;
import org.mylife.home.net.exchange.XmlNetType;
import org.mylife.home.net.structure.NetType;

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

	/**
	 * Obtention des métadonnées de design sous format xml Le nom et la version
	 * ne sont pas remplis
	 * 
	 * @return
	 */
	public XmlDesignContainer getDesignMetadata() {

		XmlDesignContainer xmlContainer = new XmlDesignContainer();
		Collection<XmlDesignPlugin> plugins = new ArrayList<XmlDesignPlugin>();
		for (PluginFactory factory : getFactories()) {
			plugins.add(mapDesignPlugin(factory));
		}
		xmlContainer.plugins = plugins.toArray(new XmlDesignPlugin[plugins
				.size()]);

		return xmlContainer;
	}

	private XmlDesignPlugin mapDesignPlugin(PluginFactory factory) {

		XmlDesignPlugin xmlPlugin = new XmlDesignPlugin();
		xmlPlugin.type = factory.getType();
		xmlPlugin.displayType = factory.getDisplayType();
		PluginDesignMetadata metadata = factory.getDesignMetadata();
		xmlPlugin.image = metadata.getImage();
		xmlPlugin.ui = metadata.isUi();

		Collection<XmlDesignConfiguration> config = new ArrayList<XmlDesignConfiguration>();
		for (PluginDesignConfiguration configItem : metadata
				.getConfigurationData()) {
			config.add(mapDesignConfiguration(configItem));
		}
		xmlPlugin.configuration = config
				.toArray(new XmlDesignConfiguration[config.size()]);

		Collection<XmlDesignAttribute> attributes = new ArrayList<XmlDesignAttribute>();
		for (PluginDesignAttribute attribute : metadata.getAttributes()) {
			attributes.add(mapDesignAttribute(attribute));
		}
		xmlPlugin.attributes = attributes
				.toArray(new XmlDesignAttribute[attributes.size()]);

		Collection<XmlDesignAction> actions = new ArrayList<XmlDesignAction>();
		for (PluginDesignAction action : metadata.getActions()) {
			actions.add(mapDesignAction(action));
		}
		xmlPlugin.actions = actions
				.toArray(new XmlDesignAction[actions.size()]);

		return xmlPlugin;
	}

	private XmlDesignConfiguration mapDesignConfiguration(
			PluginDesignConfiguration config) {
		XmlDesignConfiguration xmlConfig = new XmlDesignConfiguration();
		xmlConfig.name = config.getName();
		xmlConfig.displayName = config.getDisplayName();
		xmlConfig.mandatory = config.isMandatory();

		Class<?> clazz = config.getType();
		if (clazz == String.class) {
			xmlConfig.type = XmlDesignValueType.STRING;
		} else if (clazz == boolean.class) {
			xmlConfig.type = XmlDesignValueType.BOOLEAN;
		} else if (clazz == byte.class) {
			xmlConfig.type = XmlDesignValueType.BYTE;
		} else if (clazz == short.class) {
			xmlConfig.type = XmlDesignValueType.SHORT;
		} else if (clazz == int.class) {
			xmlConfig.type = XmlDesignValueType.INT;
		} else if (clazz == long.class) {
			xmlConfig.type = XmlDesignValueType.LONG;
		} else if (clazz == float.class) {
			xmlConfig.type = XmlDesignValueType.FLOAT;
		} else if (clazz == double.class) {
			xmlConfig.type = XmlDesignValueType.DOUBLE;
		} else {
			throw new UnsupportedOperationException("Unsupported class : "
					+ clazz.toString());
		}

		if (config.getPossibleValues() != null) {
			Collection<String> possibleValues = new ArrayList<String>();
			for (Object value : config.getPossibleValues()) {
				possibleValues.add(value == null ? null : value.toString());
			}
			xmlConfig.possibleValues = possibleValues
					.toArray(new String[possibleValues.size()]);
		}

		return xmlConfig;
	}

	private XmlDesignAttribute mapDesignAttribute(
			PluginDesignAttribute attribute) {

		XmlDesignAttribute xmlAttribute = new XmlDesignAttribute();
		xmlAttribute.name = attribute.getName();
		xmlAttribute.displayName = attribute.getDisplayName();
		xmlAttribute.type = ExchangeManager.marshal(attribute.getType());
		return xmlAttribute;
	}

	private XmlDesignAction mapDesignAction(PluginDesignAction action) {
		XmlDesignAction xmlAction = new XmlDesignAction();
		xmlAction.name = action.getName();
		xmlAction.displayName = action.getDisplayName();

		Collection<XmlNetType> arguments = new ArrayList<XmlNetType>();
		for (NetType argument : action.getArguments()) {
			arguments.add(ExchangeManager.marshal(argument));
		}
		xmlAction.arguments = arguments
				.toArray(new XmlNetType[arguments.size()]);

		return xmlAction;
	}
}
