package org.mylife.home.core.plugins.enhanced.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indique que la méthode est considérée comme un attribut. Elle ne doit prendre
 * aucun argument et son type de retour doit être Attribute.
 * 
 * @author pumbawoman
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginAttribute {

	/**
	 * Nom de l'attribut, ou nom de la méthode si null (en enlevant get si
	 * présent)
	 * 
	 * @return
	 */
	String name() default "";
	
	/**
	 * Index de l'attribut (doit être défini)
	 * @return
	 */
	int index();
}
