package org.mylife.home.core.plugins.enhanced.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation à placer sur un plugin lisible par annotations
 * 
 * @author pumbawoman
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {

	/**
	 * Type du plugin (ou la classs du plugin si null)
	 * 
	 * @return
	 */
	String type() default "";

	/**
	 * Affichage pour le type du plugin (ou la classs du plugin si null)
	 * 
	 * @return
	 */
	String displayType() default "";
}
