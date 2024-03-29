package org.mylife.home.core.plugins.enhanced.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indique que la méthode est considérée comme une action. Sa valeur de retour
 * est ignorée, et ses arguments doivent être des Enum ou des Integer décorés de
 * PluginRange
 * 
 * @author pumbawoman
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginAction {

	/**
	 * Nom de l'action, ou nom de la méthode si null (en enlevant get si
	 * présent)
	 * 
	 * @return
	 */
	String name() default "";

	/**
	 * Affichage pour l'action du plugin (ou le nom si null)
	 * 
	 * @return
	 */
	String displayType() default "";

	/**
	 * Index de l'action (doit être défini)
	 * 
	 * @return
	 */
	int index();
}
