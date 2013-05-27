package mylife.home.irc.server.commands;

import java.util.List;

import mylife.home.irc.message.Message;
import mylife.home.irc.server.structure.Connection;

/**
 * Implémentation du mot de passe
 * @author pumbawoman
 *
 */
public class Pass extends BaseCommand {

	/**
	 * Exécution de la commande
	 * @param connection
	 * @param message
	 */
	@Override
	public void handle(Connection connection, Message message) {
		List<String> parameters = message.getParameters();
		if(parameters.size() == 0)
			
	}

}
