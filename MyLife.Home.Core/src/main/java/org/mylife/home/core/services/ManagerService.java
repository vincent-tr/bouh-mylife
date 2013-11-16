package org.mylife.home.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mylife.home.common.services.BaseManagerService;
import org.mylife.home.core.data.DataPluginPersistance;
import org.mylife.home.core.exchange.XmlCoreComponent;
import org.mylife.home.core.exchange.XmlCoreContainer;
import org.mylife.home.core.exchange.XmlCoreLink;
import org.mylife.home.core.links.Link;
import org.mylife.home.core.links.LinkFactory;
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
public class ManagerService extends BaseManagerService {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(ManagerService.class
			.getName());

	/* internal */ManagerService() {
	}

	private final List<NetContainer> remoteObjects = new ArrayList<NetContainer>();
	private final List<NetContainer> internalObjects = new ArrayList<NetContainer>();
	private final List<PluginRuntimeContext> plugins = new ArrayList<PluginRuntimeContext>();
	private final List<Link> links = new ArrayList<Link>();

	private void checkId(Set<String> ids, String id) {
		if (!ids.add(id)) {
			String message = "An object with id '" + id + "' already exists";
			throw new UnsupportedOperationException(message);
		}
	}

	/**
	 * Démarrage du service
	 */
	@Override
	protected void executeStart() throws Exception {
		// Lecture de la configuration
		List<XmlNetContainer> netList = new ArrayList<XmlNetContainer>();
		List<XmlCoreContainer> coreList = new ArrayList<XmlCoreContainer>();
		ServiceAccess.getInstance().getConfigurationService()
				.loadActives(netList, coreList);

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

		// Création des liens
		for (XmlCoreContainer core : coreList) {
			for (XmlCoreLink coreLink : core.links) {
				Link link = LinkFactory.getInstance().createFromXml(coreLink);
				links.add(link);
			}
		}
	}

	/**
	 * Arrêt du service
	 */
	@Override
	protected void executeStop() throws Exception {

		for (Link link : links) {
			link.close();
		}
		links.clear();

		// Déchargement des plugins
		for (PluginRuntimeContext plugin : plugins) {
			plugin.terminate();
		}
		plugins.clear();

		// normalement il ne doit plus rester d'objets après
		for (NetContainer container : internalObjects) {
			log.severe("Internal NetObject remaining : "
					+ container.getObject().getId());
			NetRepository.unregister(container);
		}
		internalObjects.clear();

		// Déchargement des objets distants
		for (NetContainer container : remoteObjects) {
			NetRepository.unregister(container);
		}
		remoteObjects.clear();
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
		List<DataPluginPersistance> list = ServiceAccess.getInstance()
				.getPluginPersistanceService()
				.getPersistanceByComponentId(context.getId());
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
		ServiceAccess.getInstance().getPluginPersistanceService()
				.updateByComponentId(context.getId(), list);
	}
}
