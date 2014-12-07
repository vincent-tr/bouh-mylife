package org.mylife.home.components.services;

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
import org.mylife.home.components.providers.Component;
import org.mylife.home.components.providers.ComponentContext;
import org.mylife.home.components.providers.ComponentFactory;
import org.mylife.home.net.NetContainer;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.NetRepository;

public class ComponentService implements Service {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(ComponentService.class
			.getName());

	/* internal */ComponentService() {
		initFactories();
	}

	@Override
	public void terminate() {
		terminateComponents();
	}

	private void initFactories() {
		log.finest("Loadind factories");
		Map<String, ComponentFactory> map = new HashMap<String, ComponentFactory>();
		ServiceLoader<ComponentFactory> serviceLoader = ServiceLoader
				.load(ComponentFactory.class);
		for (ComponentFactory factory : serviceLoader) {
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
	private Map<String, ComponentFactory> factories;

	/**
	 * Obtention des fabriques de composants présentes dans le système
	 * 
	 * @return
	 */
	public Collection<ComponentFactory> getFactories() {
		return factories.values();
	}

	/**
	 * Obtention d'une fabrique par type de composant
	 * 
	 * @param type
	 * @return
	 */
	public ComponentFactory getFactory(String type) {
		Validate.notEmpty(type);
		return factories.get(type);
	}

	/**
	 * Gestion des composants
	 */
	private final Map<String, ComponentContext> components = Collections
			.synchronizedMap(new HashMap<String, ComponentContext>());

	/**
	 * Gestion de la synchro pour l'accès aux composants
	 */
	private final Object componentSync = new Object();

	public void addComponent(String componentId, String type,
			Map<String, String> parameters) throws Exception {

		Validate.notEmpty(type);

		ComponentFactory factory = getFactory(type);
		if (factory == null)
			throw new IllegalArgumentException("Factory does not exists : "
					+ type);

		addComponent(componentId, factory, parameters);
	}

	public void addComponent(String componentId, ComponentFactory factory,
			Map<String, String> parameters) throws Exception {

		Validate.notNull(factory);

		Component component = factory.create();

		addComponent(componentId, component, parameters);
	}

	public void addComponent(String componentId, Component component,
			Map<String, String> parameters) throws Exception {

		Validate.notEmpty(componentId);
		Validate.notNull(component);
		Validate.notNull(parameters);

		synchronized (componentSync) {
			if (components.containsKey(componentId))
				throw new IllegalArgumentException(
						"Component already exists : " + componentId);

			ComponentContainer container = new ComponentContainer(componentId,
					component, parameters);
			components.put(componentId, container);
		}
	}

	public boolean removeComponent(String componentId) {

		Validate.notEmpty(componentId);

		synchronized (componentSync) {
			ComponentContainer container = (ComponentContainer) components
					.remove(componentId);
			if (container == null)
				return false;
			container.destroy();
			return true;
		}
	}

	private void terminateComponents() {
		synchronized (componentSync) {
			List<String> ids = new ArrayList<String>(components.keySet());
			for (String id : ids) {
				removeComponent(id);
			}
		}
	}

	public Collection<ComponentContext> getComponents() {
		return Collections.unmodifiableCollection(components.values());
	}

	public ComponentContext getComponent(String componentId) {
		Validate.notEmpty(componentId);
		return components.get(componentId);
	}

	/**
	 * Conteneur de composant
	 * 
	 * @author pumbawoman
	 * 
	 */
	private static class ComponentContainer implements ComponentContext {

		private final String componentId;
		private final Component component;
		private final Map<String, String> parameters;
		private NetContainer container;

		/**
		 * Initialisation
		 * 
		 * @param componentId
		 * @param component
		 * @param parameters
		 * @throws Exception
		 */
		public ComponentContainer(String componentId, Component component,
				Map<String, String> parameters) throws Exception {
			this.componentId = componentId;
			this.component = component;
			this.parameters = parameters;

			component.init(this);

			log.info(String.format("Component %s created", componentId));
		}

		/**
		 * Fin de vie de conteneur
		 */
		public void destroy() {
			if (container != null) {
				log.info(String.format(
						"Unregistering netobject with id '%s' on channel '%s'",
						container.getObject().getId(), container.getChannel()));
				NetRepository.unregister(container);
			}

			log.info(String.format("Component %s destroyed", componentId));

			component.destroy();
		}

		@Override
		public String componentId() {
			return componentId;
		}

		@Override
		public Component component() {
			return component;
		}

		@Override
		public Map<String, String> parameters() {
			return parameters;
		}

		@Override
		public void registerObject(NetObject object, String channel) {
			if (this.container != null)
				throw new IllegalStateException();
			this.container = NetRepository.register(object, channel, true);
			log.info(String.format(
					"Registering netobject with id '%s' on channel '%s'",
					container.getObject().getId(), container.getChannel()));
		}

		@Override
		public void registerObject(NetObject object) {
			registerObject(object, NetRepository.CHANNEL_HARDWARE);
		}

		@Override
		public NetObject getObject() {
			if (container == null)
				return null;
			return container.getObject();
		}

	}
}
