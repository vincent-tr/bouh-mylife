package org.mylife.home.components.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.common.services.BaseManagerService;
import org.mylife.home.components.providers.ComponentConfiguration;
import org.mylife.home.components.providers.ComponentContext;

public class ManagerService extends BaseManagerService {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(ManagerService.class
			.getName());

	/* internal */ManagerService() {
	}

	/**
	 * Démarrage du service
	 */
	@Override
	protected void executeStart() throws Exception {
		List<ComponentConfiguration> config = ServiceAccess.getInstance()
				.getConfigurationService().listActives();
		for (ComponentConfiguration item : config) {
			createComponentFromConfig(item);
		}
	}

	/**
	 * Arrêt du service
	 */
	@Override
	protected void executeStop() throws Exception {
		ComponentService service = ServiceAccess.getInstance()
				.getComponentService();
		List<String> ids = new ArrayList<String>();
		for (ComponentContext comp : service.getComponents()) {
			ids.add(comp.componentId());
		}

		for (String id : ids)
			service.removeComponent(id);
	}

	private void createComponentFromConfig(ComponentConfiguration config)
			throws Exception {
		ComponentService componentService = ServiceAccess.getInstance()
				.getComponentService();

		componentService.addComponent(config.getComponentId(),
				config.getType(), config.getParameters());
	}

	public synchronized void configurationUpdated(ComponentConfiguration config) {
		if (getState() != STATE_RUNNING)
			return;

		// On essaye d'enlever le composant, pas grave s'il n'existait pas
		ServiceAccess.getInstance().getComponentService()
				.removeComponent(config.getComponentId());

		if (!config.isActive())
			return;

		try {
			createComponentFromConfig(config);
		} catch (Exception e) {
			log.log(Level.SEVERE,
					"Error starting component : " + config.getComponentId(), e);
		}
	}
}
