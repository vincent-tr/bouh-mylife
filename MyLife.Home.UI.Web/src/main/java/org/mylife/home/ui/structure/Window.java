package org.mylife.home.ui.structure;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mylife.home.net.exchange.ui.XmlUiComponent;
import org.mylife.home.net.exchange.ui.XmlUiWindow;

/**
 * Représentation d'une fenêtre
 * 
 * @author pumbawoman
 * 
 */
public class Window {

	private final String id;
	private final String backgroundId;
	private final Map<String, Component> components;

	/* internal */Window(XmlUiWindow source) {
		this.id = source.id;
		this.backgroundId = source.backgroundId;

		Map<String, Component> components = new HashMap<String, Component>();
		if (source.components != null) {
			for (XmlUiComponent xmlComponent : source.components) {
				Component component = new Component(xmlComponent, this);
				components.put(component.getId(), component);
			}
		}
		this.components = Collections.unmodifiableMap(components);
	}

	/**
	 * Identifiant
	 */
	public String getId() {
		return id;
	}

	/**
	 * Fond
	 */
	public String getBackgroundId() {
		return backgroundId;
	}

	/**
	 * Composants
	 */
	public Collection<Component> getComponents() {
		return components.values();
	}
	
	public Component getComponent(String id) {
		return components.get(id);
	}
}
