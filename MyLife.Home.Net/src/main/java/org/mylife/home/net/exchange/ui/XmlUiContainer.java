package org.mylife.home.net.exchange.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Conteneur pour la description de l'ui.
 * 
 * @author pumbawoman
 * 
 */
@XmlRootElement(name = "uiContainer")
public class XmlUiContainer {

	@XmlElementWrapper(name = "images")
	@XmlElement(name = "image")
	public XmlUiImage []images;
	
	@XmlElementWrapper(name = "windows")
	@XmlElement(name = "window")
	public XmlUiWindow[] windows;
	
	public String defaultWindowId;
	
	public String documentName;
	public String documentVersion;
}
