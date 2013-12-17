package org.mylife.home.core.plugins.enhanced;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.core.plugins.Plugin;
import org.mylife.home.core.plugins.PluginContext;
import org.mylife.home.core.plugins.PluginDesignMetadata;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.structure.NetClass;
import org.mylife.home.net.structure.NetMember;

/**
 * Gestion d'un plugin avancé
 * 
 * @author pumbawoman
 * 
 */
class EnhancedPluginWrapper implements Plugin {

	/**
	 * Logger
	 */
	private final static Logger log = Logger
			.getLogger(EnhancedPluginWrapper.class.getName());

	private final PluginClassMetadata metadata;
	private final Object instance;
	private PluginContext context;
	private NetObject netObject;

	public EnhancedPluginWrapper(PluginClassMetadata metadata) throws Exception {
		this.metadata = metadata;
		this.instance = metadata.getPluginClass().newInstance();
	}

	@Override
	public void init(PluginContext context) throws Exception {

		this.context = context;

		// Exécution des méthodes d'initialisation
		PluginConfigurationWrapper configurationInstance = null;
		Class<?> configurationInterface = metadata.getConfigurationInterface();
		if (configurationInterface != null)
			configurationInstance = new PluginConfigurationWrapper(context,
					configurationInterface);

		for (Method method : metadata.getInitMethods()) {

			Collection<Object> args = new ArrayList<Object>();
			for (Class<?> argClass : method.getParameterTypes()) {
				if (argClass.equals(PluginContext.class))
					args.add(context);
				if (argClass.equals(configurationInterface))
					args.add(configurationInstance.getConfiguration());
			}
			method.invoke(instance, args.toArray());
		}

		// Construction de l'objet publié
		Collection<NetMember> members = new ArrayList<NetMember>();
		for (PluginClassMetadata.MemberMetadata member : metadata.getMembers()) {
			// TODO : members
		}
		netObject = new NetObject(context.getId(), new NetClass(
				members));
		// TODO : bindings
		context.publishObject(netObject);
	}

	@Override
	public void destroy() {

		// Exécution des méthodes de destruction
		for (Method method : metadata.getDestroyMethods()) {
			try {
				method.invoke(instance);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error destroying plugin", e);
			}
		}
	}

	@Override
	public PluginDesignMetadata getDesignMetadata() {
		throw new UnsupportedOperationException();
	}

}
