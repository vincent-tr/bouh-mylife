package mylife.home.net;

import mylife.home.net.structure.NetAction;

/**
 * Gestion de l'exécution d'une action
 * @author pumbawoman
 *
 */
public interface ActionExecutor {

	void execute(NetObject obj, NetAction action, Object[] arguments);
	
}
