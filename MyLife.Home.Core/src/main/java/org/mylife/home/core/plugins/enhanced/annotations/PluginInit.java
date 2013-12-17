package org.mylife.home.core.plugins.enhanced.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Appliqué à une méthode sans paramètre ou avec un paramètre de type
 * PluginContext, et/ou un paramètre d'un type d'interface décoré par
 * PluginConfiguration, indique que la méthode doit être appelée à
 * l'initialisation du plugin
 * 
 * @author pumbawoman
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginInit {

}
