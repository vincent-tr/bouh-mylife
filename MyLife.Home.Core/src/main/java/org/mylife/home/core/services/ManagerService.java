package org.mylife.home.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.core.data.DataPluginPersistance;
import org.mylife.home.core.exchange.XmlCoreComponent;
import org.mylife.home.core.exchange.XmlCoreContainer;
import org.mylife.home.core.plugins.PluginRuntimeContext;
import org.mylife.home.net.NetContainer;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.NetRepository;
import org.mylife.home.net.exchange.XmlNetContainer;
import org.mylife.home.net.exchange.XmlNetObject;

/**
 * Service de gestion
 * 
 * @author pumbawoman
 * 
 */
public class ManagerService implements Service {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(ManagerService.class
			.getName());

	public static final int STATE_STOPPED = 0;
	public static final int STATE_ERROR = -1;
	public static final int STATE_RUNNING = 1;

	public static final int STATE_STARTING = 2;
	public static final int STATE_STOPPING = 3;

	/* internal */ManagerService() {

	}

	@Override
	public void terminate() {
		stop();
		commandExecution.shutdown();
		try {
			commandExecution.awaitTermination(Long.MAX_VALUE,
					TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			log.log(Level.SEVERE,
					"commandExecution.awaitTermination interrupted on terminate",
					e);
		}
	}

	private int state;
	private Exception error;

	/**
	 * Gestion de l'exécution de start/stop
	 */
	private final ExecutorService commandExecution = Executors
			.newSingleThreadExecutor();

	/**
	 * Obtention de l'état
	 * 
	 * @return
	 */
	public int getState() {
		return state;
	}

	private void setState(int state) {
		this.state = state;
		log.info("Setting level to : " + getStateString());
	}

	/**
	 * Obtention de l'état en chaine
	 * 
	 * @return
	 */
	public String getStateString() {
		switch (state) {
		case STATE_STOPPED:
			return "STOPPED";
		case STATE_ERROR:
			return "ERROR";
		case STATE_RUNNING:
			return "RUNNING";
		case STATE_STARTING:
			return "STARTING";
		case STATE_STOPPING:
			return "STOPPING";
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Si l'état est à error, obtient l'erreur qui s'est produite, sinon null
	 * 
	 * @return
	 */
	public Exception getError() {
		return error;
	}

	private void handleError(Exception e) {
		setState(STATE_ERROR);
		error = e;
		log.log(Level.SEVERE, "Severe error in manager service", e);
	}

	/**
	 * Démarrage
	 */
	public void start() {
		commandExecution.submit(new Start());
	}

	/**
	 * Arrêt
	 */
	public void stop() {
		commandExecution.submit(new Stop());
	}

	private class Start implements Runnable {
		@Override
		public void run() {
			startImpl();
		}
	}

	private class Stop implements Runnable {
		@Override
		public void run() {
			stopImpl();
		}
	}

	private synchronized void startImpl() {

		switch (state) {
		case STATE_STARTING:
		case STATE_RUNNING:
		case STATE_STOPPING:
			log.warning("Invalid state, ignored");
			return;
		}

		setState(STATE_STARTING);

		try {
			executeStart();
		} catch (Exception e) {
			handleError(e);
			return;
		}

		setState(STATE_RUNNING);
	}

	private synchronized void stopImpl() {

		switch (state) {
		case STATE_STARTING:
		case STATE_STOPPED:
		case STATE_STOPPING:
			log.warning("Invalid state, ignored");
			return;

		case STATE_ERROR:
			// seulement clear pour passer de error à stopped
			error = null;
			setState(STATE_STOPPED);
			return;
		}

		setState(STATE_STOPPING);

		try {
			executeStop();
		} catch (Exception e) {
			handleError(e);
			return;
		}

		setState(STATE_STOPPED);
	}

	private final List<NetContainer> remoteObjects = new ArrayList<NetContainer>();
	private final List<NetContainer> internalObjects = new ArrayList<NetContainer>();

	private final List<PluginRuntimeContext> plugins = new ArrayList<PluginRuntimeContext>();

	// TODO : links

	private void checkId(Set<String> ids, String id) {
		if (!ids.add(id)) {
			String message = "An object with id '" + id + "' already exists";
			throw new UnsupportedOperationException(message);
		}
	}

	private void executeStart() {
		// Lecture de la configuration
		List<XmlNetContainer> netList = new ArrayList<XmlNetContainer>();
		List<XmlCoreContainer> coreList = new ArrayList<XmlCoreContainer>();
		ServiceAccess.getConfigurationService().loadActives(netList, coreList);

		// Vérification que chaque id soit unique
		Set<String> ids = new HashSet<String>();
		for (XmlNetContainer net : netList) {
			for (XmlNetObject netComp : net.components) {
				checkId(ids, netComp.id);
			}
		}
		for (XmlCoreContainer core : coreList) {
			for (XmlCoreComponent coreComp : core.components) {
				checkId(ids, coreComp.id);
			}
		}

		// Création des objets distants
		for (XmlNetContainer net : netList) {
			for (XmlNetObject netComp : net.components) {
				NetObject obj = org.mylife.home.net.exchange.ExchangeManager
						.unmarshal(netComp);
				NetContainer container = NetRepository.register(obj,
						NetRepository.CHANNEL_HARDWARE, false);
				remoteObjects.add(container);
			}
		}

		// Création des plugins
		for (XmlCoreContainer core : coreList) {
			for (XmlCoreComponent coreComp : core.components) {
				PluginRuntimeContext context = new PluginRuntimeContext(
						coreComp);
				plugins.add(context);
			}
		}

		// TODO : create links
	}

	private void executeStop() {

		// TODO : unload links

		// Déchargement des plugins
		for (PluginRuntimeContext plugin : plugins) {
			plugin.terminate();
		}
		// normalement il ne doit plus rester d'objets après
		for (NetContainer container : internalObjects) {
			log.severe("Internal NetObject remaining : "
					+ container.getObject().getId());
			NetRepository.unregister(container);
		}

		// Déchargement des objets distants
		for (NetContainer container : remoteObjects) {
			NetRepository.unregister(container);
		}
	}

	/**
	 * Réservé à PluginRuntimeContext
	 * 
	 * @param context
	 * @param obj
	 */
	public NetContainer registerPluginObject(PluginRuntimeContext context,
			NetObject obj) {
		return NetRepository.register(obj, NetRepository.CHANNEL_DEBUG, true);
	}

	/**
	 * Réservé à PluginRuntimeContext
	 * 
	 * @param context
	 * @param obj
	 */
	public void unregisterPluginObject(PluginRuntimeContext context,
			NetContainer obj) {
		NetRepository.unregister(obj);
	}

	/**
	 * Réservé à PluginRuntimeContext
	 * 
	 * @param context
	 * @return
	 */
	public Map<String, String> getPluginPersistance(PluginRuntimeContext context) {
		List<DataPluginPersistance> list = ServiceAccess
				.getPluginPersistanceService().getPersistanceByComponentId(
						context.getId());
		Map<String, String> map = new HashMap<String, String>();
		for (DataPluginPersistance item : list) {
			map.put(item.getKey(), item.getValue());
		}
		return map;
	}

	/**
	 * Réservé à PluginRuntimeContext
	 * 
	 * @param context
	 * @param data
	 */
	public void savePluginPersistance(PluginRuntimeContext context,
			Map<String, String> data) {
		List<DataPluginPersistance> list = null;
		if (data != null) {
			list = new ArrayList<DataPluginPersistance>();
			for (Map.Entry<String, String> item : data.entrySet()) {
				DataPluginPersistance pp = new DataPluginPersistance();
				pp.setKey(item.getKey());
				pp.setValue(pp.getValue());
				list.add(pp);
			}
		}
		ServiceAccess.getPluginPersistanceService().updateByComponentId(
				context.getId(), list);
	}
}
