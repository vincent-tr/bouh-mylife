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
import aQute.bnd.annotation.metatype.Configurable;

/**
 * Implémentation du service
 * @author pumbawoman
 */
@Component(designate=Configuration.class)
public class NetComponentService implements NetComponentFactory {
	
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
		Configuration configuration = Configurable.createConfigurable(Configuration.class, properties);
		
		synchronized(components) {
			this.configuration = configuration;
			for(NetComponentImpl component : components)
				component.changeConfiguration(configuration);
		}
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
