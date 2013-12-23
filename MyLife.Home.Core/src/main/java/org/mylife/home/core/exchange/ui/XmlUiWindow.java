package org.mylife.home.core.exchange.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class XmlUiWindow {

	public String id;
	public String backgroundId;
	
	@XmlElementWrapper(name = "components")
	@XmlElement(name = "component")
	public XmlUiComponent[] components;
}
