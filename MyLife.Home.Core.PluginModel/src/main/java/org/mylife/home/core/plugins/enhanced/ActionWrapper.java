package org.mylife.home.core.plugins.enhanced;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.core.plugins.enhanced.metadata.ActionMetadata;
import org.mylife.home.net.ActionExecutor;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetMember;

/**
 * Gestion d'une action
 * 
 * @author pumbawoman
 * 
 */
class ActionWrapper extends MemberWrapper implements ActionExecutor {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(ActionWrapper.class
			.getName());

	private final Object pluginInstance;
	private final ActionMetadata metadata;

	/**
	 * Création du membre
	 * 
	 * @return
	 */
	public ActionWrapper(Object pluginInstance, ActionMetadata metadata) {
		this.pluginInstance = pluginInstance;
		this.metadata = metadata;
	}

	/**
	 * Binding sur le NetObject
	 * 
	 * @param netObject
	 */
	@Override
	public NetMember createMember() {
		return new NetAction(metadata.getIndex(), metadata.getName(),
				metadata.getNetTypes());
	}

	/**
	 * Binding sur le NetObject
	 * 
	 * @param netObject
	 */
	@Override
	public synchronized void bind(NetObject netObject) {
		netObject.setActionExecutor(metadata.getName(), this);
	}

	/**
	 * Suppression du binding
	 * 
	 * @param netObject
	 */
	@Override
	public synchronized void unbind(NetObject netObject) {
		netObject.setActionExecutor(metadata.getName(), null);
	}

	/**
	 * Exécution de l'action
	 */
	@Override
	public void execute(NetObject obj, NetAction action, Object[] arguments) {

		// Conversion des paramètres
		Method method = metadata.getMethod();
		Class<?>[] argumentClasses = method.getParameterTypes();
		Object[] localArguments = new Object[argumentClasses.length];
		for (int i = 0; i < argumentClasses.length; i++) {
			localArguments[i] = fromNetValue(arguments[i], argumentClasses[i]);
		}

		// Exécution de l'action
		try {
			method.invoke(pluginInstance, localArguments);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error running plugin action", e);
		}
	}
}
