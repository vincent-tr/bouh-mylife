package org.mylife.home.ui.services;

import java.util.ArrayList;
import java.util.List;

import org.mylife.home.common.services.BaseManagerService;
import org.mylife.home.net.ConnectedChangeListener;
import org.mylife.home.net.NetContainer;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.NetRepository;
import org.mylife.home.net.exchange.ExchangeManager;
import org.mylife.home.net.exchange.net.XmlNetContainer;
import org.mylife.home.net.exchange.net.XmlNetObject;
import org.mylife.home.net.exchange.ui.XmlUiContainer;
import org.mylife.home.ui.net.NetUiReceiver;

/**
 * Service de gestion du réseau
 * 
 * @author pumbawoman
 * 
 */
public class NetService extends BaseManagerService implements
		NetUiReceiver.DataUpdatedListener, ConnectedChangeListener {

	/* internal */NetService() {
	}

	private final List<NetContainer> remoteObjects = new ArrayList<NetContainer>();
	private NetUiReceiver uiReceiver;
	private final Object syncLock = new Object();

	/**
	 * Démarrage du service
	 */
	@Override
	protected void executeStart() throws Exception {

		uiReceiver = new NetUiReceiver();
		uiReceiver.addListener(this);
	}

	/**
	 * Arrêt du service
	 */
	@Override
	protected void executeStop() throws Exception {

		uiReceiver.close();
		uiReceiver = null;
		dataDesynchronized();
	}

	/**
	 * Obtention des données d'ui
	 * 
	 * @return
	 */
	public XmlUiContainer getUiContainer() {
		return uiReceiver.getUiContainer();
	}

	/**
	 * Réservé
	 */
	@Override
	public void dataSynchronized(XmlUiContainer uiContainer,
			XmlNetContainer netContainer) {

		DispatcherService dispatcher = ServiceAccess.getInstance()
				.getDispatcherService();

		synchronized (syncLock) {

			dispatcher.structureChanged(uiContainer);

			for (XmlNetObject xmlObject : netContainer.components) {
				NetObject obj = ExchangeManager.unmarshal(xmlObject);
				NetContainer container = NetRepository.register(obj,
						NetRepository.CHANNL_UI, false);
				container.registerConnectedChange(this);
				remoteObjects.add(container);

				dispatcher.objectNew(obj);
				if (container.isConnected())
					dispatcher.objectOnline(obj);
			}
		}
	}

	@Override
	public void dataDesynchronized() {

		DispatcherService dispatcher = ServiceAccess.getInstance()
				.getDispatcherService();

		synchronized (syncLock) {

			dispatcher.structureChanged(null);

			for (NetContainer container : remoteObjects) {
				NetObject obj = container.getObject();
				if (container.isConnected())
					dispatcher.objectOffline(obj);
				dispatcher.objectDeleted(obj);

				NetRepository.unregister(container);
			}
			remoteObjects.clear();
		}
	}

	@Override
	public void connectedChanged(NetContainer container, boolean isConnected) {
		DispatcherService dispatcher = ServiceAccess.getInstance()
				.getDispatcherService();

		if (isConnected)
			dispatcher.objectOnline(container.getObject());
		else
			dispatcher.objectOffline(container.getObject());
	}
}
