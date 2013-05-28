package mylife.home.irc.server.commands;

import java.util.List;

import mylife.home.irc.message.Numerics;
import mylife.home.irc.server.structure.Connection;
import mylife.home.irc.server.structure.User;

/**
 * Implémentation de NICK
 * 
 * @author pumbawoman
 * 
 */
public class Nick extends BaseCommand {

	/**
	 * Exécution de la commande
	 * 
	 * @param ce
	 */
	@Override
	public void handle(CommandExecution ce) {
		List<String> parameters = ce.getMessage().getParameters();
		int parametersSize = parameters.size();
		if (parametersSize == 0) {
			ce.reply(Numerics.ERR_NONICKNAMEGIVEN);
			return;
		}

		switch (ce.getConnection().getStatus()) {
		case Connection.STATUS_STARTING:
		case Connection.STATUS_PASS_DEFINED:
		case Connection.STATUS_CLIENT_NICK:
			// présentation d'un client (ou remplacement si 2ieme nick
			clientNew(ce);
			break;
			
		case Connection.STATUS_CLIENT_OK:
			// changement de nick
			clientChange(ce);
			break;
			
		case Connection.STATUS_SERVER_OK:
			// changement ou nouveau nick de serveur
			if(parametersSize == 7)
				serverNew(ce);
			else
				serverChange(ce);
			break;
		}
	}
	
	/**
	 * Création d'un client
	 * @param ce
	 */
	private void clientNew(CommandExecution ce) {
		Connection con = ce.getConnection();
		String nick = ce.getMessage().getParameters().get(0);
		User user = (User)con.getComponent();
		if(user == null)
			con.setComponent(user = new User());
	}
	
	/**
	 * Changement d'un nick client
	 * @param ce
	 */
	private void clientChange(CommandExecution ce) {
		
	}
	
	/**
	 * Création d'un nouveau client sur un autre serveur
	 * @param ce
	 */
	private void serverNew(CommandExecution ce) {
		
	}
	
	/**
	 * Changement de nick d'un client sur un autre serveur
	 * @param ce
	 */
	private void serverChange(CommandExecution ce) {
		
	}

}
