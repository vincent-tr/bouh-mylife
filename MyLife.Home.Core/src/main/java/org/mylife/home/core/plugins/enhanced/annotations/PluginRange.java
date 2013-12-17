package org.mylife.home.core.plugins.enhanced.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indique que le paramètre représenté est un interval. Doit être défini sur un
 * Integer, soit une sur une méthode pour le type de retour, soit sur un argument pour un paramètre
 * 
 * @author pumbawoman
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginRange {
	/**
	 * Valeur minimum de l'interval
	 * 
	 * @return
	 */
	int min();

	/**
	 * Valeur maximum de l'interval
	 * 
	 * @return
	 */
	int max();
}
