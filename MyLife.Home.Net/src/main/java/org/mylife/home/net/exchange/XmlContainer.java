package org.mylife.home.net.exchange;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "container")
public class XmlContainer {

	public XmlNetObject[] components;
	public String componentsVersion;
	public String documentVersion;
}
