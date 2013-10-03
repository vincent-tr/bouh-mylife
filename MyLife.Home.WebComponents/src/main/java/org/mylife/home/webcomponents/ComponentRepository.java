package org.mylife.home.webcomponents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Dépot de composants
 * @author pumbawoman
 *
 */
public final class ComponentRepository {

	private ComponentRepository() {
	}
	
	private static final Collection<Component> components = Collections.synchronizedCollection(new ArrayList<Component>());
	
	/**
	 * Enregistrement d'un composant
	 * @param component
	 */
	public static void registerComponent(Component component) {
		components.add(component);
	}

	/**
	 * D�senregistrement d'un composant
	 * @param component
	 */
	public static void unregisterComponent(Component component) {
		components.remove(component);
	}
	
	/**
	 * Liste des composants
	 * @return
	 */
	public static Collection<Component> getComponents() {
		return Collections.unmodifiableCollection(components);
	}
}
