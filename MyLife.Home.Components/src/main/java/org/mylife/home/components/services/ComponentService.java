package org.mylife.home.components.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.mylife.home.common.services.Service;
import org.mylife.home.components.providers.ComponentFactory;

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
	}
	
	private void initFactories() {
		log.finest("Loadind factories");
		List<ComponentFactory> list = new ArrayList<ComponentFactory>();
		ServiceLoader<ComponentFactory> serviceLoader = ServiceLoader
				.load(ComponentFactory.class);
		for (ComponentFactory factory : serviceLoader) {
			log.finest("Factory loaded : " + factory.getClass().toString());
			list.add(factory);
		}
		factories = Collections.unmodifiableList(list);
		log.finest("Factories loading terminated");
	}
	
	/**
	 * Fabriques de composants
	 */
	private List<ComponentFactory> factories;

	/**
	 * Obtention des fabriques de composants présentes dans le système
	 * @return
	 */
	public List<ComponentFactory> getFactories() {
		return factories;
	}

}
