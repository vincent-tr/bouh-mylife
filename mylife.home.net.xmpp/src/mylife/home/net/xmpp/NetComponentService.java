package mylife.home.net.xmpp;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import mylife.home.net.api.NetComponent;
import mylife.home.net.api.NetComponentFactory;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Modified;

/**
 * Implémentation du service
 * @author pumbawoman
 */
@Component
public class NetComponentService implements NetComponentFactory {

	private final String KEY_XMPP_SERVER = "xmppServer";
	private final String KEY_MUC_ROOM = "mucRoom";
	
	private final String DEFAULT_XMPP_SERVER = "files.mti-team2.dyndns.org"; 
	private final String DEFAULT_MUC_ROOM = "home@conference.mti-team2.dyndns.org";
	
	/**
	 * Liste des composants enregistrés
	 */
	private final Set<NetComponentImpl> components = new HashSet<NetComponentImpl>();
	
	/**
	 * Configuration
	 */
	private Configuration configuration;
	
	/**
	 * Création d'un composant
	 */
	@Override
	public NetComponent createComponent(String componentId, String componentDisplay, String componentType) {
		synchronized(components) {
			return new NetComponentImpl(this, configuration, componentId, componentDisplay, componentType);
		}
	}
	
	/**
	 * Fermeture du composant
	 * @param component
	 */
	public void closeComponent(NetComponentImpl component) {
		synchronized(components) {
			components.remove(component);
		}
	}
	
	/**
	 * Changement de la configuration
	 * @param properties
	 */
	@SuppressWarnings("rawtypes")
	private void changeConfiguration(Dictionary properties) {
		
		Configuration configuration = new Configuration(
				readConfigurationProperty(properties, KEY_XMPP_SERVER, DEFAULT_XMPP_SERVER),
				readConfigurationProperty(properties, KEY_MUC_ROOM, DEFAULT_MUC_ROOM));
		
		synchronized(components) {
			this.configuration = configuration;
			for(NetComponentImpl component : components)
				component.changeConfiguration(configuration);
		}
	}
	
	/**
	 * Lecture de configuration
	 * @param properties
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private String readConfigurationProperty(Dictionary properties, String key, String defaultValue) {
		Object value = properties.get(key);
		if(value == null)
			return defaultValue;
		return (String)value;
	}

	/**
	 * Activation
	 * @param ctx
	 */
	@Activate
	public void activate(ComponentContext ctx) {
		changeConfiguration(ctx.getProperties());
	}

	/**
	 * Modification de configuration
	 * @param ctx
	 */
	@Modified
	public void modified(ComponentContext ctx) {
		changeConfiguration(ctx.getProperties());
	}
}
