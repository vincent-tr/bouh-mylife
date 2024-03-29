package org.mylife.home.core.services;

import java.util.List;

import org.mylife.home.common.services.Service;
import org.mylife.home.core.data.DataPluginPersistanceAccess;
import org.mylife.home.core.data.DataPluginPersistance;

/**
 * Service de gestion de la persistance des plugins
 * 
 * @author pumbawoman
 * 
 */
public class PluginPersistanceService implements Service {

	/* internal */PluginPersistanceService() {

	}

	@Override
	public void terminate() {

	}

	/**
	 * Obtention des données par id de composant
	 * 
	 * @param id
	 * @return
	 */
	public List<DataPluginPersistance> getPersistanceByComponentId(String id) {
		DataPluginPersistanceAccess access = new DataPluginPersistanceAccess();
		try {
			return access.getPluginPersistanceByComponentId(id);
		} finally {
			access.close();
		}
	}

	/**
	 * Mist à jour des données par id de composant. Si persistance est null,
	 * uniquement une suppression est effectuée
	 * 
	 * @param id
	 * @param persistance
	 */
	public void updateByComponentId(String id,
			List<DataPluginPersistance> persistance) {
		DataPluginPersistanceAccess access = new DataPluginPersistanceAccess();
		try {

			access.deletePluginPersistanceByComponentId(id);

			if (persistance != null) {
				for (DataPluginPersistance item : persistance) {
					item.setComponentId(id);
					access.createPluginPersistance(item);
				}
			}

		} finally {
			access.close();
		}
	}
}
