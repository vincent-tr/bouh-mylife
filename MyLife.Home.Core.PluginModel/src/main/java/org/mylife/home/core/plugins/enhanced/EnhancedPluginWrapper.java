package org.mylife.home.core.plugins.enhanced;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.core.plugins.Plugin;
import org.mylife.home.core.plugins.PluginContext;
import org.mylife.home.core.plugins.design.PluginDesignMetadata;
import org.mylife.home.core.plugins.enhanced.metadata.ActionMetadata;
import org.mylife.home.core.plugins.enhanced.metadata.AttributeMetadata;
import org.mylife.home.core.plugins.enhanced.metadata.MemberMetadata;
import org.mylife.home.core.plugins.enhanced.metadata.PluginClassMetadata;
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
	private Collection<MemberWrapper> members;

	public EnhancedPluginWrapper(PluginClassMetadata metadata) throws Exception {
		this.metadata = metadata;
		this.instance = metadata.getPluginClass().newInstance();
	}

	@Override
	public void init(PluginContext context) throws Exception {

		this.context = context;

		if(context.getPurpose() == PluginContext.PURPOSE_DESIGN)
			return;
		
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
		Collection<NetMember> netMembers = new ArrayList<NetMember>();
		members = createMemberWrappers();
		for (MemberWrapper member : members) {
			netMembers.add(member.createMember());
		}
		netObject = new NetObject(context.getId(), new NetClass(netMembers));
		for (MemberWrapper member : members) {
			member.bind(netObject);
		}
		context.publishObject(netObject);
	}

	private Collection<MemberWrapper> createMemberWrappers() throws Exception {
		Collection<MemberWrapper> members = new ArrayList<MemberWrapper>();
		for (MemberMetadata memberMetadata : metadata.getMembers()) {
			MemberWrapper member = null;
			if (memberMetadata instanceof AttributeMetadata) {
				member = new AttributeWrapper(instance,
						(AttributeMetadata) memberMetadata);
			}
			if (memberMetadata instanceof ActionMetadata) {
				member = new ActionWrapper(instance,
						(ActionMetadata) memberMetadata);
			}

			if (member == null)
				throw new UnsupportedOperationException();

			members.add(member);
		}

		return members;
	}

	@Override
	public void destroy() {

		if(context.getPurpose() == PluginContext.PURPOSE_DESIGN)
			return;
		
		// Suppression des binding entre l'objet réseau et le plugin
		for (MemberWrapper member : members) {
			member.unbind(netObject);
		}

		// Exécution des méthodes de destruction
		for (Method method : metadata.getDestroyMethods()) {
			Collection<Object> args = new ArrayList<Object>();
			for (Class<?> argClass : method.getParameterTypes()) {
				if (argClass.equals(PluginContext.class))
					args.add(context);
			}
			try {

				method.invoke(instance, args);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error destroying plugin", e);
			}
		}
	}

	@Override
	public PluginDesignMetadata getDesignMetadata() {
		if(context.getPurpose() == PluginContext.PURPOSE_RUNTIME)
			throw new UnsupportedOperationException();
		
		// TODO
		throw new UnsupportedOperationException();
	}

}
