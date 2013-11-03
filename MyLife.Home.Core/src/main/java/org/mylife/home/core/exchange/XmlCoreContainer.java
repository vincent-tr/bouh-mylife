package org.mylife.home.core.exchange;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "coreContainer")
public class XmlCoreContainer {

	public XmlCoreComponent[] components;
	public XmlCoreLink[] links;
	
	public String documentName;
	public String documentVersion;
}
