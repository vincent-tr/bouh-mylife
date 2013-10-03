package org.mylife.home.net;

import org.mylife.home.net.structure.NetAction;

/**
 * Gestion de l'ex�cution d'une action
 * @author pumbawoman
 *
 */
public interface ActionExecutor {

	void execute(NetObject obj, NetAction action, Object[] arguments);
	
}
