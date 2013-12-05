package org.mylife.home.net.hub.irc.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mylife.home.common.PluginUtils;

/**
 * Fabrique de commandes
 * 
 * @author TRUMPFFV
 * 
 */
public class CommandFactory {

	private final static CommandFactory instance = new CommandFactory();

	public static CommandFactory getInstance() {
		return instance;
	}

	private CommandFactory() {
		LoadCommands();
	}

	/**
	 * Obtention de la liste des classes implementant des commandes
	 * 
	 * @return
	 * @throws IOException
	 */
	private Collection<Class<?>> listClasses() throws IOException {
		String packageName = this.getClass().getPackage().getName();
		return PluginUtils.listClassesInPackageAssignableFrom(packageName,
				Command.class);
	}

	private void LoadCommand(Class<?> clazz) {
		try {
			Command cmd = (Command) clazz.newInstance();
			String name = cmd.getName();
			if (StringUtils.isEmpty(name))
				throw new IllegalArgumentException("Invalid command name");
			name = name.toUpperCase();
			if (commands.containsKey(name))
				throw new IllegalArgumentException("Command name alreay exists");
			commands.put(name, cmd);

		} catch (Exception e) {
			throw new RuntimeException("Error loading command "
					+ clazz.toString(), e);
		}
	}

	private void LoadCommands() {
		Collection<Class<?>> list = null;
		try {
			list = listClasses();
		} catch (IOException ex) {
			throw new RuntimeException("Error loading commands", ex);
		}

		for (Class<?> clazz : list) {
			LoadCommand(clazz);
		}
	}

	private final Map<String, Command> commands = new HashMap<String, Command>();
	private final ConnectionClosedCommand connectionClosedCommand = new ConnectionClosedCommand();
	private final ConnectionOpenedCommand connectionOpenedCommand = new ConnectionOpenedCommand();

	public Command getCommand(String name) {
		if (StringUtils.isEmpty(name))
			throw new IllegalArgumentException("Invalid command name");
		name = name.toUpperCase();

		return commands.get(name);
	}
	
	public ConnectionClosedCommand getConnectionClosedCommand() {
		return connectionClosedCommand;
	}
	
	public ConnectionOpenedCommand getConnectionOpenedCommand() {
		return connectionOpenedCommand;
	}
}
