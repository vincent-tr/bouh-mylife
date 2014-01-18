package mylife.home.components;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;

@Component(configurationPolicy=ConfigurationPolicy.optional, provide={StateBackupService.class})
public class StateBackupServiceImpl implements StateBackupService {
	
	public static final String localPid = StateBackupServiceImpl.class.getName();

	/**
	 * Service de configuration
	 */
	private ConfigurationAdmin configService;
	
	/**
	 * Configuration
	 */
	private Configuration config;
	
	/**
	 * Données de configuration
	 */
	private Dictionary data;
	
	/**
	 * Verrou
	 */
	private final Object dataLock = new Object();
	
	/**
	 * Service de configuration
	 * @param configService
	 */
	@Reference
	public void setConfigService(ConfigurationAdmin configService) {
		this.configService = configService;
	}
	
	/**
	 * Activation
	 * @param ctx
	 * @throws IOException 
	 */
	@Activate
	public void activate(ComponentContext ctx) throws IOException {
		refresh();
	}

	/**
	 * Modification de configuration
	 * @param ctx
	 * @throws IOException 
	 */
	@Modified
	public void modified(ComponentContext ctx) throws IOException {
		refresh();
	}
	
	private void refresh() throws IOException {
		synchronized(dataLock) {
			config = configService.getConfiguration(localPid);
			data = config.getProperties();
			if(data == null)
				data = new Hashtable();
		}
	}
	
	/**
	 * Enregistrement
	 */
	private void store() {
		synchronized(dataLock) {
			try {
				config.update(data);
			} catch(IOException ex) {
				throw new RuntimeException("Error saving state", ex);
			}
		}
	}

	/**
	 * Désactivation
	 * @param ctx
	 */
	/*
	@Deactivate
	public void deactivate(ComponentContext ctx) {
		store();
	}
	*/
	
	/**
	 * Obtention de l'état d'un composant
	 * @param pid
	 * @return
	 */
	@Override
	public String getState(String pid) {
		return (String)data.get(pid);
	}
	
	/**
	 * Définition de l'état d'un composant
	 * @param pid
	 * @param state
	 */
	@Override
	public void setState(String pid, String state) {
		data.put(pid, state);
		store();
	}
}
