package org.mylife.home.core.exchange.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Conteneur pour la description de l'ui.
 * 
 * @author pumbawoman
 * 
 */
public class XmlUiContainer {

	@XmlElementWrapper(name = "images")
	@XmlElement(name = "image")
	public XmlUiImage []images;
	
	@XmlElementWrapper(name = "windows")
	@XmlElement(name = "window")
	public XmlUiWindow[] windows;
	
	@XmlElementWrapper(name = "panels")
	@XmlElement(name = "panel")
	public XmlUiPanel[] panels;
	
	// TODO : panel templates
	
	public String defaultWindowId;
	
	public String documentName;
	public String documentVersion;
}
