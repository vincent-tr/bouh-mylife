package mylife.home.irc.server.commands;

import java.util.List;

import mylife.home.irc.message.Message;
import mylife.home.irc.message.Numerics;
import mylife.home.irc.server.structure.Connection;
import mylife.home.irc.server.structure.Network;
import mylife.home.irc.server.structure.Server;

/**
 * Implémentation de USER
 * 
 * @author pumbawoman
 * 
 */
public class User extends BaseCommand {

	/**
	 * Exécution de la commande
	 * 
	 * @param ce
	 */
	@Override
	public void handle(CommandExecution ce) {
		List<String> parameters = ce.getMessage().getParameters();
		int parametersSize = parameters.size();
		if (parametersSize < 4) {
			ce.reply(Numerics.ERR_NEEDMOREPARAMS, ce.getMessage().getCommand());
			return;
		}

		Connection con = ce.getConnection();
		if (con.getStatus() != Connection.STATUS_CLIENT_NICK) {
			ce.reply(Numerics.ERR_ALREADYREGISTRED);
			return;
		}

		String user = parameters.get(0);
		String host = null; // TODO : dns reverse
		String realName = parameters.get(4);
		String nick = con.getInitData().get(Connection.INIT_DATA_KEY_NICK);

		// check de collision
		Network net = ce.getServer().getNetwork();

		// ajout de l'utilisateur
		Server me = net.findLocalServer();
		mylife.home.irc.server.structure.User newUser = new mylife.home.irc.server.structure.User(
				nick, user, host, realName, me, con);
		net.getUsers().add(newUser);
		// TODO : set modes + envoi à l'utilisateur

		// broadcast
		Message message = new Message(null, "NICK", nick, "0",
				newUser.getUser(), newUser.getHost(), String.valueOf(me
						.getToken()), newUser.getModes().toString(),
				newUser.getRealName());
		ce.serverBroadcast(message);
	}
}
