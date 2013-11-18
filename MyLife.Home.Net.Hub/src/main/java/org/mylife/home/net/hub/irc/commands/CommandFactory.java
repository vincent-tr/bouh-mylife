package org.mylife.home.net.hub.irc.commands;

import java.io.IOException;
import java.util.Collection;

import org.mylife.home.common.PluginUtils;
import org.mylife.home.net.hub.irc.Command;

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

	}

	/**
	 * Obtention de la liste des classes impl�mentant des commandes
	 * 
	 * @return
	 * @throws IOException
	 */
	public Collection<Class<?>> listClasses() throws IOException {
		String packageName = this.getClass().getPackage().getName();
		return PluginUtils.listClassesInPackageAssignableFrom(packageName, Command.class);
	}
}
