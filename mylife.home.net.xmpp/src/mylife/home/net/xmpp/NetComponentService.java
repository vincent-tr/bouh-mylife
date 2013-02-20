package mylife.home.net.xmpp;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import mylife.home.net.api.NetComponent;
import mylife.home.net.api.NetComponentFactory;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.metatype.Configurable;

/**
 * Implémentation du service
 * @author pumbawoman
 */
@Component(designate=Configuration.class,configurationPolicy=ConfigurationPolicy.optional)
public class NetComponentService implements NetComponentFactory {
	
	// http://code.google.com/p/asmack/issues/detail?id=13
	// http://issues.igniterealtime.org/browse/SMACK-315?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel
	// https://github.com/Flowdalic/asmack/blob/master/static-src/custom/org/jivesoftware/smackx/InitStaticCode.java
	static {
	    ClassLoader appClassLoader = NetComponentService.class.getClassLoader();

	    try {
		    Class.forName(org.jivesoftware.smackx.ServiceDiscoveryManager.class.getName(), true, appClassLoader);
		    Class.forName(org.jivesoftware.smack.PrivacyListManager.class.getName(), true, appClassLoader);
		    Class.forName(org.jivesoftware.smackx.XHTMLManager.class.getName(), true, appClassLoader);
		    Class.forName(org.jivesoftware.smackx.muc.MultiUserChat.class.getName(), true, appClassLoader);
		    Class.forName(org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager.class.getName(), true, appClassLoader);
		    Class.forName(org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager.class.getName(), true, appClassLoader);
		    Class.forName(org.jivesoftware.smackx.filetransfer.FileTransferManager.class.getName(), true, appClassLoader);
		    Class.forName(org.jivesoftware.smackx.LastActivityManager.class.getName(), true, appClassLoader);
		    Class.forName(org.jivesoftware.smack.ReconnectionManager.class.getName(), true, appClassLoader);
		    Class.forName(org.jivesoftware.smackx.commands.AdHocCommandManager.class.getName(), true, appClassLoader);
	    } catch (ClassNotFoundException e) {
		    throw new IllegalStateException("Could not init static class blocks", e);
	    }
	}
	
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
