package mylife.home.irc.server.commands;

import java.util.List;

import mylife.home.irc.message.Message;
import mylife.home.irc.message.Numerics;
import mylife.home.irc.message.Prefix;
import mylife.home.irc.message.UserPrefix;
import mylife.home.irc.server.structure.Connection;
import mylife.home.irc.server.structure.Network;
import mylife.home.irc.server.structure.Server;
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
			if (parametersSize == 7)
				serverNew(ce);
			else
				serverChange(ce);
			break;
		}
	}

	/**
	 * Vérifie si le nick passé en paramètre existe
	 * 
	 * @param nick
	 * @return
	 */
	private boolean checkNickExists(String nick) {
		return this.getOwner().getNetwork().findUser(nick) != null;
	}

	/**
	 * Création d'un client
	 * 
	 * @param ce
	 */
	private void clientNew(CommandExecution ce) {
		String nick = ce.getMessage().getParameters().get(0);

		if (checkNickExists(nick)) {
			ce.reply(Numerics.ERR_NICKNAMEINUSE, nick);
			return;
		}

		ce.getConnection().getInitData()
				.put(Connection.INIT_DATA_KEY_NICK, nick);
	}

	/**
	 * Changement d'un nick client
	 * 
	 * @param ce
	 */
	private void clientChange(CommandExecution ce) {
		String nick = ce.getMessage().getParameters().get(0);
		User user = (User) ce.getConnection().getComponent();

		if (checkNickExists(nick)) {
			ce.reply(Numerics.ERR_NICKNAMEINUSE, nick);
			return;
		}

		// meme nick on ne fait rien
		if(user.getNick().equalsIgnoreCase(nick))
			return;
		
		// création du préfix avec l'ancien nick
		Prefix prefix = user.prefix();
		
		user.setNick(nick);

		// dispatch du nick
		Message message = new Message(prefix, "NICK", nick);
		ce.serverBroadcast(message);
	}

	/**
	 * Création d'un nouveau client sur un autre serveur
	 * 
	 * @param ce
	 */
	private void serverNew(CommandExecution ce) {

		String nick = ce.getMessage().getParameters().get(0);
		int hopCount = Integer.parseInt(ce.getMessage().getParameters().get(1));
		String user = ce.getMessage().getParameters().get(2);
		String host = ce.getMessage().getParameters().get(3);
		int token = Integer.parseInt(ce.getMessage().getParameters().get(4));
		String modes = ce.getMessage().getParameters().get(5);
		String realName = ce.getMessage().getParameters().get(6);
		
		// check de collision
		Network net = ce.getServer().getNetwork();
		User to = net.findUser(nick);
		if (to != null) {
			// nick collision : on tue le nick, on le supprime de la base et on retourne sans créer le nouveau nick
			killCollision(ce, to);
			return;
		}
		
		// ajout de l'utilisateur
		++hopCount;
		Server server = net.findServer(token);
		User newUser = new User(nick, user, host, realName, server, null);
		net.getUsers().add(newUser);
		Mode.setUserMode(newUser, modes);
		
		// broadcast
		Message message = new Message(null, "NICK", nick, String.valueOf(hopCount),
				newUser.getUser(), newUser.getHost(), String.valueOf(newUser.getServer().getToken()),
				newUser.getModes().toString(), newUser.getRealName());
		Server fromServer = (Server)ce.getConnection().getComponent();
		ce.serverBroadcast(message, fromServer);
	}

	/**
	 * Changement de nick d'un client sur un autre serveur
	 * 
	 * @param ce
	 */
	private void serverChange(CommandExecution ce) {
		String nick = ce.getMessage().getParameters().get(0);
		Network net = ce.getServer().getNetwork();
		User from = net.findUser(((UserPrefix)ce.getMessage().getPrefix()).getUser().getNick());
		User to = net.findUser(nick);
		if (to != null) {
			// nick collision : on tue le nouveau et l'ancien nick, et on supprime tout de la base
			killCollision(ce, from);
			killCollision(ce, to);
			return;
		}
		
		// enregistrement
		from.setNick(nick);
		
		// broadcast
		Server fromServer = (Server)ce.getConnection().getComponent();
		ce.serverBroadcast(ce.getMessage(), fromServer);
	}
	
	/**
	 * Ejecte un utilisateur suite à collision
	 * @param ce
	 * @param user
	 */
	private void killCollision(CommandExecution ce, User user) {
		ce.getServer().getNetwork().deleteUser(user);
		Message message = new Message(ce.selfPrefix(), "KILL", user.getNick(), Numerics.ERR_NICKCOLLISION.textMessage());
		ce.serverBroadcast(message);
	}

}
