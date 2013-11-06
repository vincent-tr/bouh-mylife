package org.mylife.home.net.hub.irc.commands;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

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
	 * Obtention de la liste des classes implémentant des commandes
	 * 
	 * @return
	 * @throws IOException
	 */
	public Collection<Class<?>> listClasses() throws IOException {
		// http://dzone.com/snippets/get-all-classes-within-package
		Class<?> thisClass = this.getClass();
		ClassLoader classLoader = thisClass.getClassLoader();
		String packageName = thisClass.getPackage().getName();
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		Collection<Class<?>> classes = new ArrayList<Class<?>>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();

			String file = resource.getFile();
			// que les classes
			if (!file.endsWith(".class"))
				continue;
			// pas les inner classes
			if (file.indexOf('$') > -1)
				continue;

			// la classe est éligible, on remet le nom en classe
			String className = file.replace('/', '.');
			Class<?> clazz = null;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				continue;
			}

			// On vérifie si la classe correspond à une commande
			if (!clazz.isAssignableFrom(Command.class))
				continue;

			classes.add(clazz);
		}

		return classes;
	}
}
