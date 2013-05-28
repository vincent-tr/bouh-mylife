package mylife.home.irc.server.commands;

import java.util.List;

import mylife.home.irc.message.Numerics;

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
		
		String nick = parameters.get(0);
		String hopCount = null;
		if(parametersSize > 1)
			hopCount = parameters.get(1);
	}

}
