package org.mylife.home.net.exchange.net;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "netContainer")
public class XmlNetContainer {

	@XmlElementWrapper(name = "components")
	@XmlElement(name = "component")
	public XmlNetObject[] components;
	
	public String componentsVersion;
	public String documentVersion;
}
