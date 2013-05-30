package mylife.home.irc.server.commands;

import java.util.List;

import mylife.home.irc.message.Numerics;
import mylife.home.irc.server.structure.Connection;

/**
 * Implémentation de PASS
 * 
 * @author pumbawoman
 * 
 */
public class Pass extends BaseCommand {

	/**
	 * Exécution de la commande
	 * 
	 * @param ce
	 */
	@Override
	public void handle(CommandExecution ce) {
		List<String> parameters = ce.getMessage().getParameters();
		if (parameters.size() == 0) {
			ce.reply(Numerics.ERR_NEEDMOREPARAMS, ce.getMessage().getCommand());
			return;
		}
		
		String pass = parameters.get(0);

		switch (ce.getConnection().getStatus()) {
		case Connection.STATUS_STARTING:
		case Connection.STATUS_PASS_DEFINED:
			break;

		default:
			ce.reply(Numerics.ERR_ALREADYREGISTRED);
			return;
		}
		
		ce.getConnection().getInitData().put(Connection.INIT_DATA_KEY_PASS, pass);
		ce.getConnection().setStatus(Connection.STATUS_PASS_DEFINED);
	}
}
