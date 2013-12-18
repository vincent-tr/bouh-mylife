package org.mylife.home.core.plugins;

/**
 * Erreur d'instanciation d'un plugin
 * @author pumbawoman
 *
 */
public class PluginInstanciateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1105389783841426991L;

	public PluginInstanciateException(Exception e) {
		super("Error instanciating plugin", e);
	}
}
