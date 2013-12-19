package org.mylife.home.core.exchange;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "coreContainer")
public class XmlCoreContainer {

	@XmlElementWrapper(name = "components")
	@XmlElement(name = "component")
	public XmlCoreComponent[] components;

	@XmlElementWrapper(name = "links")
	@XmlElement(name = "link")
	public XmlCoreLink[] links;
	
	public String documentName;
	public String documentVersion;
}
