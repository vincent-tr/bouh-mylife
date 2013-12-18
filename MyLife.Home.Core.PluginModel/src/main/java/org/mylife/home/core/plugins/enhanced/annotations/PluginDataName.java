package org.mylife.home.core.plugins.enhanced.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Modification d'une propriété de configuration afin de redéfinir son nom
 * 
 * @author pumbawoman
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginDataName {

	/**
	 * Nom de la propriété de configuration
	 * 
	 * @return
	 */
	String name();
	
	/**
	 * Nom de la propriété de configuration à l'affichage
	 * @return
	 */
	String displayName() default "";
}
