package org.mylife.home.core.plugins.enhanced.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indique que l'interface spécifiée correspond à la configuration du plugin.
 * Toutes ses méthodes doivent alors être de type primitif ou String, et sans
 * paramètre
 * 
 * @author pumbawoman
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginConfiguration {

}
