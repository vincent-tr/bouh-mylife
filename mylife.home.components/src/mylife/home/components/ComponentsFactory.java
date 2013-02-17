package mylife.home.components;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import mylife.home.hw.api.DeviceManager;
import mylife.home.net.api.NetComponentFactory;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;

/**
 * Gestion des composants
 * @author pumbawoman
 */
@aQute.bnd.annotation.component.Component(provide=Object.class)
public class ComponentsFactory {

	private final String CONFIG_ID = "id";
	private final String CONFIG_CLASS = "class";
	private final String CONFIG_DISPLAY = "display";
	
	private DeviceManager deviceManager;
	private NetComponentFactory netManager;
	private Dictionary<?,?> configuration;
	
	/**
	 * Activation
	 * @param ctx
	 */
	@Activate
	public void activate(ComponentContext ctx) {
		synchronized(sync) {
			clear(); // ??
			configuration = ctx.getProperties();
			init();
		}
	}

	/**
	 * Désactivation
	 * @param ctx
	 */
	@Deactivate
	public void deactivate(ComponentContext ctx) {
		synchronized(sync) {
			clear();
		}
	}
	
	/**
	 * Modification de configuration
	 * @param ctx
	 */
	@Modified
	public void modified(ComponentContext ctx) {
		synchronized(sync) {
			clear();
			configuration = ctx.getProperties();
			init();
		}
	}
	
	@Reference
	public void setDeviceManager(DeviceManager deviceManager) {
		synchronized(sync) {
			clear();
			this.deviceManager = deviceManager;
			init();
		}
	}
	
	@Reference
	public void setNetManager(NetComponentFactory netManager) {
		synchronized(sync) {
			clear();
			this.netManager = netManager;
			init();
		}
	}
	
	/**
	 * Liste des composants
	 */
	private final Set<Component> components = new HashSet<Component>();
	
	/**
	 * Synchro
	 */
	private final Object sync = new Object();
	
	/**
	 * Libération des composants
	 */
	private void clear() {
		for(Component component : components) {
			try {
				component.close();				
			} catch(Exception ex) {
				// Logs
			}
		}
		components.clear();
	}
	
	/**
	 * Initialisation des composants
	 */
	private void init() {
		if(deviceManager == null)
			return;
		if(netManager == null)
			return;
		if(configuration == null)
			return;
		
		// lecture de la configuration et initialisation des composants
		int index = 0;
		while(readConfigurationItem(index++));
	}

	/**
	 * Lecture de la configuration
	 * @param index
	 * @return
	 */
	private boolean readConfigurationItem(int index) {
		
		// Obtention des valeurs de base
		String id = (String)configuration.get("" + index + "." + CONFIG_ID);
		if(id == null)
			return false;
		String className = (String)configuration.get("" + index + "." + CONFIG_CLASS);
		if(className == null)
			return false;
		String display = (String)configuration.get("" + index + "." + CONFIG_DISPLAY);
		if(display == null)
			return false;
		
		try {
			
			// Instanciation du composant
			Component component = (Component)Class.forName(className).newInstance();
			
			// On obtient toute la configuration qui correspond au composant
			Map<String, String> componentConfiguration = new Hashtable<String, String>();
			String prefix = ""+ index + ".";
			for (Enumeration<?> e = configuration.keys(); e.hasMoreElements();) {
				
				String key = (String)e.nextElement();
				if(!key.startsWith(prefix))
					continue;
				
				String value = (String)configuration.get(key);
				key = key.substring(prefix.length());
				
				componentConfiguration.put(key, value);
			}
			
			// Initialisation du composant
			component.configure(deviceManager, netManager, id, display, componentConfiguration);
			component.init();
			
			// Le composant est opérationnel, on l'ajoute à la liste
			components.add(component);
			
		} catch (Exception e) {
			// Logs
		}
		
		return true;
	}
}
